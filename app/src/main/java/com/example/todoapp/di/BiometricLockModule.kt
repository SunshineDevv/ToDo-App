package com.example.todoapp.di

import com.example.todoapp.data.biometric.repository.BiometricLockRepositoryImpl
import com.example.todoapp.domain.biometric.repository.BiometricLockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BiometricLockModule {

    @Binds
    @Singleton
    abstract fun bindBiometricLockRepository(
        implementation: BiometricLockRepositoryImpl
    ): BiometricLockRepository
}