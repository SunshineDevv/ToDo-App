package com.example.todoapp.data.note.mapper

import com.example.todoapp.data.local.database.entity.NoteDb
import com.example.todoapp.data.note.remote.dto.NoteResponse

fun NoteResponse.toNoteDb(): NoteDb? {
    val noteId = id?.takeIf { it.isNotBlank() } ?: return null

    val created = dateCreate ?: System.currentTimeMillis()
    val updated = dateUpdate ?: created

    return NoteDb(
        id = noteId,
        userOwnerId = userOwnerId,
        noteName = noteName.orEmpty(),
        noteText = noteText.orEmpty(),
        dateCreate = created,
        dateUpdate = updated,
        noteColor = noteColor ?: "button_background_orange",
        isSyncedNote = true,
        isDeletedNote = isDeletedNote ?: false
    )
}