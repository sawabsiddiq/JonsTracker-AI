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
import com.example.data.remote.DemoEmail
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
        val query = "newer_than:2d"

        try {
            val listResponse = GmailClient.service.listMessages(bearerToken = bearer, query = query, maxResults = 10)
            val messages = listResponse.messages ?: emptyList()

            if (messages.isEmpty()) {
                Log.d(TAG, "Inbox is fully synchronized. No new messages.")
                return@withContext Result.success()
            }

            val unprocessed = messages.filter { !repository.isEmailProcessed(it.id) }
            if (unprocessed.isEmpty()) {
                Log.d(TAG, "All raw emails are already logged.")
                return@withContext Result.success()
            }

            for (ref in unprocessed) {
                try {
                    val detail = GmailClient.service.getMessage(bearer, ref.id)
                    val email = GmailClient.mapToDemoEmail(detail)

                    // AI extraction with fallback
                    val extractionResult = GeminiClient.extractJobDetails(email.body)
                    val finalResult = extractionResult ?: generateLocalExtractionFallback(email)

                    if (finalResult.isJobRelated && finalResult.confidence >= 0.82f) {
                        // Persist to pending AI approvals table so user can review/edit/confirm
                        val pending = PendingAiExtraction(
                            messageId = email.messageId,
                            threadId = email.threadId,
                            sender = email.sender,
                            subject = email.subject,
                            dateString = email.dateString,
                            body = email.body,
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
                            source = finalResult.source,
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

    private fun generateLocalExtractionFallback(email: DemoEmail): JobExtractionResult {
        val subject = email.subject.lowercase()
        val body = email.body.lowercase()

        val isJob = subject.contains("apply") || subject.contains("application") ||
                subject.contains("status") || subject.contains("interview") ||
                subject.contains("offer") || subject.contains("careers") ||
                subject.contains("hiring") || subject.contains("assessment") ||
                body.contains("thank you for applying") || body.contains("coding challenge")

        if (!isJob) {
            return JobExtractionResult(isJobRelated = false, confidence = 0.90f, eventType = "irrelevant")
        }

        val company = when {
            subject.contains("google") -> "Google"
            subject.contains("stripe") -> "Stripe"
            subject.contains("meta") -> "Meta"
            subject.contains("apple") -> "Apple"
            subject.contains("netflix") -> "Netflix"
            subject.contains("amazon") -> "Amazon"
            else -> "Unknown Company"
        }

        val type = when {
            subject.contains("offer") -> "offer"
            subject.contains("interview") || body.contains("schedule your interview") -> "interview_invitation"
            subject.contains("assessment") || body.contains("coding challenge") || body.contains("hackerrank") -> "assessment_invitation"
            subject.contains("thank you") || body.contains("application received") -> "application_confirmation"
            subject.contains("rejection") || body.contains("not moving forward") -> "rejection"
            else -> "recruiter_reply"
        }

        val status = when (type) {
            "offer" -> "Offer"
            "interview_invitation" -> "Interview"
            "assessment_invitation" -> "Assessment"
            "application_confirmation" -> "Applied"
            "rejection" -> "Rejected"
            else -> "Recruiter replied"
        }

        return JobExtractionResult(
            isJobRelated = true,
            confidence = 0.85f,
            eventType = type,
            companyName = company,
            jobTitle = "Software Developer",
            applicationStatus = status,
            summary = "Heuristic match: ${email.subject}",
            nextAction = "Verify and confirm this entry",
            source = "Gmail"
        )
    }
}
