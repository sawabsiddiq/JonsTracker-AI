package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JobExtractionResult(
    @Json(name = "is_job_related") val isJobRelated: Boolean,
    val confidence: Float,
    @Json(name = "event_type") val eventType: String, // application_confirmation, recruiter_reply, etc.
    @Json(name = "company_name") val companyName: String? = null,
    @Json(name = "job_title") val jobTitle: String? = null,
    @Json(name = "application_status") val applicationStatus: String? = null, // Saved, Applied, Recruiter replied, Assessment, Interview, Offer, Rejected, Ghosted
    @Json(name = "event_date") val eventDate: String? = null, // "YYYY-MM-DD"
    val deadline: String? = null,
    @Json(name = "recruiter_name") val recruiterName: String? = null,
    @Json(name = "recruiter_email") val recruiterEmail: String? = null,
    val source: String = "Gmail",
    val summary: String? = null,
    @Json(name = "next_action") val nextAction: String? = null,
    @Json(name = "follow_up_date") val followUpDate: String? = null
)

// --- Gemini REST Data Classes ---

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)
