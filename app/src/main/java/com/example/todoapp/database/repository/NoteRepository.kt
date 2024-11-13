package com.example.todoapp.database.repository

import androidx.lifecycle.LiveData
import com.example.todoapp.database.dao.NoteDao
import com.example.todoapp.database.model.NoteDb

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: LiveData<List<NoteDb>> = noteDao.getAllNotes()

    val notesFlow = noteDao.getNotesFlow()

    suspend fun upsert(note: NoteDb) {
        noteDao.upsertNote(note)
    }

    suspend fun delete(note: NoteDb) {
        noteDao.deleteNote(note)
    }
}