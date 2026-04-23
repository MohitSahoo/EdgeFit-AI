package com.edgefit.coach.data.model

import com.google.gson.annotations.SerializedName

data class VideoStatusResponse(
    val status: String,
    val message: String? = null,
    @SerializedName("stream_url")
    val streamUrl: String? = null,
    val features: List<String>? = null,
    @SerializedName("motivation_monitoring")
    val motivationMonitoring: Boolean? = null
)
