package com.example.todoapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todoapp.database.dao.NoteDao
import com.example.todoapp.database.model.NoteDb

@Database(
    entities = [NoteDb::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getNoteDao(): NoteDao
}