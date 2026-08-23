package com.example.todoapp.data.local.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todoapp.data.local.database.dao.NoteDao
import com.example.todoapp.data.local.database.dao.UserDao
import com.example.todoapp.data.local.database.entity.NoteDb
import com.example.todoapp.data.local.database.entity.UserDb

@Database(
    entities = [NoteDb::class, UserDb::class],
    version = 8,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getNoteDao(): NoteDao
    abstract fun getUserDao(): UserDao
}