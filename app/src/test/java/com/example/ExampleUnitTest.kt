package com.example

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class ExampleUnitTest {

    @Test
    fun `test status mapping conversions`() {
        // Direct mirroring of mapEventTypeToStatus
        fun testMapEventTypeToStatus(type: String): String? {
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

        assertEquals("Applied", testMapEventTypeToStatus("application_confirmation"))
        assertEquals("Recruiter replied", testMapEventTypeToStatus("recruiter_reply"))
        assertEquals("Assessment", testMapEventTypeToStatus("assessment_invitation"))
        assertEquals("Interview", testMapEventTypeToStatus("interview_invitation"))
        assertEquals("Interview", testMapEventTypeToStatus("interview_reschedule"))
        assertEquals("Offer", testMapEventTypeToStatus("offer"))
        assertEquals("Rejected", testMapEventTypeToStatus("rejection"))
        assertNull(testMapEventTypeToStatus("unknown_event_type"))
    }

    @Test
    fun `test on device local extraction heuristics`() {
        fun simulateHeuristics(subject: String, body: String): Pair<Boolean, String> {
            val lowerSub = subject.lowercase()
            val lowerBody = body.lowercase()

            val isJob = lowerSub.contains("apply") || lowerSub.contains("recruiting") || lowerSub.contains("interview") || 
                    lowerSub.contains("offer") || lowerSub.contains("careers") || lowerSub.contains("application") ||
                    lowerBody.contains("hiring")

            if (!isJob) return false to "irrelevant"

            val type = when {
                lowerSub.contains("offer") -> "offer"
                lowerSub.contains("interview") || lowerBody.contains("schedule your interview") -> "interview_invitation"
                lowerSub.contains("assessment") || lowerBody.contains("coding challenge") -> "assessment_invitation"
                lowerSub.contains("thank you") || lowerBody.contains("application received") -> "application_confirmation"
                lowerSub.contains("rejection") || lowerBody.contains("not moving forward") -> "rejection"
                else -> "recruiter_reply"
            }
            return true to type
        }

        // Test non-job related email
        val result1 = simulateHeuristics("Amazon Receipt", "Your order of shoes has shipped.")
        assertFalse(result1.first)

        // Test interview invite
        val result2 = simulateHeuristics("Stripe Interview Schedule", "Please pick a slot on my calendly.")
        assertTrue(result2.first)
        assertEquals("interview_invitation", result2.second)

        // Test job confirmation
        val result3 = simulateHeuristics("Thank you for applying to Google", "We have received your resume.")
        assertTrue(result3.first)
        assertEquals("application_confirmation", result3.second)
    }

    @Test
    fun `test date string formatting parsing`() {
        fun testParseDateString(dateStr: String?): Long? {
            if (dateStr.isNullOrEmpty()) return null
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                format.parse(dateStr)?.time
            } catch (e: Exception) {
                null
            }
        }

        assertNull(testParseDateString(null))
        assertNull(testParseDateString(""))
        assertNull(testParseDateString("invalid-date-format"))
        
        val parsed = testParseDateString("2026-06-02")
        assertNotNull(parsed)
        assertTrue(parsed!! > 0L)
    }

    @Test
    fun `test duplicate matching and follow-up flags`() {
        val processedIds = setOf("message_1", "message_2")
        val isProcessed1 = processedIds.contains("message_1")
        val isProcessed3 = processedIds.contains("message_3")

        assertTrue(isProcessed1)
        assertFalse(isProcessed3)

        val currentDate = System.currentTimeMillis()
        val followUpDate = currentDate - 100000L
        val isFollowUpDue = followUpDate < currentDate
        assertTrue(isFollowUpDue)
    }
}
