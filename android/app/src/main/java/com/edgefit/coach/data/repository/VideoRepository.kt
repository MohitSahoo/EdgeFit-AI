package com.edgefit.coach.data.repository

import com.edgefit.coach.data.model.VideoStatusResponse
import com.edgefit.coach.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VideoRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun startVideo(): Result<VideoStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.startVideo()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to start video: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stopVideo(): Result<VideoStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.stopVideo()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to stop video: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVideoStatus(): Result<VideoStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getVideoStatus()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get video status: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
