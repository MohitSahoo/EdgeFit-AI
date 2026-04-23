package com.edgefit.coach.data.repository

import com.edgefit.coach.data.model.AnalysisResponse
import com.edgefit.coach.data.model.ReportFileResponse
import com.edgefit.coach.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnalysisRepository(private val apiService: ApiService) {

    suspend fun generateReport(): Result<AnalysisResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.generateAnalysisReport()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to generate report: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReportFile(): Result<ReportFileResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getReportFile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get report file: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
