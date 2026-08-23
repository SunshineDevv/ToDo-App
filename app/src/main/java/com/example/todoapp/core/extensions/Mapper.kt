package com.example.todoapp.core.extensions

import com.example.todoapp.data.local.database.entity.NoteDb
import com.example.todoapp.presentation.note.detail.NoteModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun NoteDb.toNoteUIModel(): NoteModel {
    return NoteModel(
        id = id,
        userOwnerId =userOwnerId,
        noteName = noteName,
        noteText = noteText,
        noteDateCreate = dateCreate?.toFormattedDate(),
        noteDateUpdate = dateUpdate?.toFormattedDate(),
        noteColor = noteColor
    )
}

fun NoteModel.toNoteDbModel(): NoteDb {
    return NoteDb(
        id = id,
        userOwnerId = userOwnerId,
        noteName = noteName,
        noteText = noteText,
        dateCreate = noteDateCreate?.toDateInMillis(),
        dateUpdate = noteDateUpdate?.toDateInMillis(),
        noteColor = noteColor
    )
}

fun List<NoteModel>.toNoteDbModelList(): List<NoteDb> {
    return this.map { it.toNoteDbModel() }
}

fun List<NoteDb>.toNoteModelList(): List<NoteModel> {
    return this.map { it.toNoteUIModel() }
}

fun Long.toFormattedDate(): String {
    val date = Date(this)
    val format = SimpleDateFormat("dd.MM", Locale.getDefault())
    return format.format(date)
}

fun String.toDateInMillis(): Long {
    val format = SimpleDateFormat("dd.MM", Locale.getDefault())
    val date = format.parse(this)
    return date?.time ?: 0L
}