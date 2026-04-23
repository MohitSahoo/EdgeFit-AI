package com.edgefit.coach.data.model

import com.google.gson.annotations.SerializedName

data class HealthCheckResponse(
    val status: String,
    val timestamp: String,
    val components: ComponentsStatus,
    val urls: UrlsInfo
)

data class ComponentsStatus(
    @SerializedName("api_server")
    val apiServer: String,
    @SerializedName("websocket_server")
    val websocketServer: String,
    @SerializedName("streamlit_frontend")
    val streamlitFrontend: String,
    @SerializedName("video_stream")
    val videoStream: String,
    @SerializedName("chat_system")
    val chatSystem: String,
    val dashboard: String,
    val analysis: String
)

data class UrlsInfo(
    val frontend: String,
    @SerializedName("api_docs")
    val apiDocs: String,
    val websocket: String
)

data class GenericResponse(
    val status: String,
    val message: String
)

data class FilesStatusResponse(
    val status: String,
    val files: Map<String, FileInfo>,
    val timestamp: String
)

data class FileInfo(
    val exists: Boolean,
    val size: Long? = null,
    val modified: String? = null
)
