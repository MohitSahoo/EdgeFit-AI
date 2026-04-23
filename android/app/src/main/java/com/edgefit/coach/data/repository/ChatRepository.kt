package com.edgefit.coach.data.repository

import com.edgefit.coach.data.model.ChatHistoryItem
import com.edgefit.coach.data.model.ChatMessageRequest
import com.edgefit.coach.data.model.ChatMessageResponse
import com.edgefit.coach.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository(private val apiService: ApiService) {

    suspend fun sendMessage(message: String): Result<ChatMessageResponse> = withContext(Dispatchers.IO) {
        try {
            val request = ChatMessageRequest(message)
            val response = apiService.sendChatMessage(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to send message: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatHistory(limit: Int = 20): Result<List<ChatHistoryItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getChatHistory(limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.conversations)
            } else {
                Result.failure(Exception("Failed to get chat history: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearHistory(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.clearChatHistory()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to clear history: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
