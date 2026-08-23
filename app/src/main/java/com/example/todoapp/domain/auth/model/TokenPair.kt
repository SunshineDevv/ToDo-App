package com.example.todoapp.domain.auth.model

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)