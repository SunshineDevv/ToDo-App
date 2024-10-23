package com.example.todoapp.extensions

import com.example.todoapp.database.model.NoteDb
import com.example.todoapp.ui.fragment.noteaction.NoteModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun NoteDb.toNoteUIModel(): NoteModel {
    return NoteModel(
        id = id,
        noteName = noteName,
        noteText = noteText,
        noteDateCreate = dateCreate?.toFormattedDate(),
        noteDateUpdate = dateUpdate?.toFormattedDate()
    )
}

fun NoteModel.toNoteDbModel(): NoteDb {
    return NoteDb(
        id = id,
        noteName = noteName,
        noteText = noteText,
        dateCreate = noteDateCreate?.toDateInMillis(),
        dateUpdate = noteDateUpdate?.toDateInMillis()
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