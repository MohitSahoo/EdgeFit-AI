package com.edgefit.coach.data.model

import com.google.gson.annotations.SerializedName

data class DashboardData(
    @SerializedName("posture_health_score")
    val postureHealthScore: Double,
    @SerializedName("slouch_to_good_conversions")
    val slouchToGoodConversions: Int,
    @SerializedName("consistency_index")
    val consistencyIndex: Double,
    @SerializedName("session_success_rate")
    val sessionSuccessRate: Double,
    @SerializedName("recent_trend_score")
    val recentTrendScore: Double,
    @SerializedName("total_good_posture_minutes")
    val totalGoodPostureMinutes: Double
)

data class DashboardResponse(
    val status: String,
    @SerializedName("generated_at")
    val generatedAt: String,
    val data: DashboardData,
    val metadata: DashboardMetadata? = null
)

data class DashboardMetadata(
    val description: String,
    @SerializedName("endpoints_count")
    val endpointsCount: Int,
    val source: String
)
