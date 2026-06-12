package com.edgefit.coach

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EdgeFitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
