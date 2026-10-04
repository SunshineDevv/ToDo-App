package com.example.todoapp.data.note.repository

import androidx.lifecycle.LiveData
import com.example.todoapp.data.local.database.dao.NoteDao
import com.example.todoapp.data.local.database.entity.NoteDb
import com.example.todoapp.data.note.mapper.toNoteDb
import com.example.todoapp.data.note.remote.api.NotesBackendApi
import com.example.todoapp.data.note.remote.dto.CreateNoteRequest
import com.example.todoapp.data.note.remote.dto.UpdateNoteRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val notesApi: NotesBackendApi
) {

    val allNotes: LiveData<List<NoteDb>> = noteDao.getAllNotes()

    suspend fun upsert(note: NoteDb) {
        noteDao.upsertNote(note)
    }

    suspend fun delete(note: NoteDb) {
        noteDao.deleteNote(note)
    }

    fun getUserNotes(userId: String): LiveData<List<NoteDb>> {
        return noteDao.getUserNotes(userId)
    }

    suspend fun refreshNotesFromBackend() {
        val response = notesApi.getNotes()

        val remoteNotes = response.notes
            .orEmpty()
            .mapNotNull { it.toNoteDb() }

        if (remoteNotes.isNotEmpty()) {
            noteDao.insertAll(remoteNotes)
        }
    }

    suspend fun createNote(
        id: String,
        noteName: String,
        noteText: String,
        noteColor: String
    ): NoteDb {
        val response = notesApi.createNote(
            CreateNoteRequest(
                id = id,
                noteName = noteName,
                noteText = noteText,
                noteColor = noteColor
            )
        )

        val note = response.note?.toNoteDb()
            ?: throw IllegalStateException("Invalid create note response")

        noteDao.upsertNote(note)

        return note
    }

    suspend fun updateNote(
        id: String,
        noteName: String,
        noteText: String,
        noteColor: String
    ): NoteDb {
        val response = notesApi.updateNote(
            id = id,
            request = UpdateNoteRequest(
                noteName = noteName,
                noteText = noteText,
                noteColor = noteColor
            )
        )

        val note = response.note?.toNoteDb()
            ?: throw IllegalStateException("Invalid update note response")

        noteDao.upsertNote(note)

        return note
    }

    suspend fun deleteNoteFromBackend(id: String) {
        notesApi.deleteNote(id)
        noteDao.deleteNoteById(id)
    }

    suspend fun syncNotes() {
        refreshNotesFromBackend()
    }
}