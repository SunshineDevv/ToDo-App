package com.example.todoapp.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteDb(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "note_user_owner", defaultValue = "0") val userOwnerId: String?,
    @ColumnInfo(name = "note_name") val noteName: String?,
    @ColumnInfo(name = "note_text") val noteText: String?,
    @ColumnInfo(name = "date_create") val dateCreate: Long?,
    @ColumnInfo(name = "date_update") val dateUpdate: Long?,
    @ColumnInfo(name = "note_color", defaultValue = "button_background_orange") val noteColor: String?,
    @ColumnInfo(name = "is_sync_note", defaultValue = "false") val isSyncedNote: Boolean = false,
    @ColumnInfo(name = "is_deleted_note", defaultValue = "false") var isDeletedNote: Boolean = false
)
