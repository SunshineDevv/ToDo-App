package com.example.todoapp.presentation.note.state

sealed class NoteState {
    data class Success(val successMsg: String) : NoteState()
    data class Error(val errorMsg: String?) : NoteState()
    data object Empty : NoteState()
}