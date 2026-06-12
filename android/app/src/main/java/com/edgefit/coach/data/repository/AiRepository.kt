package com.edgefit.coach.data.repository

import com.edgefit.coach.data.model.*
import com.edgefit.coach.data.remote.GroqApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AI repository that calls Groq directly from Android.
 * Includes offline fallback with template messages.
 */
class AiRepository(
    private val groqApiService: GroqApiService,
    private val apiKey: String
) {
    companion object {
        private const val CHAT_MODEL = "llama-3.3-70b-versatile"
        private const val QUICK_MODEL = "llama-3.1-8b-instant"

        private const val SYSTEM_PROMPT = """You are EdgeFit AI Coach, a professional fitness and exercise coaching assistant. Your role is to:
1. Provide helpful advice about exercises, form, and workout routines
2. Analyze workout data and give actionable recommendations  
3. Answer questions about fitness, nutrition, and healthy habits
4. Be encouraging, professional, and concise
5. Keep responses practical and to the point

Stay focused on fitness topics. Always maintain a helpful and motivational tone."""

        // Offline fallback templates
        private val OFFLINE_MOTIVATION = listOf(
            "Keep pushing! Every rep counts towards your goals! 💪",
            "You're stronger than you think. Stay consistent!",
            "Great effort! Remember, progress is progress, no matter how small.",
            "Your body achieves what your mind believes. Keep going!",
            "The only bad workout is the one that didn't happen. You showed up! 🔥",
            "Form over speed. Quality reps build real strength.",
            "Consistency beats intensity. You're building a habit! 🏆",
            "Rest when you need to, but never quit. You've got this!"
        )
    }

    /**
     * Send a chat message to Groq.
     *
     * @return The AI response text, or an offline fallback.
     */
    suspend fun chat(
        message: String,
        conversationHistory: List<ChatHistoryItem> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messages = mutableListOf(
                GroqMessage(role = "system", content = SYSTEM_PROMPT)
            )

            // Add conversation history (last 10 exchanges)
            conversationHistory.takeLast(10).forEach { item ->
                messages.add(GroqMessage(role = "user", content = item.userMessage))
                messages.add(GroqMessage(role = "assistant", content = item.assistantResponse))
            }

            messages.add(GroqMessage(role = "user", content = message))

            val request = GroqChatRequest(
                model = CHAT_MODEL,
                messages = messages,
                temperature = 0.7f,
                maxTokens = 1024
            )

            val response = groqApiService.chatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.choices?.firstOrNull()?.message?.content
                if (text != null) {
                    Result.success(text)
                } else {
                    Result.failure(Exception(body?.error?.message ?: "Empty response from Groq"))
                }
            } else {
                Result.failure(Exception("Groq API error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            // Offline fallback
            Result.failure(e)
        }
    }

    /**
     * Get a motivational quote based on workout performance.
     * Uses the faster model for quick responses.
     */
    suspend fun getMotivation(
        exerciseType: String,
        reps: Int,
        formScore: Float,
        durationSeconds: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """Based on this workout performance, give a short (1-2 sentences) motivational message:
- Exercise: $exerciseType
- Reps: $reps
- Form Score: ${formScore.toInt()}%
- Duration: ${durationSeconds / 60} min ${durationSeconds % 60} sec

${if (formScore >= 80) "Focus on praising great form." else if (formScore >= 60) "Encourage improvement." else "Provide constructive feedback to improve form."}"""

            val request = GroqChatRequest(
                model = QUICK_MODEL,
                messages = listOf(
                    GroqMessage(role = "system", content = "You are a concise fitness coach. Respond in 1-2 sentences max."),
                    GroqMessage(role = "user", content = prompt)
                ),
                temperature = 0.8f,
                maxTokens = 150
            )

            val response = groqApiService.chatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            if (response.isSuccessful) {
                val text = response.body()?.choices?.firstOrNull()?.message?.content
                Result.success(text ?: getOfflineMotivation())
            } else {
                Result.success(getOfflineMotivation())
            }
        } catch (e: Exception) {
            Result.success(getOfflineMotivation())
        }
    }

    /**
     * Generate a workout plan using AI.
     */
    suspend fun generateWorkoutPlan(
        fitnessLevel: String,
        goals: List<String>,
        availableMinutes: Int,
        preferredExercises: List<String>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """Create a workout plan with these parameters:
- Fitness Level: $fitnessLevel
- Goals: ${goals.joinToString(", ")}
- Available Time: $availableMinutes minutes
- Preferred Exercises: ${if (preferredExercises.isNotEmpty()) preferredExercises.joinToString(", ") else "No preference"}

Format as a structured plan with exercises, sets, reps, and rest periods.
Include warm-up and cool-down."""

            val request = GroqChatRequest(
                model = CHAT_MODEL,
                messages = listOf(
                    GroqMessage(role = "system", content = SYSTEM_PROMPT),
                    GroqMessage(role = "user", content = prompt)
                ),
                temperature = 0.6f,
                maxTokens = 2048
            )

            val response = groqApiService.chatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            if (response.isSuccessful) {
                val text = response.body()?.choices?.firstOrNull()?.message?.content
                if (text != null) {
                    Result.success(text)
                } else {
                    Result.failure(Exception("Empty plan response"))
                }
            } else {
                Result.failure(Exception("Failed to generate plan: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get an AI analysis of workout data (sent as context, computed on Android).
     */
    suspend fun analyzeWorkout(
        exerciseType: String,
        reps: Int,
        durationSeconds: Int,
        formScore: Float,
        repDetails: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """Analyze this workout session and provide brief insights:

Exercise: $exerciseType
Total Reps: $reps
Duration: ${durationSeconds / 60} min ${durationSeconds % 60} sec
Average Form Score: ${formScore.toInt()}%

$repDetails

Provide:
1. Performance assessment (1-2 sentences)
2. What went well
3. One specific area to improve
4. Recommendation for next session"""

            val request = GroqChatRequest(
                model = CHAT_MODEL,
                messages = listOf(
                    GroqMessage(role = "system", content = SYSTEM_PROMPT),
                    GroqMessage(role = "user", content = prompt)
                ),
                temperature = 0.5f,
                maxTokens = 512
            )

            val response = groqApiService.chatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            if (response.isSuccessful) {
                val text = response.body()?.choices?.firstOrNull()?.message?.content
                Result.success(text ?: "Workout completed successfully!")
            } else {
                Result.failure(Exception("Analysis failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Offline fallback motivation — used when network is unavailable.
     * Motivation should never fail.
     */
    private fun getOfflineMotivation(): String {
        return OFFLINE_MOTIVATION.random()
    }

    /**
     * Check if the Groq API is reachable.
     */
    suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = GroqChatRequest(
                model = QUICK_MODEL,
                messages = listOf(GroqMessage(role = "user", content = "ping")),
                maxTokens = 5
            )
            val response = groqApiService.chatCompletion("Bearer $apiKey", request)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
