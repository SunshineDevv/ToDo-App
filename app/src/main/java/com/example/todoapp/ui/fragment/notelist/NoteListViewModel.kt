package com.example.todoapp.ui.fragment.notelist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.database.model.NoteDb
import com.example.todoapp.database.repository.NoteRepository
import com.example.todoapp.extensions.observeLiveData
import com.example.todoapp.extensions.toNoteDbModel
import com.example.todoapp.extensions.toNoteModelList
import com.example.todoapp.ui.fragment.State
import com.example.todoapp.ui.fragment.note.NoteModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository,
) : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _notes = MutableStateFlow<List<NoteModel>>(emptyList())
    val notes = _notes.asStateFlow()

    private val _state = MutableStateFlow<State>(State.Empty)
    val state = _state.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    fun onStart() {
        firebaseAuth.currentUser?.uid?.let { userId ->
            observeLiveData(repository.getUserNotes(userId), ::handleNotesChanged)
        }
    }

    fun syncNotesToFirestore() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                try {
                    repository.syncNotes(userId)
//                    _state.value = State.Success("All notes synced successfully!")
                } catch (e: Exception) {
//                    _state.value = State.Error("Sync failed: ${e.message}")
                }
            }
        } else {
            Log.e("SyncError", "User ID is null")
//            _state.value = State.Error("User not logged in.")
        }
    }

    private fun handleNotesChanged(noteDbs: List<NoteDb>) {
        val sortedList = noteDbs.sortedByDescending {
            it.dateUpdate ?: it.dateCreate
        }
        val filteredList = sortedList.filter {
            !it.isDeletedNote
        }.toNoteModelList()
        _notes.value = filteredList
    }

    fun setSelected(note: NoteModel) {
        val updatedNotes = _notes.value.map {
            if (it.id == note.id) {
                it.copy(isSelected = MutableStateFlow(!it.isSelected.value))
            } else {
                it
            }
        }
        _notes.value = updatedNotes ?: emptyList()
    }

    fun deleteNote(noteList: List<NoteModel>) {
        noteList.forEach { note ->
            val updatedNoteDb = note.toNoteDbModel().copy(
                isDeletedNote = true,
                dateUpdate = System.currentTimeMillis()
            )

            viewModelScope.launch(Dispatchers.IO) {
                repository.upsert(updatedNoteDb)
            }

            val noteData = mapOf(
                "isDeletedNote" to true,
                "dateUpdate" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(note.userOwnerId.toString())
                .collection("notes")
                .document(note.id)
                .update(noteData)
                .addOnSuccessListener {
//                    _state.value = State.Success("Note marked as deleted in Firestore!")
                }
                .addOnFailureListener {
//                    _state.value = State.Error("Failed to mark note as deleted: ${it.message}")
                }
        }

        _state.value = if (noteList.size == 1) {
            State.Success("Note was deleted!")
        } else {
            State.Success("Notes were deleted!")
        }
    }

    fun clearState() {
        _state.value = State.Empty
    }

    fun enableSelectionMode() {
        _isSelectionMode.value = true
    }

    fun disableSelectionMode() {
        _isSelectionMode.value = false
    }
}