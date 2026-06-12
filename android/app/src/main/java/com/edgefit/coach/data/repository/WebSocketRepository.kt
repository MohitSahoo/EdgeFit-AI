package com.edgefit.coach.data.repository

import com.edgefit.coach.data.model.MotivationMessage
import com.edgefit.coach.data.remote.WebSocketManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WebSocketRepository @Inject constructor(private val webSocketManager: WebSocketManager) {

    val messages: Flow<MotivationMessage> = webSocketManager.messages

    fun connect() {
        webSocketManager.connect()
    }

    fun disconnect() {
        webSocketManager.disconnect()
    }

    fun sendPing() {
        webSocketManager.sendPing()
    }

    fun isConnected(): Boolean {
        return webSocketManager.isConnected()
    }
}
