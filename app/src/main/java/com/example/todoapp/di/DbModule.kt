package com.example.todoapp.di

import android.content.Context
import androidx.room.Room
import com.example.todoapp.database.AppDatabase
import com.example.todoapp.database.dao.NoteDao
import com.example.todoapp.database.dao.UserDao
import com.example.todoapp.database.dao.UserWithNotesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DbModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase{
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "Note_App"
        ).build()
    }

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.getNoteDao()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.getUserDao()
    }

    @Provides
    fun UserWithNotesDao(database: AppDatabase): UserWithNotesDao {
        return database.getUserWithNotesDao()
    }
}