package com.edgefit.coach.di

import android.content.Context
import com.edgefit.coach.data.local.EdgeFitDatabase
import com.edgefit.coach.data.local.WorkoutRepository
import com.edgefit.coach.data.local.dao.WorkoutDao
import com.edgefit.coach.data.remote.ApiService
import com.edgefit.coach.data.remote.GroqApiService
import com.edgefit.coach.data.remote.WebSocketManager
import com.edgefit.coach.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ─── Database ───

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EdgeFitDatabase {
        return EdgeFitDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideWorkoutDao(database: EdgeFitDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(dao: WorkoutDao): WorkoutRepository {
        return WorkoutRepository(dao)
    }

    // ─── Networking ───

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGroqApiService(client: OkHttpClient): GroqApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAiRepository(groqApiService: GroqApiService): AiRepository {
        return AiRepository(groqApiService, apiKey = com.edgefit.coach.BuildConfig.GROQ_API_KEY)
    }

    @Provides
    @Singleton
    fun provideApiService(client: OkHttpClient): ApiService {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // Default emulator IP
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWebSocketManager(): WebSocketManager {
        return WebSocketManager(serverIp = "10.0.2.2", serverPort = "8000")
    }

    @Provides
    @Singleton
    fun provideDashboardRepository(apiService: ApiService): DashboardRepository {
        return DashboardRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideVideoRepository(apiService: ApiService): VideoRepository {
        return VideoRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideWebSocketRepository(webSocketManager: WebSocketManager): WebSocketRepository {
        return WebSocketRepository(webSocketManager)
    }

    @Provides
    @Singleton
    fun provideAnalysisRepository(apiService: ApiService): AnalysisRepository {
        return AnalysisRepository(apiService)
    }
}
