package com.example.todoapp.domain.auth.model

sealed interface RestoreSessionResult {
    data object Authenticated : RestoreSessionResult
    data object Unauthenticated : RestoreSessionResult
    data object NetworkUnavailable : RestoreSessionResult
    data object ServerUnavailable : RestoreSessionResult
}