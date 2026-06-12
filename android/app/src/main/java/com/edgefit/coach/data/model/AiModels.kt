package com.edgefit.coach.data.model

import com.google.gson.annotations.SerializedName

// ─── Groq API Request/Response Models (OpenAI-compatible format) ───

data class GroqChatRequest(
    val model: String = "llama-3.3-70b-versatile",
    val messages: List<GroqMessage>,
    val temperature: Float = 0.7f,
    @SerializedName("max_tokens")
    val maxTokens: Int = 1024
)

data class GroqMessage(
    val role: String,  // "system", "user", "assistant"
    val content: String
)

data class GroqChatResponse(
    val id: String?,
    val choices: List<GroqChoice>?,
    val error: GroqError? = null
)

data class GroqChoice(
    val message: GroqMessage,
    @SerializedName("finish_reason")
    val finishReason: String?
)

data class GroqError(
    val message: String?,
    val type: String?
)

// ─── App-level AI models ───

data class ChatRequest(
    val message: String,
    val conversationHistory: List<ChatHistoryItem> = emptyList()
)

data class MotivationRequest(
    val exerciseType: String,
    val reps: Int,
    val formScore: Float,
    val durationSeconds: Int
)

data class WorkoutPlanRequest(
    val fitnessLevel: String,
    val goals: List<String>,
    val availableMinutes: Int,
    val preferredExercises: List<String> = emptyList()
)

// ─── Generic response wrapper ───

data class GenericResponse(
    val status: String,
    val message: String? = null
)
