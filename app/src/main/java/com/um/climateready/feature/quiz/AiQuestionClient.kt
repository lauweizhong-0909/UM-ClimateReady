package com.um.climateready.feature.quiz

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiQuestionClient(
    private val apiKey: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .callTimeout(8, TimeUnit.SECONDS)
        .build()

    fun generateQuestion(
        scenario: String,
        model: String = "gemini-2.5-flash"
    ): String? {
        if (apiKey.isBlank()) return null

        val prompt = """
            Create one multiple-choice climate preparedness
            question about this scenario: $scenario

            Return ONLY a JSON object with:
            "question": non-empty question text
            "options": object with A, B, C, D
            "correctAnswer": one of A, B, C, D
            "explanation": non-empty explanation

            All four options must be non-empty.
            No markdown formatting.
        """.trimIndent()

        return try {
            val payload = JSONObject()
                .put("contents", org.json.JSONArray().put(
                    JSONObject().put(
                        "parts",
                        org.json.JSONArray().put(
                            JSONObject().put("text", prompt)
                        )
                    )
                ))

            val body = payload.toString().toRequestBody(
                "application/json".toMediaType()
            )

            val request = Request.Builder()
                .url(
                    "https://generativelanguage.googleapis.com/" +
                            "v1beta/models/$model:generateContent"
                )
                .header("x-goog-api-key", apiKey)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val responseJson = JSONObject(
                    response.body?.string() ?: return null
                )

                val text = responseJson
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()

                if (AiJsonValidator.validate(text)) {
                    text
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
