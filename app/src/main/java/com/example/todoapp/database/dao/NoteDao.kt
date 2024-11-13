package com.example.todoapp.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.todoapp.database.model.NoteDb
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes")
    fun getAllNotes(): LiveData<List<NoteDb>>

    @Query("SELECT * FROM notes")
    fun getNotesFlow(): Flow<List<NoteDb>>

    @Upsert
    suspend fun upsertNote(noteDb: NoteDb)

    @Delete
    suspend fun deleteNote(noteDb: NoteDb)

}