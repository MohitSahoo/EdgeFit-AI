package com.edgefit.coach.data.remote

import com.edgefit.coach.data.model.MotivationMessage
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketManager(private val serverIp: String, private val serverPort: String) {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    private val gson = Gson()

    val messages: Flow<MotivationMessage> = callbackFlow {
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Connection opened successfully
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val message = gson.fromJson(text, MotivationMessage::class.java)
                    trySend(message)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
            }
        }

        val request = Request.Builder()
            .url("ws://$serverIp:$serverPort/ws/motivation")
            .build()

        webSocket = client.newWebSocket(request, listener)

        awaitClose {
            webSocket?.close(1000, "Client closing")
            webSocket = null
        }
    }

    fun connect() {
        // Connection is established when Flow is collected
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
    }

    fun sendPing() {
        webSocket?.send("ping")
    }

    fun isConnected(): Boolean {
        return webSocket != null
    }
}
