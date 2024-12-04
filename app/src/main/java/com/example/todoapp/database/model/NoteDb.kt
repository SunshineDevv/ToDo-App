package com.example.todoapp.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteDb(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "note_name") val noteName: String?,
    @ColumnInfo(name = "note_text") val noteText: String?,
    @ColumnInfo(name = "date_create") val dateCreate: Long?,
    @ColumnInfo(name = "date_update") val dateUpdate: Long?,
    @ColumnInfo(name = "note_color", defaultValue = "#E8774E") val noteColor: String?
)
