package com.example.todoapp.ui.fragment.note

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.database.AppDatabase
import com.example.todoapp.database.model.NoteDb
import com.example.todoapp.database.repository.NoteRepository
import com.example.todoapp.ui.fragment.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel : ViewModel() {

    private lateinit var repository: NoteRepository

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    fun onStart(context: Context) {
        val contactDao = AppDatabase.getDatabase(context).getNoteDao()
        repository = NoteRepository(contactDao)
    }

    fun addNote(nameNote: String, textNote: String, dateCreateNote: Long, dateUpdateNote: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsert(
                NoteDb(
                    noteName = nameNote,
                    noteText = textNote,
                    dateCreate = dateCreateNote,
                    dateUpdate = dateUpdateNote
                )
            )
        }
        _state.postValue(State.Success("New note was added!"))
    }

    fun clearState() {
        _state.value = State.Empty
    }
}