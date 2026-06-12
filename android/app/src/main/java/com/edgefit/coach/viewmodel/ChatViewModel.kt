package com.edgefit.coach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgefit.coach.data.model.ChatHistoryItem
import com.edgefit.coach.data.repository.AiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatHistoryItem>>(emptyList())
    val messages: StateFlow<List<ChatHistoryItem>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            _isSending.value = true
            _error.value = null

            // Add user message to UI immediately for responsiveness
            val userTimestamp = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            val tempItem = ChatHistoryItem(
                timestamp = userTimestamp,
                userMessage = message,
                assistantResponse = "...", // Temporary placeholder
                conversationId = "local"
            )
            
            // We append a temporary item to show the user's message while waiting
            val currentList = _messages.value.toMutableList()
            currentList.add(tempItem)
            _messages.value = currentList

            val result = aiRepository.chat(message, _messages.value.dropLast(1))

            result.fold(
                onSuccess = { response ->
                    val finalItem = ChatHistoryItem(
                        timestamp = userTimestamp,
                        userMessage = message,
                        assistantResponse = response,
                        conversationId = "local"
                    )
                    // Replace the temporary item with the actual response
                    val updatedList = _messages.value.toMutableList()
                    updatedList[updatedList.lastIndex] = finalItem
                    _messages.value = updatedList
                    _isSending.value = false
                },
                onFailure = { exception ->
                    // Remove the temporary item on failure
                    val fallbackList = _messages.value.toMutableList()
                    fallbackList.removeAt(fallbackList.lastIndex)
                    _messages.value = fallbackList
                    
                    _error.value = exception.message ?: "Failed to send message"
                    _isSending.value = false
                }
            )
        }
    }

    fun clearHistory() {
        _messages.value = emptyList()
    }

    fun clearError() {
        _error.value = null
    }
}
