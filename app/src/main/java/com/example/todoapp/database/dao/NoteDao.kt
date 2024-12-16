package com.example.todoapp.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.todoapp.database.model.NoteDb

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes")
    fun getAllNotes(): LiveData<List<NoteDb>>

    @Upsert
    suspend fun upsertNote(noteDb: NoteDb)

    @Delete
    suspend fun deleteNote(noteDb: NoteDb)

    @Query("SELECT * FROM notes WHERE note_user_owner = :userId")
    fun getUserNotes(userId: String): LiveData<List<NoteDb>>

    @Query("SELECT * FROM notes WHERE note_user_owner = :userId AND is_sync_note = 0")
    suspend fun getUnsyncedNotesForUser(userId: String): List<NoteDb>

    @Query("UPDATE notes SET is_sync_note = 1 WHERE id = :id")
    suspend fun markNoteAsSynced(id: String)

    @Upsert
    suspend fun insertAll(notes: List<NoteDb>)

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: String): NoteDb?

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)

}