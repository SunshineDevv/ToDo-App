package com.example.todoapp.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todoapp.database.dao.NoteDao
import com.example.todoapp.database.model.NoteDb

@Database(
    entities = [NoteDb::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2,to = 3)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getNoteDao(): NoteDao
}