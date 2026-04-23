package com.edgefit.coach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgefit.coach.data.model.ChatHistoryItem
import com.edgefit.coach.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatHistoryItem>>(emptyList())
    val messages: StateFlow<List<ChatHistoryItem>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadChatHistory()
    }

    fun loadChatHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            chatRepository.getChatHistory().fold(
                onSuccess = { history ->
                    _messages.value = history
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to load chat history"
                    _isLoading.value = false
                }
            )
        }
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            _isSending.value = true
            _error.value = null

            chatRepository.sendMessage(message).fold(
                onSuccess = { response ->
                    // Add the new message to the list
                    val newMessage = ChatHistoryItem(
                        timestamp = response.timestamp,
                        userMessage = message,
                        assistantResponse = response.response,
                        conversationId = response.conversationId
                    )
                    _messages.value = _messages.value + newMessage
                    _isSending.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to send message"
                    _isSending.value = false
                }
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            chatRepository.clearHistory().fold(
                onSuccess = {
                    _messages.value = emptyList()
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to clear history"
                    _isLoading.value = false
                }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }
}
