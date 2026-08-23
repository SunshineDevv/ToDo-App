package com.example.todoapp.domain.auth.model

data class ForgotPasswordResult(
    val message: String,
    val devResetToken: String?
)