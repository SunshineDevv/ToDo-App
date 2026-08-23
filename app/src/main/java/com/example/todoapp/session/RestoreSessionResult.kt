package com.example.todoapp.session

sealed interface RestoreSessionResult {
    data object Authenticated : RestoreSessionResult
    data object Unauthenticated : RestoreSessionResult
    data object NetworkUnavailable : RestoreSessionResult
    data object ServerUnavailable : RestoreSessionResult
}