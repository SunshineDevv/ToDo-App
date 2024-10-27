package com.example.todoapp.ui.fragment.notelist

import android.content.Context
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
import kotlinx.coroutines.launch

class NoteListViewModel : ViewModel() {

    private lateinit var repository: NoteRepository

    private val _notes = MutableLiveData<List<NoteModel>>(emptyList())
    val notes: LiveData<List<NoteModel>> = _notes

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    fun onStart(context: Context) {
        val contactDao = AppDatabase.getDatabase(context).getNoteDao()
        repository = NoteRepository(contactDao)
        observeLiveData(repository.allNotes, ::handleNotesChanged)
    }

    private fun handleNotesChanged(noteDbs: List<NoteDb>) {
        val sortedList = noteDbs.sortedByDescending {
            it.dateUpdate ?: it.dateCreate
        }.toNoteModelList()
        _notes.postValue(sortedList)
    }

    fun deleteNote(contact: NoteModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(contact.toNoteDbModel())
        }
        _state.postValue(State.Success("Note was deleted!"))
    }

    fun clearState() {
        _state.value = State.Empty
    }
}