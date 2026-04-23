package com.edgefit.coach.data.remote

import android.content.Context
import com.edgefit.coach.util.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null
    private lateinit var preferencesManager: PreferencesManager

    fun initialize(context: Context) {
        preferencesManager = PreferencesManager(context)
    }

    private fun getBaseUrl(): String {
        return runBlocking {
            val ip = preferencesManager.serverIp.first()
            val port = preferencesManager.serverPort.first()
            "http://$ip:$port/"
        }
    }

    private fun getOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)  // Long timeout for /analyze/report
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getApiService(): ApiService {
        if (retrofit == null || retrofit?.baseUrl().toString() != getBaseUrl()) {
            retrofit = Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(ApiService::class.java)
    }

    fun updateBaseUrl() {
        retrofit = null  // Force recreation with new base URL
    }
}
