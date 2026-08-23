package com.example.todoapp.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicBackendApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedBackendApi