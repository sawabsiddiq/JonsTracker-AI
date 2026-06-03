package com.example.data.remote

import android.util.Log
import com.jobtrackai.app.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    suspend fun extractJobDetails(emailBody: String): JobExtractionResult? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured or in placeholder state.")
            return null
        }

        val prompt = """
            Extract job application details and milestone events from the email text below.
            
            Email Content:
            ----
            $emailBody
            ----
            
            Analyze whether this is job-related and output a structured JSON response.
            You MUST respond in the following JSON format ONLY. 
            Do not include markdown triple-ticks, "```json", or any extra text.
            If the company name or role is not explicitly mentioned, try to infer them or leave them null if impossible.
            
            Target Schema:
            {
              "is_job_related": true/false (Set to true if this email relates to a job application confirmation, recruiter reply, assessment link, interview call, offer, rejection, or job update),
              "confidence": 0.95 (Estimated float accuracy from 0.0 to 1.0),
              "event_type": "one_of_the_allowed_types" (Must be EXACTLY one of: "application_confirmation", "recruiter_reply", "assessment_invitation", "interview_invitation", "interview_reschedule", "offer", "rejection", "follow_up_needed", "job_alert", "irrelevant", "unknown"),
              "company_name": "Name of company" (or null if not found),
              "job_title": "Job title or role" (or null if not found),
              "application_status": "one_of_the_allowed_stages" (Must be EXACTLY one of: "Applied", "Recruiter replied", "Assessment", "Interview", "Offer", "Rejected", "Ghosted" - matching the state of the application after this email),
              "event_date": "YYYY-MM-DD" (If an interview/assessment date is scheduled, report its date. Otherwise, report null),
              "deadline": "YYYY-MM-DD" (Deadline for completing assessment or task, or null if none),
              "recruiter_name": "Name of recruiter" (or null if none),
              "recruiter_email": "Recruiter email address" (or null if none),
              "source": "Gmail",
              "summary": "Short 1-sentence description. Examples: 'We received your application for Software Developer', 'Invited to 45m technical panel interview'",
              "next_action": "Recommended next step action. Examples: 'Complete HackerRank assessment', 'Await recruiter interview schedule call'",
              "follow_up_date": "YYYY-MM-DD" (Recommend a date to follow up on this application, or null if none)
            }
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = "You are an ATS parser. Return clean JSON only. Do not wrap in markdown or block codes."))
            )
        )

        return try {
            val response = api.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                Log.d(TAG, "Raw returned JSON: $jsonText")
                // Clean markdown wrapping if model accidentally outputs it
                val cleanJson = jsonText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
                
                val adapter = moshi.adapter(JobExtractionResult::class.java)
                adapter.fromJson(cleanJson)
            } else {
                Log.e(TAG, "No response candidate content received.")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing job details via Gemini REST API", e)
            null
        }
    }
}
