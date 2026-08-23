package com.example.todoapp.di

import com.example.todoapp.data.auth.remote.BackendConfig
import com.example.todoapp.data.auth.remote.api.AuthBackendApi
import com.example.todoapp.data.auth.remote.interceptor.AuthInterceptor
import com.example.todoapp.data.auth.remote.interceptor.TokenAuthenticator
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .create()
    }

    @Provides
    @Singleton
    @PublicOkHttpClient
    fun providePublicOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides
    @Singleton
    @AuthenticatedOkHttpClient
    fun provideAuthenticatedOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    @PublicRetrofit
    fun providePublicRetrofit(
        @PublicOkHttpClient okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BackendConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @AuthenticatedRetrofit
    fun provideAuthenticatedRetrofit(
        @AuthenticatedOkHttpClient okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BackendConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @PublicBackendApi
    fun providePublicAuthBackendApi(
        @PublicRetrofit retrofit: Retrofit
    ): AuthBackendApi {
        return retrofit.create(AuthBackendApi::class.java)
    }

    @Provides
    @Singleton
    @AuthenticatedBackendApi
    fun provideAuthenticatedAuthBackendApi(
        @AuthenticatedRetrofit retrofit: Retrofit
    ): AuthBackendApi {
        return retrofit.create(AuthBackendApi::class.java)
    }
}