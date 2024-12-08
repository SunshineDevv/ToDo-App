package com.example.todoapp.ui.fragment.notelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.database.model.NoteDb
import com.example.todoapp.database.repository.NoteRepository
import com.example.todoapp.extensions.observeLiveData
import com.example.todoapp.extensions.toNoteDbModel
import com.example.todoapp.extensions.toNoteModelList
import com.example.todoapp.ui.fragment.State
import com.example.todoapp.ui.fragment.note.NoteModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<NoteModel>>(emptyList())
    val notes = _notes.asStateFlow()

    private val _state = MutableStateFlow<State>(State.Empty)
    val state = _state.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    fun onStart() {
        observeLiveData(repository.allNotes, ::handleNotesChanged)
    }

    private fun handleNotesChanged(noteDbs: List<NoteDb>) {
        val sortedList = noteDbs.sortedByDescending {
            it.dateUpdate ?: it.dateCreate
        }.toNoteModelList()
        _notes.value = sortedList
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
        viewModelScope.launch(Dispatchers.IO) {
            noteList.forEach { note ->
                repository.delete(note.toNoteDbModel())
            }
        }
        if (noteList.size == 1) {
            _state.value = State.Success("Note was deleted!")
        } else {
            _state.value = State.Success("Notes were deleted!")
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