package com.edgefit.coach.data.model

import com.google.gson.annotations.SerializedName

data class ChatMessageRequest(
    val message: String,
    val timestamp: String? = null
)

data class ChatMessageResponse(
    val response: String,
    val timestamp: String,
    @SerializedName("conversation_id")
    val conversationId: String
)

data class ChatHistoryItem(
    val timestamp: String,
    @SerializedName("user_message")
    val userMessage: String,
    @SerializedName("assistant_response")
    val assistantResponse: String,
    @SerializedName("conversation_id")
    val conversationId: String,
    val type: String? = null,
    @SerializedName("report_content")
    val reportContent: String? = null
)

data class ChatHistoryResponse(
    val status: String,
    val conversations: List<ChatHistoryItem>,
    @SerializedName("total_conversations")
    val totalConversations: Int,
    @SerializedName("system_prompt")
    val systemPrompt: String
)
