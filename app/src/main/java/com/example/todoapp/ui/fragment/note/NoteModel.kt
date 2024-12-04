package com.example.todoapp.ui.fragment.note

import kotlinx.coroutines.flow.MutableStateFlow

data class NoteModel(
    val id: Long,
    val noteText: String?,
    val noteName: String?,
    val noteDateCreate: String?,
    val noteDateUpdate: String?,
    var isSelected: MutableStateFlow<Boolean> = MutableStateFlow(false),
    var noteColor: String?
)
