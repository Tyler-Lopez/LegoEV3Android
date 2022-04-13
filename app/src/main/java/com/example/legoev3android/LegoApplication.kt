package com.example.legoev3android

import android.app.Application
import com.example.legoev3android.domain.use_case.*
import dagger.Provides
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Singleton

@HiltAndroidApp
class LegoApplication : Application() {
}