package com.example.legoev3android.di

import com.example.legoev3android.domain.use_case.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideControllerUseCase() = ControllerUseCases(
        MonitorBatteryUseCase(),
        JoystickDriveUseCase(),
        JoystickSteerUseCase(),
        PlayNoteUseCase()
    )
}