package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {

    // --- Job Application Queries ---
    @Query("SELECT * FROM job_applications ORDER BY appliedDate DESC")
    fun getAllApplicationsFlow(): Flow<List<JobApplication>>

    @Query("SELECT * FROM job_applications WHERE id = :id")
    suspend fun getApplicationById(id: Int): JobApplication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: JobApplication): Long

    @Update
    suspend fun updateApplication(application: JobApplication)

    @Delete
    suspend fun deleteApplication(application: JobApplication)

    // --- Job Event Queries ---
    @Query("SELECT * FROM job_events WHERE applicationId = :applicationId ORDER BY eventDate DESC")
    fun getEventsByApplicationFlow(applicationId: Int): Flow<List<JobEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: JobEvent): Long

    @Query("DELETE FROM job_events WHERE applicationId = :applicationId")
    suspend fun deleteEventsForApplication(applicationId: Int)

    @Query("DELETE FROM job_events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: Int)

    // --- Processed Email Queries ---
    @Query("SELECT * FROM processed_emails ORDER BY processedAt DESC")
    fun getAllProcessedEmailsFlow(): Flow<List<ProcessedEmail>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProcessedEmail(email: ProcessedEmail): Long

    @Query("SELECT EXISTS(SELECT 1 FROM processed_emails WHERE gmailMessageId = :messageId)")
    suspend fun isEmailProcessed(messageId: String): Boolean

    @Query("DELETE FROM processed_emails")
    suspend fun deleteAllProcessedEmails()
}
