package com.example.todoapp.presentation.note.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.local.database.entity.NoteDb
import com.example.todoapp.data.legacy.repository.NoteRepository
import com.example.todoapp.core.extensions.observeLiveData
import com.example.todoapp.core.extensions.toNoteDbModel
import com.example.todoapp.core.extensions.toNoteModelList
import com.example.todoapp.presentation.note.state.NoteState
import com.example.todoapp.presentation.note.detail.NoteModel
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

    private val _Note_state = MutableStateFlow<NoteState>(NoteState.Empty)
    val state = _Note_state.asStateFlow()

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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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

        _Note_state.value = if (noteList.size == 1) {
            NoteState.Success("Note was deleted!")
        } else {
            NoteState.Success("Notes were deleted!")
        }
    }

    fun clearState() {
        _Note_state.value = NoteState.Empty
    }

    fun enableSelectionMode() {
        _isSelectionMode.value = true
    }

    fun disableSelectionMode() {
        _isSelectionMode.value = false
    }
}