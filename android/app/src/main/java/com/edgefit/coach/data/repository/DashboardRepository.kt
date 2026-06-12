package com.edgefit.coach.data.repository

import com.edgefit.coach.data.model.DashboardData
import com.edgefit.coach.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DashboardRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun getDashboardData(): Result<DashboardData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDashboardData()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to fetch dashboard data: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshDashboard(): Result<DashboardData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.refreshDashboard()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to refresh dashboard: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
