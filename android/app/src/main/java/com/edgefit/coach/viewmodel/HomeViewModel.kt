package com.edgefit.coach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgefit.coach.data.model.QuoteData
import com.edgefit.coach.data.model.VideoStatusResponse
import com.edgefit.coach.data.repository.VideoRepository
import com.edgefit.coach.data.repository.WebSocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val webSocketRepository: WebSocketRepository
) : ViewModel() {

    private val _sessionStatus = MutableStateFlow<VideoStatusResponse?>(null)
    val sessionStatus: StateFlow<VideoStatusResponse?> = _sessionStatus.asStateFlow()

    private val _motivationQuote = MutableStateFlow<QuoteData?>(null)
    val motivationQuote: StateFlow<QuoteData?> = _motivationQuote.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeWebSocket()
        checkSessionStatus()
        startPingKeepAlive()
    }

    private fun observeWebSocket() {
        viewModelScope.launch {
            webSocketRepository.messages.collect { message ->
                when (message.type) {
                    "motivation" -> {
                        message.data?.let { _motivationQuote.value = it }
                    }
                    "connection" -> {
                        // Connection established
                    }
                    "pong" -> {
                        // Keepalive response received
                    }
                }
            }
        }
    }

    private fun startPingKeepAlive() {
        viewModelScope.launch {
            while (true) {
                delay(30000) // 30 seconds
                if (webSocketRepository.isConnected()) {
                    webSocketRepository.sendPing()
                }
            }
        }
    }

    fun startSession() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            videoRepository.startVideo().fold(
                onSuccess = { response ->
                    _sessionStatus.value = response
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to start session"
                    _isLoading.value = false
                }
            )
        }
    }

    fun stopSession() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            videoRepository.stopVideo().fold(
                onSuccess = { response ->
                    _sessionStatus.value = response
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to stop session"
                    _isLoading.value = false
                }
            )
        }
    }

    fun checkSessionStatus() {
        viewModelScope.launch {
            videoRepository.getVideoStatus().fold(
                onSuccess = { response ->
                    _sessionStatus.value = response
                },
                onFailure = { exception ->
                    _error.value = exception.message
                }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun dismissQuote() {
        _motivationQuote.value = null
    }
}
