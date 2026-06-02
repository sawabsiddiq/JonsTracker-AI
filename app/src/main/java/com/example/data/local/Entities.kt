package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "job_applications")
data class JobApplication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val companyName: String,
    val jobTitle: String,
    val location: String = "",
    val source: String = "Manual",
    val jobUrl: String = "",
    val appliedDate: Long = System.currentTimeMillis(),
    val currentStatus: String = "Applied", // Saved, Applied, Recruiter replied, Assessment, Interview, Offer, Rejected, Ghosted
    val priority: String = "Medium", // High, Medium, Low
    val salaryRange: String = "",
    val notes: String = "",
    val nextAction: String = "",
    val followUpDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "job_events")
data class JobEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val applicationId: Int,
    val eventType: String, // application_confirmation, recruiter_reply, assessment_invitation, interview_invitation, interview_reschedule, offer, rejection, follow_up_needed, job_alert, irrelevant, unknown
    val eventDate: Long,
    val summary: String,
    val extractedByAi: Boolean = false,
    val confidence: Float = 1.0f,
    val rawSnippet: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "processed_emails")
data class ProcessedEmail(
    @PrimaryKey val gmailMessageId: String,
    val threadId: String,
    val processedAt: Long = System.currentTimeMillis(),
    val subject: String,
    val sender: String,
    val dateString: String,
    val snippet: String,
    val classification: String,
    val confidence: Float,
    val linkedApplicationId: Int? = null
)

@Entity(tableName = "pending_ai_extractions")
data class PendingAiExtraction(
    @PrimaryKey val messageId: String,
    val threadId: String,
    val sender: String,
    val subject: String,
    val dateString: String,
    val body: String,
    val snippet: String,
    val isJobRelated: Boolean,
    val confidence: Float,
    val eventType: String,
    val companyName: String? = null,
    val jobTitle: String? = null,
    val applicationStatus: String? = null,
    val eventDate: String? = null,
    val deadline: String? = null,
    val recruiterName: String? = null,
    val recruiterEmail: String? = null,
    val source: String = "Gmail",
    val summary: String? = null,
    val nextAction: String? = null,
    val followUpDate: String? = null
)
