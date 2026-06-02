package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.JobApplication
import com.example.data.local.JobEvent
import com.example.data.local.ProcessedEmail
import com.example.data.local.PendingAiExtraction
import com.example.data.local.SecurePrefsManager
import com.example.data.analytics.CrashlyticsHelper
import com.example.data.repository.JobRepository
import com.example.data.remote.DemoEmail
import com.example.data.remote.GeminiClient
import com.example.data.remote.GmailClient
import com.example.data.remote.JobExtractionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class Screen {
    object Onboarding : Screen()
    object Today : Screen()
    object Applications : Screen()
    object AiInbox : Screen()
    object Pipeline : Screen()
    object Insights : Screen()
    object Settings : Screen()
    data class Detail(val applicationId: Int) : Screen()
}

sealed class SyncErrorType {
    object None : SyncErrorType()
    object NoInternet : SyncErrorType()
    object AuthFailure : SyncErrorType()
    object TokenExpired : SyncErrorType()
    object GeminiFailure : SyncErrorType()
    object EmptyInbox : SyncErrorType()
    object DuplicateJobUpdate : SyncErrorType()
}

class JobTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val jobDao = AppDatabase.getDatabase(application).jobDao()
    private val repository = JobRepository(jobDao)

    private val TAG = "JobTrackerViewModel"

    // --- State Flows ---
    val allApplications: StateFlow<List<JobApplication>> = repository.allApplications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val processedEmails: StateFlow<List<ProcessedEmail>> = repository.allProcessedEmails
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val prefs = SecurePrefsManager.getSecurePrefs(application)

    private val themePrefs = com.example.data.local.ThemePreferences(application)
    val themeMode: StateFlow<com.example.data.local.AppThemeMode> = themePrefs.themeModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = com.example.data.local.AppThemeMode.SYSTEM
        )

    fun setThemeMode(mode: com.example.data.local.AppThemeMode) {
        viewModelScope.launch {
            themePrefs.setThemeMode(mode)
        }
    }

    private val _scanDays = MutableStateFlow(prefs.getInt("scan_days", 15))
    val scanDays: StateFlow<Int> = _scanDays.asStateFlow()

    private val _maxMessagesToFetch = MutableStateFlow(prefs.getInt("max_messages_to_fetch", 100))
    val maxMessagesToFetch: StateFlow<Int> = _maxMessagesToFetch.asStateFlow()

    private val _maxEmailsToAnalyze = MutableStateFlow(prefs.getInt("max_emails_to_analyze", 50))
    val maxEmailsToAnalyze: StateFlow<Int> = _maxEmailsToAnalyze.asStateFlow()

    private val _includeSpamTrash = MutableStateFlow(prefs.getBoolean("include_spam_trash", false))
    val includeSpamTrash: StateFlow<Boolean> = _includeSpamTrash.asStateFlow()

    private val _scanMode = MutableStateFlow(prefs.getString("scan_mode", "Balanced") ?: "Balanced")
    val scanMode: StateFlow<String> = _scanMode.asStateFlow()

    fun setScanDays(days: Int) {
        prefs.edit().putInt("scan_days", days).apply()
        _scanDays.value = days
    }

    fun setMaxMessagesToFetch(max: Int) {
        prefs.edit().putInt("max_messages_to_fetch", max).apply()
        _maxMessagesToFetch.value = max
    }

    fun setMaxEmailsToAnalyze(max: Int) {
        prefs.edit().putInt("max_emails_to_analyze", max).apply()
        _maxEmailsToAnalyze.value = max
    }

    fun setIncludeSpamTrash(include: Boolean) {
        prefs.edit().putBoolean("include_spam_trash", include).apply()
        _includeSpamTrash.value = include
    }

    fun setScanMode(mode: String) {
        prefs.edit().putString("scan_mode", mode).apply()
        _scanMode.value = mode
    }

    private val _gmailAccessToken = MutableStateFlow(prefs.getString("gmail_token", null))
    val gmailAccessToken: StateFlow<String?> = _gmailAccessToken.asStateFlow()

    private val _autoSyncEnabled = MutableStateFlow(prefs.getBoolean("auto_sync", true))
    val autoSyncEnabled: StateFlow<Boolean> = _autoSyncEnabled.asStateFlow()

    private val _lastGmailScan = MutableStateFlow(prefs.getLong("last_gmail_scan", 0L))
    val lastGmailScan: StateFlow<Long> = _lastGmailScan.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Onboarding)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // --- Gmail Scan / AI Review state ---
    private val _gmailSyncResults = MutableStateFlow<List<Pair<DemoEmail, JobExtractionResult>>>(emptyList())
    val gmailSyncResults: StateFlow<List<Pair<DemoEmail, JobExtractionResult>>> = _gmailSyncResults.asStateFlow()

    private val _syncingState = MutableStateFlow(false)
    val syncingState: StateFlow<Boolean> = _syncingState.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private val _syncErrorType = MutableStateFlow<SyncErrorType>(SyncErrorType.None)
    val syncErrorType: StateFlow<SyncErrorType> = _syncErrorType.asStateFlow()

    // --- Navigation backstack ---
    private val screenStack = mutableListOf<Screen>()

    init {
        // Automatically skip onboarding if there are pre-existing job applications:
        viewModelScope.launch {
            allApplications.first { it.isNotEmpty() }.let {
                if (_currentScreen.value == Screen.Onboarding) {
                    navigateTo(Screen.Today)
                }
            }
        }

        // Collect SQLite-persisted pending extractions into _gmailSyncResults
        viewModelScope.launch {
            repository.allPendingExtractions.collect { list ->
                val pairs = list.map { pending ->
                    val email = DemoEmail(
                        messageId = pending.messageId,
                        threadId = pending.threadId,
                        sender = pending.sender,
                        subject = pending.subject,
                        dateString = pending.dateString,
                        body = pending.bodyExcerpt,
                        snippet = pending.snippet
                    )
                    val result = JobExtractionResult(
                        isJobRelated = pending.isJobRelated,
                        confidence = pending.confidence,
                        eventType = pending.eventType,
                        companyName = pending.companyName,
                        jobTitle = pending.jobTitle,
                        applicationStatus = pending.applicationStatus,
                        eventDate = pending.eventDate,
                        deadline = pending.deadline,
                        recruiterName = pending.recruiterName,
                        recruiterEmail = pending.recruiterEmail,
                        source = pending.source ?: "Gmail",
                        summary = pending.summary,
                        nextAction = pending.nextAction,
                        followUpDate = pending.followUpDate
                    )
                    email to result
                }
                _gmailSyncResults.value = pairs
            }
        }

        // Initialize WorkManager Periodic sync if autoSync is active and credentials exist
        if (_autoSyncEnabled.value && !_gmailAccessToken.value.isNullOrEmpty()) {
            scheduleBackgroundSync()
        }
    }

    fun saveGmailToken(token: String?) {
        prefs.edit().putString("gmail_token", token).apply()
        _gmailAccessToken.value = token
        if (!token.isNullOrEmpty()) {
            scheduleBackgroundSync()
            scanGmail()
        } else {
            cancelBackgroundSync()
        }
    }

    fun clearGmailToken() {
        prefs.edit().remove("gmail_token").apply()
        _gmailAccessToken.value = null
        cancelBackgroundSync()
    }

    fun deleteGmailSyncHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllProcessedEmails()
            repository.deleteAllPendingExtractions()
            _lastGmailScan.value = 0L
            prefs.edit().putLong("last_gmail_scan", 0L).apply()
            withContext(Dispatchers.Main) {
                _syncError.value = "Your Google Mail scraping logs and hashes have been entirely cleared."
                _syncErrorType.value = SyncErrorType.None
            }
        }
    }

    fun deleteAllApplicationData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllData()
            withContext(Dispatchers.Main) {
                _gmailSyncResults.value = emptyList()
                _currentScreen.value = Screen.Onboarding
                screenStack.clear()
            }
        }
    }

    fun exportData(onExportReady: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = allApplications.value
            val sb = java.lang.StringBuilder()
            sb.append("[\n")
            apps.forEachIndexed { index, app ->
                sb.append("  {\n")
                sb.append("    \"company\": \"${app.companyName.replace("\"", "\\\"")}\",\n")
                sb.append("    \"role\": \"${app.jobTitle.replace("\"", "\\\"")}\",\n")
                sb.append("    \"status\": \"${app.currentStatus}\",\n")
                sb.append("    \"applied_date\": ${app.appliedDate},\n")
                sb.append("    \"source\": \"${app.source}\",\n")
                sb.append("    \"priority\": \"${app.priority}\"\n")
                sb.append("  }")
                if (index < apps.size - 1) sb.append(",")
                sb.append("\n")
            }
            sb.append("]")
            withContext(Dispatchers.Main) {
                onExportReady(sb.toString())
            }
        }
    }

    fun setAutoSync(enabled: Boolean) {
        prefs.edit().putBoolean("auto_sync", enabled).apply()
        _autoSyncEnabled.value = enabled
        if (enabled) {
            scheduleBackgroundSync()
        } else {
            cancelBackgroundSync()
        }
    }

    private fun scheduleBackgroundSync() {
        try {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()
            val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.data.sync.GmailSyncWorker>(
                15, java.util.concurrent.TimeUnit.MINUTES
            )
            .setConstraints(constraints)
            .build()

            androidx.work.WorkManager.getInstance(getApplication())
                .enqueueUniquePeriodicWork(
                    "GmailSyncWorker",
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule periodic WorkManager task", e)
        }
    }

    private fun cancelBackgroundSync() {
        try {
            androidx.work.WorkManager.getInstance(getApplication())
                .cancelUniqueWork("GmailSyncWorker")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel periodic WorkManager task", e)
        }
    }

    fun navigateTo(screen: Screen) {
        screenStack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeAt(screenStack.size - 1)
        } else {
            _currentScreen.value = Screen.Today
        }
    }

    // --- Core Operations ---
    
    fun addApplicationManually(
        companyName: String,
        jobTitle: String,
        location: String,
        source: String,
        status: String,
        priority: String,
        salaryRange: String,
        notes: String,
        appliedDate: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newApp = JobApplication(
                companyName = companyName.trim(),
                jobTitle = jobTitle.trim(),
                location = location.trim(),
                source = source.trim().ifEmpty { "Manual" },
                currentStatus = status,
                priority = priority,
                salaryRange = salaryRange.trim(),
                notes = notes.trim(),
                appliedDate = appliedDate,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val appId = repository.insertApplication(newApp).toInt()

            // Intersperse initial confirmation event
            val initialEvent = JobEvent(
                applicationId = appId,
                eventType = "application_confirmation",
                eventDate = appliedDate,
                summary = "Application added manually as '$status'.",
                extractedByAi = false,
                createdAt = System.currentTimeMillis()
            )
            repository.insertEvent(initialEvent)
        }
    }

    fun updateApplicationDetails(application: JobApplication) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateApplication(application.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteApplication(application: JobApplication) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteApplication(application)
            withContext(Dispatchers.Main) {
                navigateBack()
            }
        }
    }

    fun addTimelineEvent(appId: Int, type: String, summary: String, timestamp: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val event = JobEvent(
                applicationId = appId,
                eventType = type,
                eventDate = timestamp,
                summary = summary,
                extractedByAi = false,
                createdAt = System.currentTimeMillis()
            )
            repository.insertEvent(event)

            // Update app status based on direct manual milestone addition
            val app = repository.getApplicationById(appId)
            if (app != null) {
                val newStatus = mapEventTypeToStatus(type) ?: app.currentStatus
                repository.updateApplication(
                    app.copy(
                        currentStatus = newStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun getEventsForApplication(applicationId: Int): Flow<List<JobEvent>> {
        return repository.getEventsByApplicationFlow(applicationId)
    }

    fun deleteEventById(eventId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEventById(eventId)
        }
    }

    private fun updateLastGmailScanTimestamp() {
        val now = System.currentTimeMillis()
        prefs.edit().putLong("last_gmail_scan", now).apply()
        _lastGmailScan.value = now
    }

    // --- Progress tracking states for live and demo scans ---
    private val _scanProgressFound = MutableStateFlow(0)
    val scanProgressFound: StateFlow<Int> = _scanProgressFound.asStateFlow()

    private val _scanProgressSkipped = MutableStateFlow(0)
    val scanProgressSkipped: StateFlow<Int> = _scanProgressSkipped.asStateFlow()

    private val _scanProgressAnalyzed = MutableStateFlow(0)
    val scanProgressAnalyzed: StateFlow<Int> = _scanProgressAnalyzed.asStateFlow()

    private val _scanProgressDetected = MutableStateFlow(0)
    val scanProgressDetected: StateFlow<Int> = _scanProgressDetected.asStateFlow()

    private val _scanLimitReached = MutableStateFlow(false)
    val scanLimitReached: StateFlow<Boolean> = _scanLimitReached.asStateFlow()

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

    private fun createNeedsManualReviewResult(email: DemoEmail, reason: String = "AI Parse Failed"): JobExtractionResult {
        val fallback = generateLocalExtractionFallback(email)
        return fallback.copy(
            confidence = 0.5f,
            summary = "Needs manual review ($reason). Original subject: ${email.subject}",
            nextAction = "Verify and correct any missing company, role, or status details manually.",
            eventType = "follow_up_needed",
            applicationStatus = "Recruiter replied"
        )
    }

    // --- Gmail Syncer logic (Option A) ---

    fun scanGmail() {
        viewModelScope.launch {
            _syncingState.value = true
            _syncError.value = null
            _syncErrorType.value = SyncErrorType.None
            _gmailSyncResults.value = emptyList()

            // Reset progress counters
            _scanProgressFound.value = 0
            _scanProgressSkipped.value = 0
            _scanProgressAnalyzed.value = 0
            _scanProgressDetected.value = 0
            _scanLimitReached.value = false

            if (SecurePrefsManager.hasFailed) {
                _syncingState.value = false
                _syncError.value = "Security Error: Encrypted storage failed to initialize. Gmail integration is safely disabled."
                return@launch
            }

            val token = _gmailAccessToken.value
            if (token.isNullOrEmpty()) {
                _syncingState.value = false
                _syncErrorType.value = SyncErrorType.AuthFailure
                _syncError.value = "Gmail connection requires production OAuth setup."
                return@launch
            }

            var success = true
            try {
                var totalCharactersSent = 0
                val maxCharsPerScan = 100000 // cost budget cap limit

                if (token != null) {
                    val bearer = "Bearer $token"
                    val days = _scanDays.value
                    var query = "newer_than:${days}d (apply OR application OR interview OR offer OR career OR hiring OR assessment OR resume OR job)"
                    if (_includeSpamTrash.value) {
                        query += " in:anywhere"
                    }

                    val maxMsgToFetch = _maxMessagesToFetch.value
                    val maxToAnalyze = _maxEmailsToAnalyze.value

                    val listResponse = try {
                        withContext(Dispatchers.IO) {
                            GmailClient.service.listMessages(bearerToken = bearer, query = query, maxResults = maxMsgToFetch)
                        }
                    } catch (e: java.io.IOException) {
                        _syncErrorType.value = SyncErrorType.NoInternet
                        _syncError.value = "No internet connection detected. Please verify your internet link and try again."
                        CrashlyticsHelper.recordException(e)
                        return@launch
                    } catch (e: retrofit2.HttpException) {
                        if (e.code() == 401 || e.code() == 403) {
                            _syncErrorType.value = SyncErrorType.TokenExpired
                            _syncError.value = "Your authorized Gmail session has expired. Please disconnected and reconnect."
                        } else {
                            _syncErrorType.value = SyncErrorType.AuthFailure
                            _syncError.value = "Google Auth Connection error (${e.code()}): ${e.message()}"
                        }
                        CrashlyticsHelper.recordException(e)
                        return@launch
                    }

                    val messages = listResponse.messages ?: emptyList()
                    _scanProgressFound.value = messages.size

                    if (messages.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            _syncErrorType.value = SyncErrorType.EmptyInbox
                            _syncError.value = "Your inbox is clean and fully synced. No emails found matching the search criteria!"
                        }
                        return@launch
                    }

                    val unprocessed = messages.filter { !repository.isEmailProcessed(it.id) }
                    _scanProgressSkipped.value = messages.size - unprocessed.size

                    if (unprocessed.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            _syncErrorType.value = SyncErrorType.DuplicateJobUpdate
                            _syncError.value = "Duplicate update: All fetched emails in the specified range are already processed and active in your pipeline."
                        }
                        return@launch
                    }

                    val pendingResults = mutableListOf<Pair<DemoEmail, JobExtractionResult>>()

                    withContext(Dispatchers.IO) {
                        for (ref in unprocessed) {
                            // Check scan budget caps
                            if (_scanProgressAnalyzed.value >= maxToAnalyze) {
                                _scanLimitReached.value = true
                                _syncError.value = "Scan paused because your selected scan limit was reached."
                                break
                            }
                            if (totalCharactersSent >= maxCharsPerScan) {
                                _scanLimitReached.value = true
                                _syncError.value = "Scan paused because your selected scan limit was reached."
                                break
                            }

                            try {
                                val detail = GmailClient.service.getMessage(bearer, ref.id)
                                val email = GmailClient.mapToDemoEmail(detail)

                                // Apply local filter
                                val passesFilter = shouldAnalyzeLocalFilter(email.subject, email.snippet, email.body, _scanMode.value)
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
                                    _scanProgressSkipped.value += 1
                                    continue
                                }

                                // Trim email body to prevent excess token usage
                                val trimmedBody = if (email.body.length > 4000) email.body.substring(0, 4000) else email.body
                                totalCharactersSent += trimmedBody.length
                                _scanProgressAnalyzed.value += 1

                                val extractionResult = try {
                                    GeminiClient.extractJobDetails(trimmedBody)
                                } catch (e: Exception) {
                                    null
                                }

                                if (extractionResult != null) {
                                    if (extractionResult.isJobRelated) {
                                        pendingResults.add(email to extractionResult)
                                        _scanProgressDetected.value += 1
                                    } else {
                                        val logEmail = ProcessedEmail(
                                            gmailMessageId = email.messageId,
                                            threadId = email.threadId,
                                            subject = email.subject,
                                            sender = email.sender,
                                            dateString = email.dateString,
                                            snippet = email.snippet,
                                            classification = "irrelevant",
                                            confidence = extractionResult.confidence,
                                            linkedApplicationId = null
                                        )
                                        repository.insertProcessedEmail(logEmail)
                                        _scanProgressSkipped.value += 1
                                    }
                                } else {
                                    // Gemini failed or returned malformed content -> create deep client-friendly Needs Manual Reviewfallback
                                    val fallbackResult = createNeedsManualReviewResult(email, "Raw parsing failed")
                                    pendingResults.add(email to fallbackResult)
                                    _scanProgressDetected.value += 1
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to parse individual live email ${ref.id}", e)
                                CrashlyticsHelper.recordException(e)
                            }
                        }
                    }

                    if (pendingResults.isNotEmpty()) {
                        // Persist these to SQLite so that AI Inbox reviews are non-volatile
                        withContext(Dispatchers.IO) {
                            pendingResults.forEach { (email, result) ->
                                val pendingEntity = PendingAiExtraction(
                                    messageId = email.messageId,
                                    threadId = email.threadId,
                                    sender = email.sender,
                                    subject = email.subject,
                                    dateString = email.dateString,
                                    bodyExcerpt = email.body.take(1000),
                                    snippet = email.snippet,
                                    isJobRelated = result.isJobRelated,
                                    confidence = result.confidence,
                                    eventType = result.eventType,
                                    companyName = result.companyName,
                                    jobTitle = result.jobTitle,
                                    applicationStatus = result.applicationStatus,
                                    eventDate = result.eventDate,
                                    deadline = result.deadline,
                                    recruiterName = result.recruiterName,
                                    recruiterEmail = result.recruiterEmail,
                                    source = result.source ?: "Gmail",
                                    summary = result.summary,
                                    nextAction = result.nextAction,
                                    followUpDate = result.followUpDate
                                )
                                repository.insertPendingExtraction(pendingEntity)
                            }
                        }
                        navigateTo(Screen.AiInbox)
                    }
                }
            } catch (e: Exception) {
                success = false
                Log.e(TAG, "Sync process failed", e)
                CrashlyticsHelper.recordException(e)
                _syncError.value = "Scraper error: ${e.message}"
            } finally {
                if (success) {
                    updateLastGmailScanTimestamp()
                }
                _syncingState.value = false
            }
        }
    }

    private suspend fun autoScanAndInjectLiveGmail(token: String) {
        val bearer = "Bearer $token"
        try {
            val listResponse = withContext(Dispatchers.IO) {
                GmailClient.service.listMessages(bearerToken = bearer, query = "newer_than:1d", maxResults = 5)
            }
            val messages = listResponse.messages ?: emptyList()
            val unprocessed = messages.filter { !repository.isEmailProcessed(it.id) }
            for (ref in unprocessed) {
                val detail = withContext(Dispatchers.IO) { GmailClient.service.getMessage(bearer, ref.id) }
                val email = GmailClient.mapToDemoEmail(detail)
                val extraction = withContext(Dispatchers.IO) { GeminiClient.extractJobDetails(email.body) } ?: generateLocalExtractionFallback(email)

                if (extraction.isJobRelated && extraction.confidence >= 0.82f) {
                    // Redirect detected live emails into the AI review queue first (No auto-injection)
                    val currentList = _gmailSyncResults.value.toMutableList()
                    if (currentList.none { it.first.messageId == email.messageId }) {
                        currentList.add(email to extraction)
                        _gmailSyncResults.value = currentList
                    }
                } else {
                    // Mark as processed (either irrelevant or ignored)
                    val proceEmail = ProcessedEmail(
                        gmailMessageId = email.messageId,
                        threadId = email.threadId,
                        sender = email.sender,
                        subject = email.subject,
                        dateString = email.dateString,
                        snippet = email.snippet,
                        classification = if (extraction.isJobRelated) "low_confidence_skipped" else "irrelevant",
                        confidence = extraction.confidence,
                        linkedApplicationId = null
                    )
                    withContext(Dispatchers.IO) {
                        repository.insertProcessedEmail(proceEmail)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BackgroundScanner", "Periodic live back-sync error: ${e.message}")
        }
    }

    private suspend fun injectExtractedJobIntoDatabase(email: DemoEmail, result: JobExtractionResult) {
        withContext(Dispatchers.IO) {
            val company = result.companyName ?: "Unknown Company"
            val title = result.jobTitle ?: "Unknown Role"
            val apps = repository.allApplications.first()
            val matchedApp = apps.find {
                it.companyName.equals(company, ignoreCase = true) &&
                        it.jobTitle.equals(title, ignoreCase = true)
            }

            val targetAppId: Int
            if (matchedApp != null) {
                targetAppId = matchedApp.id
                val updatedApp = matchedApp.copy(
                    currentStatus = result.applicationStatus ?: matchedApp.currentStatus,
                    notes = if (result.nextAction != null) {
                        "${matchedApp.notes}\n\n[Auto Background Sync] Next action: ${result.nextAction}".trim()
                    } else matchedApp.notes,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateApplication(updatedApp)
            } else {
                val parsedDate = parseDateString(result.eventDate) ?: System.currentTimeMillis()
                val newApp = JobApplication(
                    companyName = company,
                    jobTitle = title,
                    currentStatus = result.applicationStatus ?: "Applied",
                    source = "Live Gmail Event",
                    appliedDate = parsedDate,
                    notes = result.summary ?: "Extracted in real-time background sync.",
                    nextAction = result.nextAction ?: "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                targetAppId = repository.insertApplication(newApp).toInt()
            }

            val parsedEventDate = parseDateString(result.eventDate) ?: System.currentTimeMillis()
            val newEvent = JobEvent(
                applicationId = targetAppId,
                eventType = result.eventType,
                eventDate = parsedEventDate,
                summary = result.summary ?: "Live background email trigger.",
                extractedByAi = true,
                confidence = result.confidence,
                rawSnippet = email.snippet,
                createdAt = System.currentTimeMillis()
            )
            repository.insertEvent(newEvent)

            val proceEmail = ProcessedEmail(
                gmailMessageId = email.messageId,
                threadId = email.threadId,
                subject = email.subject,
                sender = email.sender,
                dateString = email.dateString,
                snippet = email.snippet,
                classification = result.eventType,
                confidence = result.confidence,
                linkedApplicationId = targetAppId
            )
            repository.insertProcessedEmail(proceEmail)

            Log.d("BackgroundScanner", "Successfully auto-synced & injected email event for $company ($title)")
        }
    }

    // Custom Paste Analyzer
    fun analyzePastedEmailContent(emailBody: String, onComplete: (JobExtractionResult?) -> Unit) {
        viewModelScope.launch {
            _syncingState.value = true
            val parsedResult = withContext(Dispatchers.IO) {
                GeminiClient.extractJobDetails(emailBody)
            }
            if (parsedResult != null) {
                onComplete(parsedResult)
            } else {
                // local fallback if offline
                onComplete(generateLocalFallbackForText(emailBody))
            }
            _syncingState.value = false
        }
    }

    // Review Actions

    fun confirmSyncResult(email: DemoEmail, result: JobExtractionResult) {
        viewModelScope.launch(Dispatchers.IO) {
            val company = result.companyName ?: "Unknown Company"
            val title = result.jobTitle ?: "Unknown Role"

            // Look up existing application for this company and title
            val apps = allApplications.value
            val matchedApp = apps.find {
                it.companyName.equals(company, ignoreCase = true) &&
                        it.jobTitle.equals(title, ignoreCase = true)
            }

            val targetAppId: Int
            if (matchedApp != null) {
                targetAppId = matchedApp.id
                // Update existing application
                val updatedApp = matchedApp.copy(
                    currentStatus = result.applicationStatus ?: matchedApp.currentStatus,
                    notes = if (result.nextAction != null) {
                        "${matchedApp.notes}\n\n[Auto Update] Next action: ${result.nextAction}".trim()
                    } else matchedApp.notes,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateApplication(updatedApp)
            } else {
                // Create a completely new application
                val parsedDate = parseDateString(result.eventDate) ?: System.currentTimeMillis()
                val newApp = JobApplication(
                    companyName = company,
                    jobTitle = title,
                    currentStatus = result.applicationStatus ?: "Applied",
                    source = "Gmail Sync",
                    appliedDate = parsedDate,
                    notes = result.summary ?: "Extracted via Gemini AI parser.",
                    nextAction = result.nextAction ?: "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                targetAppId = repository.insertApplication(newApp).toInt()
            }

            // Insert matching timeline Event record
            val parsedEventDate = parseDateString(result.eventDate) ?: System.currentTimeMillis()
            val newEvent = JobEvent(
                applicationId = targetAppId,
                eventType = result.eventType,
                eventDate = parsedEventDate,
                summary = result.summary ?: "Discovered via email scan.",
                extractedByAi = true,
                confidence = result.confidence,
                rawSnippet = email.snippet,
                createdAt = System.currentTimeMillis()
            )
            repository.insertEvent(newEvent)

            // Save processing metadata in processed_emails table
            val proceEmail = ProcessedEmail(
                gmailMessageId = email.messageId,
                threadId = email.threadId,
                subject = email.subject,
                sender = email.sender,
                dateString = email.dateString,
                snippet = email.snippet,
                classification = result.eventType,
                confidence = result.confidence,
                linkedApplicationId = targetAppId
            )
            repository.insertProcessedEmail(proceEmail)
            repository.deletePendingExtractionById(email.messageId)

            // Remove from local review state block
            withContext(Dispatchers.Main) {
                _gmailSyncResults.value = _gmailSyncResults.value.filter { it.first.messageId != email.messageId }
                if (_gmailSyncResults.value.isEmpty()) {
                    navigateBack()
                }
            }
        }
    }

    fun ignoreSyncResult(email: DemoEmail) {
        viewModelScope.launch(Dispatchers.IO) {
            val proceEmail = ProcessedEmail(
                gmailMessageId = email.messageId,
                threadId = email.threadId,
                subject = email.subject,
                sender = email.sender,
                dateString = email.dateString,
                snippet = email.snippet,
                classification = "ignored",
                confidence = 1.0f,
                linkedApplicationId = null
            )
            repository.insertProcessedEmail(proceEmail)
            repository.deletePendingExtractionById(email.messageId)

            withContext(Dispatchers.Main) {
                _gmailSyncResults.value = _gmailSyncResults.value.filter { it.first.messageId != email.messageId }
                if (_gmailSyncResults.value.isEmpty()) {
                    navigateBack()
                }
            }
        }
    }

    // --- Helper Fallbacks (To guarantee robust execution if API Keys are not present) ---

    private fun generateLocalExtractionFallback(email: DemoEmail): JobExtractionResult {
        val lowerSub = email.subject.lowercase()
        val lowerBody = email.body.lowercase()

        val isJob = when {
            lowerSub.contains("apply") || lowerSub.contains("recruiting") || lowerSub.contains("interview") || 
            lowerSub.contains("offer") || lowerSub.contains("careers") || lowerSub.contains("application") ||
            lowerBody.contains("hiring") -> true
            else -> false
        }

        if (!isJob) {
            return JobExtractionResult(isJobRelated = false, confidence = 0.95f, eventType = "irrelevant")
        }

        val company = when {
            lowerSub.contains("google") || lowerBody.contains("google") -> "Google"
            lowerSub.contains("stripe") || lowerBody.contains("stripe") -> "Stripe"
            lowerSub.contains("meta") || lowerBody.contains("meta") -> "Meta"
            lowerSub.contains("apple") || lowerBody.contains("apple") -> "Apple"
            lowerSub.contains("netflix") || lowerBody.contains("netflix") -> "Netflix"
            else -> "Hiring Startup"
        }

        val role = when {
            lowerBody.contains("pm") || lowerSub.contains("product manager") -> "Product Manager"
            lowerBody.contains("ux") || lowerSub.contains("ux architect") -> "UX Architect"
            lowerBody.contains("mobile") || lowerBody.contains("android") -> "Software Engineer (Mobile)"
            else -> "Software Engineer"
        }

        val (type, status, summary, action) = when {
            lowerSub.contains("interview") || lowerBody.contains("interview") -> 
                listOf("interview_invitation", "Interview", "Invited to technical screening video call panel.", "Join the scheduled meeting panel on time.")
            lowerBody.contains("assessment") || lowerSub.contains("assessment") -> 
                listOf("assessment_invitation", "Assessment", "Online technical assessment received.", "Complete algorithmic challenge.")
            lowerSub.contains("offer") || lowerBody.contains("offer") -> 
                listOf("offer", "Offer", "Received job employment offer details!", "Read over contract conditions.")
            lowerSub.contains("unfortunately") || lowerBody.contains("unfortunately") || lowerSub.contains("careful consideration") -> 
                listOf("rejection", "Rejected", "Application decision update: not proceeding.", "Keep updating profile and apply to other open paths.")
            else -> 
                listOf("application_confirmation", "Applied", "We have received your resume materials.", "Audit the active pipeline dashboard logs.")
        }

        return JobExtractionResult(
            isJobRelated = true,
            confidence = 0.90f,
            eventType = type,
            companyName = company,
            jobTitle = role,
            applicationStatus = status,
            summary = summary,
            nextAction = action,
            source = "Gmail",
            eventDate = "2026-06-10"
        )
    }

    private fun generateLocalFallbackForText(body: String): JobExtractionResult {
        val email = DemoEmail(
            messageId = "pasted_" + System.currentTimeMillis(),
            threadId = "thread_" + System.currentTimeMillis(),
            sender = "Pasted",
            subject = "Pasted Job Email Analysis",
            dateString = "Now",
            body = body,
            snippet = body.take(80)
        )
        return generateLocalExtractionFallback(email)
    }

    private fun mapEventTypeToStatus(type: String): String? {
        return when (type) {
            "application_confirmation" -> "Applied"
            "recruiter_reply" -> "Recruiter replied"
            "assessment_invitation" -> "Assessment"
            "interview_invitation", "interview_reschedule" -> "Interview"
            "offer" -> "Offer"
            "rejection" -> "Rejected"
            "follow_up_needed" -> "Applied"
            else -> null
        }
    }

    private fun parseDateString(dateStr: String?): Long? {
        if (dateStr.isNullOrEmpty()) return null
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }
}

class JobTrackerViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobTrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JobTrackerViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
