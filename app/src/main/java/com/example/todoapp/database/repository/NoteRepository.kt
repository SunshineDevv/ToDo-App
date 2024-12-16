package com.example.todoapp.database.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.todoapp.database.dao.NoteDao
import com.example.todoapp.database.model.NoteDb
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val firestore: FirebaseFirestore
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

    private suspend fun syncLocalNotesToFirestore(userId: String) {
        try {
            val unsyncedNotes = noteDao.getUnsyncedNotesForUser(userId)
            unsyncedNotes.forEach { note ->
                val noteData = hashMapOf(
                    "noteName" to note.noteName,
                    "noteText" to note.noteText,
                    "dateCreate" to note.dateCreate,
                    "dateUpdate" to note.dateUpdate,
                    "noteColor" to note.noteColor,
                    "isDeletedNote" to note.isDeletedNote
                )
                try {
                    firestore.collection("users")
                        .document(userId)
                        .collection("notes")
                        .document(note.id)
                        .set(noteData)
                        .await()
                    // Помечаем локальную заметку как синхронизированную
                    noteDao.markNoteAsSynced(note.id)
                } catch (e: Exception) {
                    Log.e("SyncError", "Failed to sync note: ${note.id}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("SyncError", "Error syncing local notes to Firestore", e)
        }
    }

    private suspend fun syncFirestoreNotesToRoom(userId: String) {
        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("notes")
                .get()
                .await()

            if (snapshot != null && !snapshot.isEmpty) {
                val firestoreNotes = snapshot.documents.mapNotNull { document ->
                    val firestoreDateUpdate = document.getLong("dateUpdate") ?: 0L
                    val isDeletedNote = document.getBoolean("isDeletedNote") ?: false
                    val localNote = noteDao.getNoteById(document.id)

                    if (isDeletedNote) {
                        // Если заметка удалена в Firestore
                        if (localNote == null || firestoreDateUpdate > (localNote.dateUpdate
                                ?: 0)
                        ) {
                            // Если локальной версии нет или она устарела, удаляем её
                            noteDao.deleteNoteById(document.id)
                            null
                        } else {
                            null
                        }
                    } else {
                        // Если заметка не удалена
                        if (localNote == null || firestoreDateUpdate > (localNote.dateUpdate
                                ?: 0)
                        ) {
                            // Возвращаем обновлённую или новую заметку
                            NoteDb(
                                id = document.id,
                                userOwnerId = userId,
                                noteName = document.getString("noteName"),
                                noteText = document.getString("noteText"),
                                dateCreate = document.getLong("dateCreate"),
                                dateUpdate = firestoreDateUpdate,
                                noteColor = document.getString("noteColor"),
                                isSyncedNote = true,
                                isDeletedNote = false
                            )
                        } else {
                            null // Если локальная версия новее, ничего не делаем
                        }
                    }
                }

                // Сохраняем обновленные или новые заметки в Room
                if (firestoreNotes.isNotEmpty()) {
                    noteDao.insertAll(firestoreNotes)
                }
            }
        } catch (e: Exception) {
            Log.e("SyncError", "Error syncing Firestore notes to Room", e)
        }
    }

    suspend fun syncNotes(userId: String) {
        syncLocalNotesToFirestore(userId)
        syncFirestoreNotesToRoom(userId)
    }
}