package com.example.todoapp.presentation.note.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.core.extensions.observeLiveData
import com.example.todoapp.core.extensions.toNoteModelList
import com.example.todoapp.core.result.AppResult
import com.example.todoapp.data.local.database.entity.NoteDb
import com.example.todoapp.data.note.repository.NoteRepository
import com.example.todoapp.domain.auth.usecase.GetCurrentUserUseCase
import com.example.todoapp.presentation.note.detail.NoteModel
import com.example.todoapp.presentation.note.state.NoteState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private var currentUserId: String? = null

    private val _notes = MutableStateFlow<List<NoteModel>>(emptyList())
    val notes = _notes.asStateFlow()

    private val _Note_state = MutableStateFlow<NoteState>(NoteState.Empty)
    val state = _Note_state.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    fun onStart() {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val userId = result.data.id
                    currentUserId = userId

                    observeLiveData(
                        repository.getUserNotes(userId),
                        ::handleNotesChanged
                    )

                    refreshNotesFromBackend()
                }

                is AppResult.Failure -> {
                    _Note_state.value = NoteState.Error("Failed to load current user.")
                }
            }
        }
    }

    fun refreshNotesFromBackend() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.refreshNotesFromBackend()
            } catch (e: Exception) {
                _Note_state.value = NoteState.Error("Failed to load notes: ${e.message}")
            }
        }
    }

    fun syncNotesToBackend() {
        refreshNotesFromBackend()
    }

    private fun handleNotesChanged(noteDbs: List<NoteDb>) {
        val sortedList = noteDbs.sortedByDescending {
            it.dateUpdate ?: it.dateCreate
        }

        val filteredList = sortedList
            .filter { !it.isDeletedNote }
            .toNoteModelList()

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

        _notes.value = updatedNotes
    }

    fun deleteNote(noteList: List<NoteModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                noteList.forEach { note ->
                    repository.deleteNoteFromBackend(note.id)
                }

                _Note_state.value = if (noteList.size == 1) {
                    NoteState.Success("Note was deleted!")
                } else {
                    NoteState.Success("Notes were deleted!")
                }
            } catch (e: Exception) {
                _Note_state.value = NoteState.Error("Failed to delete note: ${e.message}")
            }
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