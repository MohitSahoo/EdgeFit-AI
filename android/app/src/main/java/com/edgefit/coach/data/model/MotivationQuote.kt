package com.edgefit.coach.data.model

import com.google.gson.annotations.SerializedName

data class MotivationMessage(
    val type: String,  // "connection", "motivation", "pong"
    val data: QuoteData? = null,
    val message: String? = null,
    val timestamp: String
)

data class QuoteData(
    val quote: String,
    val timestamp: String,
    @SerializedName("good_percentage")
    val goodPercentage: Double = 0.0,
    @SerializedName("good_posture_count")
    val goodPostureCount: Int = 0,
    @SerializedName("slouching_count")
    val slouchingCount: Int = 0
)
