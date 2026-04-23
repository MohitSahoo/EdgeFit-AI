package com.edgefit.coach.data.remote

import com.edgefit.coach.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("health")
    suspend fun healthCheck(): Response<HealthCheckResponse>

    @GET("video/start")
    suspend fun startVideo(): Response<VideoStatusResponse>

    @GET("video/stop")
    suspend fun stopVideo(): Response<VideoStatusResponse>

    @GET("video/status")
    suspend fun getVideoStatus(): Response<VideoStatusResponse>

    @GET("dashboard/data")
    suspend fun getDashboardData(): Response<DashboardResponse>

    @GET("dashboard/refresh")
    suspend fun refreshDashboard(): Response<DashboardResponse>

    @POST("chat/message")
    suspend fun sendChatMessage(@Body request: ChatMessageRequest): Response<ChatMessageResponse>

    @GET("chat/history")
    suspend fun getChatHistory(@Query("limit") limit: Int = 20): Response<ChatHistoryResponse>

    @DELETE("chat/history")
    suspend fun clearChatHistory(): Response<GenericResponse>

    @POST("analyze/report")
    suspend fun generateAnalysisReport(): Response<AnalysisResponse>

    @GET("analyze/report-file")
    suspend fun getReportFile(): Response<ReportFileResponse>

    @GET("files/status")
    suspend fun getFilesStatus(): Response<FilesStatusResponse>
}
