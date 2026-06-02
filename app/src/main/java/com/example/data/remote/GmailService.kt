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


