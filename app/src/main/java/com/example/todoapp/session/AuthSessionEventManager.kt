package com.example.todoapp.session

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthSessionEvent {
    object SessionExpired : AuthSessionEvent()
}

@Singleton
class AuthSessionEventManager @Inject constructor() {

    private val _events = MutableSharedFlow<AuthSessionEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val events: SharedFlow<AuthSessionEvent> = _events.asSharedFlow()

    fun notifySessionExpired() {
        _events.tryEmit(AuthSessionEvent.SessionExpired)
    }
}