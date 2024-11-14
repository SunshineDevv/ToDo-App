package com.example.todoapp.ui.fragment.notelist

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.database.AppDatabase
import com.example.todoapp.database.model.NoteDb
import com.example.todoapp.database.repository.NoteRepository
import com.example.todoapp.extensions.observeLiveData
import com.example.todoapp.extensions.toNoteDbModel
import com.example.todoapp.extensions.toNoteModelList
import com.example.todoapp.ui.fragment.State
import com.example.todoapp.ui.fragment.note.NoteModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NoteListViewModel : ViewModel() {

    private lateinit var repository: NoteRepository

    private val _notes = MutableLiveData<List<NoteModel>>(emptyList())
    val notes: LiveData<List<NoteModel>> = _notes

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private val _isSelectionMode = MutableLiveData<Boolean>()
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode

    fun onStart(context: Context) {
        Log.i("CHECK_LOG", "observer heita ili ya")
        val contactDao = AppDatabase.getDatabase(context).getNoteDao()
        repository = NoteRepository(contactDao)
        observeLiveData(repository.allNotes, ::handleNotesChanged)
    }

    private fun handleNotesChanged(noteDbs: List<NoteDb>) {
        Log.i("CHECK_LOG", "observer heita")
        val sortedList = noteDbs.sortedByDescending {
            it.dateUpdate ?: it.dateCreate
        }.toNoteModelList()
        _notes.postValue(sortedList)
    }

    fun setSelected(note: NoteModel) {
        val updatedNotes = _notes.value?.map {
            if (it.id == note.id) {
                it.copy(isSelected = MutableStateFlow(!it.isSelected.value))
            } else {
                it
            }
        }
        _notes.postValue(updatedNotes ?: emptyList())
    }

    fun deleteNote(noteList: List<NoteModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            noteList.forEach { note ->
                repository.delete(note.toNoteDbModel())
            }
        }
        _state.postValue(State.Success("Note was deleted!"))
    }

    fun clearState() {
        _state.value = State.Empty
    }

    fun enableSelectionMode() {
        _isSelectionMode.value = true
        Log.i("SELECTIONN", "Enable: ${_isSelectionMode.value}")
    }

    fun disableSelectionMode() {
        _isSelectionMode.value = false
        Log.i("SELECTIONN", "Disable: ${_isSelectionMode.value}")

    }

}