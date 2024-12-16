package com.example.todoapp.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todoapp.database.dao.NoteDao
import com.example.todoapp.database.dao.UserDao
import com.example.todoapp.database.model.NoteDb
import com.example.todoapp.database.model.UserDb


@Database(
    entities = [NoteDb::class, UserDb::class],
    version = 7,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 6,to = 7)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getNoteDao(): NoteDao
    abstract fun getUserDao(): UserDao
}