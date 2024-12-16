package com.example.todoapp.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithNotes(
    @Embedded val user: UserDb,
    @Relation(
        parentColumn = "userId",
        entityColumn = "note_user_owner"
    )
    val noteList: List<NoteDb>
)
