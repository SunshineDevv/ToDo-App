package com.example.todoapp.di

import com.example.todoapp.data.device.repository.TrustedDeviceRepositoryImpl
import com.example.todoapp.domain.device.repository.TrustedDeviceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrustedDeviceRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTrustedDeviceRepository(
        implementation:
        TrustedDeviceRepositoryImpl
    ): TrustedDeviceRepository
}