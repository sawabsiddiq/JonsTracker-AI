package com.example.data.remote

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

// --- Gmail REST API Models ---

@JsonClass(generateAdapter = true)
data class GmailListResponse(
    val messages: List<GmailMessageRef>? = null,
    val nextPageToken: String? = null,
    val resultSizeEstimate: Int? = null
)

@JsonClass(generateAdapter = true)
data class GmailMessageRef(
    val id: String,
    val threadId: String
)

@JsonClass(generateAdapter = true)
data class GmailMessageDetail(
    val id: String,
    val threadId: String,
    val snippet: String,
    val payload: GmailPayload? = null,
    val internalDate: String? = null
)

@JsonClass(generateAdapter = true)
data class GmailPayload(
    val partId: String? = null,
    val mimeType: String? = null,
    val filename: String? = null,
    val headers: List<GmailHeader>? = null,
    val body: GmailBody? = null,
    val parts: List<GmailPayload>? = null
)

@JsonClass(generateAdapter = true)
data class GmailHeader(
    val name: String,
    val value: String
)

@JsonClass(generateAdapter = true)
data class GmailBody(
    val size: Int? = null,
    val data: String? = null // Base64url encoded body
)

// --- Gmail Retrofit Service API ---

interface GmailApi {
    @GET("gmail/v1/users/me/messages")
    suspend fun listMessages(
        @Header("Authorization") bearerToken: String,
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null
    ): GmailListResponse

    @GET("gmail/v1/users/me/messages/{id}")
    suspend fun getMessage(
        @Header("Authorization") bearerToken: String,
        @Path("id") messageId: String,
        @Query("format") format: String = "full"
    ): GmailMessageDetail
}

object GmailClient {
    private const val BASE_URL = "https://gmail.googleapis.com/"

    val service: GmailApi by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GmailApi::class.java)
    }

    fun extractHeader(headers: List<com.example.data.remote.GmailHeader>?, name: String): String {
        return headers?.find { it.name.equals(name, ignoreCase = true) }?.value ?: ""
    }

    fun extractEmailBody(payload: com.example.data.remote.GmailPayload?): String {
        if (payload == null) return ""
        val bodyData = payload.body?.data
        if (!bodyData.isNullOrEmpty()) {
            return try {
                val decoded = android.util.Base64.decode(bodyData, android.util.Base64.URL_SAFE)
                String(decoded, Charsets.UTF_8)
            } catch (e: java.lang.Exception) {
                ""
            }
        }
        payload.parts?.forEach { part ->
            val text = extractEmailBody(part)
            if (text.isNotEmpty()) {
                return text
            }
        }
        return ""
    }

    fun mapToDemoEmail(detail: com.example.data.remote.GmailMessageDetail): DemoEmail {
        val headers = detail.payload?.headers
        val sender = extractHeader(headers, "From")
        val subject = extractHeader(headers, "Subject")
        val dateString = extractHeader(headers, "Date")
        var body = extractEmailBody(detail.payload)
        if (body.isEmpty()) {
            body = detail.snippet
        }
        return DemoEmail(
            messageId = detail.id,
            threadId = detail.threadId,
            sender = sender.ifEmpty { "Unknown" },
            subject = subject.ifEmpty { "No Subject" },
            dateString = dateString.ifEmpty { "Unknown Date" },
            body = body,
            snippet = detail.snippet
        )
    }
}

// --- Demo Scenarios & Emulated Job Emails ---

data class DemoEmail(
    val messageId: String,
    val threadId: String,
    val sender: String,
    val subject: String,
    val dateString: String,
    val body: String,
    val snippet: String
)

