package com.example.todoapp.ui.fragment.note

data class NoteModel(
    val id: Long,
    val noteText: String?,
    val noteName: String?,
    val noteDateCreate: String?,
    val noteDateUpdate: String?
)
