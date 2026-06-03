package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.local.PendingAiExtraction
import com.example.data.local.ProcessedEmail
import com.example.data.local.SecurePrefsManager
import com.example.data.remote.GeminiClient
import com.example.data.remote.GmailClient
import com.example.data.remote.JobExtractionResult
import com.example.data.remote.ParsedEmail
import com.example.data.repository.JobRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GmailSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = "GmailSyncWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting periodic Gmail background sync worker...")

        if (SecurePrefsManager.hasFailed) {
            Log.e(TAG, "Secure storage is unavailable on this device. Gmail sync is disabled.")
            return@withContext Result.failure()
        }

        val context = applicationContext
        val prefs = SecurePrefsManager.getSecurePrefs(context)
        val token = prefs.getString("gmail_token", null)

        val autoSync = prefs.getBoolean("auto_sync", true)
        if (!autoSync) {
            Log.d(TAG, "Sync is disabled in user preferences.")
            return@withContext Result.success()
        }

        if (token.isNullOrEmpty()) {
            Log.d(TAG, "No Gmail OAuth credentials available in secure vault.")
            return@withContext Result.success()
        }

        val database = AppDatabase.getDatabase(context)
        val repository = JobRepository(database.jobDao())

        val bearer = "Bearer $token"
        
        // Read configuration bounds or use safe defaults
        val scanDays = prefs.getInt("scan_days", 30)
        val query = "newer_than:${scanDays}d (apply OR application OR interview OR offer OR career OR hiring OR assessment OR resume OR job)"
        val maxFetch = prefs.getInt("max_messages_to_fetch", 50)
        val maxAnalyze = prefs.getInt("max_emails_to_analyze", 25)
        val scanMode = prefs.getString("scan_mode", "Balanced") ?: "Balanced"

        try {
            val listResponse = GmailClient.service.listMessages(bearerToken = bearer, query = query, maxResults = maxFetch)
            val messages = listResponse.messages ?: emptyList()

            if (messages.isEmpty()) {
                Log.d(TAG, "Inbox is fully synchronized. No new messages.")
                return@withContext Result.success()
            }

            // Deduplicate: skip emails that are either processed or already pending in AI Inbox
            val unprocessed = messages.filter { 
                !repository.isEmailProcessed(it.id) && !repository.isPendingAiExtraction(it.id)
            }
            if (unprocessed.isEmpty()) {
                Log.d(TAG, "All raw emails are already logged.")
                return@withContext Result.success()
            }

            var aiScansCount = 0

            for (ref in unprocessed) {
                if (aiScansCount >= maxAnalyze) {
                    Log.d(TAG, "Reached bg job max AI scan limit ($maxAnalyze)")
                    break
                }

                try {
                    val detail = GmailClient.service.getMessage(bearer, ref.id)
                    val email = GmailClient.mapToParsedEmail(detail)

                    // Run the exact same filtering checks
                    val passesFilter = shouldAnalyzeLocalFilter(email.subject, email.snippet, email.body, scanMode)
                    if (!passesFilter) {
                        val logEmail = ProcessedEmail(
                            gmailMessageId = email.messageId,
                            threadId = email.threadId,
                            subject = email.subject,
                            sender = email.sender,
                            dateString = email.dateString,
                            snippet = email.snippet,
                            classification = "irrelevant",
                            confidence = 1.0f,
                            linkedApplicationId = null
                        )
                        repository.insertProcessedEmail(logEmail)
                        continue
                    }

                    // Trim body to prevent excess token usage
                    val trimmedBody = if (email.body.length > 4000) email.body.substring(0, 4000) else email.body
                    aiScansCount++

                    // AI extraction with fallback
                    val extractionResult = try {
                        GeminiClient.extractJobDetails(trimmedBody)
                    } catch (e: Exception) {
                        null
                    }

                    val finalResult = extractionResult ?: createNeedsManualReviewResult(email, "AI Extraction failed or timed out")

                    if (finalResult.isJobRelated) {
                        // Truncated body for local storage (500 to 1000 characters excerpt max)
                        val bodyExcerpt = if (email.body.length > 1000) email.body.substring(0, 1000) else email.body

                        // Persist to pending AI approvals table so user can review/edit/confirm
                        val pending = PendingAiExtraction(
                            messageId = email.messageId,
                            threadId = email.threadId,
                            sender = email.sender,
                            subject = email.subject,
                            dateString = email.dateString,
                            bodyExcerpt = bodyExcerpt,
                            snippet = email.snippet,
                            isJobRelated = finalResult.isJobRelated,
                            confidence = finalResult.confidence,
                            eventType = finalResult.eventType,
                            companyName = finalResult.companyName,
                            jobTitle = finalResult.jobTitle,
                            applicationStatus = finalResult.applicationStatus,
                            eventDate = finalResult.eventDate,
                            deadline = finalResult.deadline,
                            recruiterName = finalResult.recruiterName,
                            recruiterEmail = finalResult.recruiterEmail,
                            source = finalResult.source ?: "Gmail",
                            summary = finalResult.summary,
                            nextAction = finalResult.nextAction,
                            followUpDate = finalResult.followUpDate
                        )
                        repository.insertPendingExtraction(pending)
                    } else {
                        // Email is irrelevant, log as processed so we don't query it again
                        val logEmail = ProcessedEmail(
                            gmailMessageId = email.messageId,
                            threadId = email.threadId,
                            subject = email.subject,
                            sender = email.sender,
                            dateString = email.dateString,
                            snippet = email.snippet,
                            classification = "irrelevant",
                            confidence = finalResult.confidence,
                            linkedApplicationId = null
                        )
                        repository.insertProcessedEmail(logEmail)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Skipping email item ${ref.id} due to inline extraction failure.", e)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Background task sync failed completely.", e)
            Result.retry()
        }
    }

    private fun shouldAnalyzeLocalFilter(subject: String, snippet: String, body: String, mode: String): Boolean {
        val subLower = subject.lowercase()
        val snipLower = snippet.lowercase()
        val bodyLower = body.lowercase()

        // Block obvious spam or billing alerts commonly matching keywords
        if (subLower.contains("receipt") || subLower.contains("invoice") || subLower.contains("billing") ||
            subLower.contains("transaction") || subLower.contains("subscription") || subLower.contains("payment") ||
            subLower.contains("your order") || subLower.contains("shipped") || subLower.contains("delivery") ||
            subLower.contains("coupon") || subLower.contains("deal of the day") || subLower.contains("newsletter") ||
            subLower.contains("unlimited plans") || subLower.contains("security alert") || subLower.contains("verify your account")
        ) {
            return false
        }

        return when (mode.trim().lowercase()) {
            "low cost" -> {
                subLower.contains("apply") || subLower.contains("application") ||
                        subLower.contains("status") || subLower.contains("interview") ||
                        subLower.contains("offer") || subLower.contains("careers") ||
                        subLower.contains("hiring") || subLower.contains("assessment") ||
                        subLower.contains("congratulations") || subLower.contains("next steps") ||
                        snipLower.contains("schedule your interview") || snipLower.contains("coding challenge")
            }
            "balanced" -> {
                subLower.contains("apply") || subLower.contains("application") ||
                        subLower.contains("status") || subLower.contains("interview") ||
                        subLower.contains("offer") || subLower.contains("careers") ||
                        subLower.contains("hiring") || subLower.contains("assessment") ||
                        subLower.contains("congratulations") || subLower.contains("next steps") ||
                        subLower.contains("job") || subLower.contains("resume") ||
                        subLower.contains("candidate") || subLower.contains("recruiting") ||
                        snipLower.contains("apply") || snipLower.contains("application") ||
                        snipLower.contains("interview") || snipLower.contains("offer") ||
                        snipLower.contains("hiring") || snipLower.contains("job") ||
                        bodyLower.contains("thank you for applying") || bodyLower.contains("coding challenge")
            }
            else -> {
                // Thorough: let everything through
                true
            }
        }
    }

    private fun createNeedsManualReviewResult(email: ParsedEmail, reason: String = "AI Parse Failed"): JobExtractionResult {
        return JobExtractionResult(
            isJobRelated = true,
            confidence = 0.5f,
            eventType = "follow_up_needed",
            companyName = null,
            jobTitle = null,
            applicationStatus = "Applied",
            summary = "Needs manual review ($reason). Original subject: ${email.subject}",
            nextAction = "Verify and correct any missing company, role, or status details manually.",
            source = "Gmail",
            eventDate = null
        )
    }
}
