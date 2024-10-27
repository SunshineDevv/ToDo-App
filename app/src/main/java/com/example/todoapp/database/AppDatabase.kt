package com.example.todoapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.todoapp.database.dao.NoteDao
import com.example.todoapp.database.model.NoteDb

@Database(
    entities = [NoteDb::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getNoteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "Note_App"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}