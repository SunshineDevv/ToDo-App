package com.example.todoapp.data.note.remote.dto

data class NotesResponse(
    val notes: List<NoteResponse>? = emptyList()
)

data class NoteSingleResponse(
    val note: NoteResponse? = null
)

data class NoteResponse(
    val id: String? = null,
    val userOwnerId: String? = null,
    val noteName: String? = null,
    val noteText: String? = null,
    val dateCreate: Long? = null,
    val dateUpdate: Long? = null,
    val noteColor: String? = null,
    val isDeletedNote: Boolean? = false
)

data class CreateNoteRequest(
    val id: String,
    val noteName: String,
    val noteText: String,
    val noteColor: String
)

data class UpdateNoteRequest(
    val noteName: String,
    val noteText: String,
    val noteColor: String
)