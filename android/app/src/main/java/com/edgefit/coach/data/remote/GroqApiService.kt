package com.edgefit.coach.data.remote

import com.edgefit.coach.data.model.GroqChatRequest
import com.edgefit.coach.data.model.GroqChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit interface for the Groq API (direct, no FastAPI middleman).
 *
 * Base URL: https://api.groq.com/openai/v1/
 */
interface GroqApiService {

    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GroqChatRequest
    ): Response<GroqChatResponse>
}
