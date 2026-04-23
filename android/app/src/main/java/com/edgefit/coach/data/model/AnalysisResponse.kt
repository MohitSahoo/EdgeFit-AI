package com.edgefit.coach.data.model

import com.google.gson.annotations.SerializedName

data class AnalysisResponse(
    val status: String,
    val message: String,
    val analysis: AnalysisData,
    @SerializedName("next_steps")
    val nextSteps: NextSteps
)

data class AnalysisData(
    @SerializedName("report_generated")
    val reportGenerated: Boolean,
    @SerializedName("report_file")
    val reportFile: String,
    @SerializedName("analysis_response")
    val analysisResponse: String,
    @SerializedName("conversation_id")
    val conversationId: String,
    val timestamp: String
)

data class NextSteps(
    @SerializedName("chat_endpoint")
    val chatEndpoint: String,
    val description: String
)

data class ReportFileResponse(
    val status: String,
    val filename: String,
    val content: String,
    @SerializedName("generated_at")
    val generatedAt: String
)
