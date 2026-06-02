package com.example.data.repository

import com.example.data.local.JobApplication
import com.example.data.local.JobDao
import com.example.data.local.JobEvent
import com.example.data.local.ProcessedEmail
import kotlinx.coroutines.flow.Flow

class JobRepository(private val jobDao: JobDao) {

    val allApplications: Flow<List<JobApplication>> = jobDao.getAllApplicationsFlow()
    val allProcessedEmails: Flow<List<ProcessedEmail>> = jobDao.getAllProcessedEmailsFlow()

    suspend fun getApplicationById(id: Int): JobApplication? {
        return jobDao.getApplicationById(id)
    }

    suspend fun insertApplication(application: JobApplication): Long {
        return jobDao.insertApplication(application)
    }

    suspend fun updateApplication(application: JobApplication) {
        jobDao.updateApplication(application)
    }

    suspend fun deleteApplication(application: JobApplication) {
        jobDao.deleteEventsForApplication(application.id)
        jobDao.deleteApplication(application)
    }

    fun getEventsByApplicationFlow(applicationId: Int): Flow<List<JobEvent>> {
        return jobDao.getEventsByApplicationFlow(applicationId)
    }

    suspend fun insertEvent(event: JobEvent): Long {
        return jobDao.insertEvent(event)
    }

    suspend fun deleteEventById(eventId: Int) {
        jobDao.deleteEventById(eventId)
    }

    suspend fun insertProcessedEmail(email: ProcessedEmail): Long {
        return jobDao.insertProcessedEmail(email)
    }

    suspend fun isEmailProcessed(messageId: String): Boolean {
        return jobDao.isEmailProcessed(messageId)
    }

    suspend fun deleteAllProcessedEmails() {
        jobDao.deleteAllProcessedEmails()
    }
}