object GmailDemoData {
    val presetEmails = listOf(
        DemoEmail(
            messageId = "msg_goog_001",
            threadId = "th_goog_001",
            sender = "Google Careers <noreply-careers@google.com>",
            subject = "Thank you for applying to Google: Software Engineer, Mobile",
            dateString = "Mon, 1 Jun 2026 09:12:00 +0000",
            body = """
                Hi Candidate,
                
                Thank you for applying for the Software Engineer, Mobile (Android), Room & Jetpack Compose position at Google (London, UK). We have received your application materials.
                
                Our talent acquisition team is actively reviewing submissions. If your profile is a match for the team's needs, a recruiter will reach out with next steps.
                
                You can monitor your candidate profile dashboard at any time for status updates.
                
                Best regards,
                Google Candidates Relations
            """.trimIndent(),
            snippet = "Thank you for applying for Software Engineer, Mobile. We have received your application materials..."
        ),
        DemoEmail(
            messageId = "msg_strp_002",
            threadId = "th_strp_002",
            sender = "Stripe Recruiting <talent@stripe.com>",
            subject = "Next Steps: Online Technical Assessment invitation from Stripe",
            dateString = "Tue, 26 May 2026 14:35:10 +0000",
            body = """
                Hello,
                
                We're excited to learn more about your engineering background! The next step in our process is a 90-minute online coding assessment through HackerRank.
                
                Position: Software Engineer, Core Payments SDK
                Link: https://hackerrank.com/stripe-payments-test-9921-xyz
                Deadline: Please complete this assessment by June 10, 2026.
                
                The assessment contains algorithmic and coding questions. If you have accessibility questions or need accommodations, please reply directly.
                
                Good luck!
                Stripe Recruiting Team
            """.trimIndent(),
            snippet = "The next step in our process is a online coding assessment. Please complete this by June 10..."
        ),
        DemoEmail(
            messageId = "msg_meta_003",
            threadId = "th_meta_003",
            sender = "Elena Vance <evance@meta.com>",
            subject = "Scheduling: Product Engineer Interview - Meta",
            dateString = "Wed, 27 May 2026 18:02:40 +0000",
            body = """
                Hi there,
                
                I'm Elena, a coordinator at Meta. The team was impressed by your profile, and we would love to schedule a 45-minute live technical screen via BlueJeans!
                
                We have scheduled your coding panel interview for Monday, June 10, 2026 at 2:00 PM BST.
                
                Please ensure you have a working camera, a stable internet connection, and your favorite IDE configured before the session.
                
                Click this link to RSVP: https://meta.zoom.us/j/4491023812
                
                Best,
                Elena Vance
                Meta Recruitment
            """.trimIndent(),
            snippet = "The team was impressed by your profile. We have scheduled your interview for June 10..."
        ),
        DemoEmail(
            messageId = "msg_aple_004",
            threadId = "th_aple_004",
            sender = "Apple Offers <offers-mgr@apple.com>",
            subject = "Congratulations! Offer of Employment from Apple",
            dateString = "Fri, 29 May 2026 17:15:00 +0000",
            body = """
                Dear Candidate,
                
                On behalf of Apple, we are thrilled to extend an official offer of employment for the position of Senior PM, Special Projects.
                
                Details:
                Role: Senior Product Manager
                Salary range: $145,000 - $160,000 USD
                Start date: July 1, 2026
                
                Your onboarding documentation will be emailed to you separately in DocuSign. We look forward to welcome you to Cupertino!
                
                Sincerely,
                Apple People Team
            """.trimIndent(),
            snippet = "We are thrilled to extend an official offer of employment for the position of Senior PM..."
        ),
        DemoEmail(
            messageId = "msg_netf_005",
            threadId = "th_netf_005",
            sender = "Netflix Careers <talent-acquisition@netflix.com>",
            subject = "Update on your Netflix application",
            dateString = "Thu, 28 May 2026 11:40:12 +0000",
            body = """
                Hello,
                
                Thank you for taking the time to apply and interview for the Senior UX Architect role at Netflix. 
                
                Unfortunately, after careful consideration, we have decided not to move forward with your application at this time. We had many qualified candidates, and this was an extremely difficult choice.
                
                We will retain your resume in our database and contact you if another position matching your qualifications opens up in the future.
                
                We wish you the absolute best in your career pursuits.
                
                Netflix Talent Team
            """.trimIndent(),
            snippet = "Unfortunately, after careful consideration, we have decided not to move forward at this time..."
        ),
        DemoEmail(
            messageId = "msg_amzn_006",
            threadId = "th_amzn_006",
            sender = "Amazon Digital Receipt <shipment-updates@amazon.com>",
            subject = "Your Amazon.com order #114-1920-88192 Has Shipped!",
            dateString = "Sun, 31 May 2026 16:21:05 +0000",
            body = """
                Hi sawabsiddiq@gmail.com,
                
                Great news! Your Amazon purchase containing:
                - 1x USB-C Multiport Dock Adapter ($24.99)
                - 2x Ergonomic Desk Cushion ($18.50)
                
                has been shipped from our package center in Bristol. It is scheduled to arrive at your address tomorrow evening. 
                
                Tracks: https://amazon.co.uk/track/order/114-1920-88192
                
                Thank you for shopping with Amazon!
            """.trimIndent(),
            snippet = "Great news! Your Amazon purchase containing USB-C adapter has shipped and will arrive..."
        ),
        DemoEmail(
            messageId = "msg_unkn_007",
            threadId = "th_unkn_007",
            sender = "Recruiting Coordinator <coordinator@startuptech.io>",
            subject = "Quick question regarding your resume",
            dateString = "Mon, 01 Jun 2026 10:45:00 +0000",
            body = """
                Hello,
                
                Thanks for applying to StartupTech. I'm a coordinator here.
                
                I looked at your resume and noticed you didn't list your work authorization status. Could you reply and let me know if you will require sponsorship in the future?
                
                Thanks!
                StartupTech Hiring Coordinator
            """.trimIndent(),
            snippet = "Could you reply and let me know if you will require sponsorship in the future?..."
        )
    )
}
