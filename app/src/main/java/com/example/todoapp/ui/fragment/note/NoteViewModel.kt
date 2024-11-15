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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    val nameNote = MutableStateFlow("")
    val textNote = MutableStateFlow("")

    private val _state = MutableStateFlow<State>(State.Empty)
    val state: MutableStateFlow<State> = _state

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
        _state.value = State.Success("New note was added!")
    }

    fun clearState() {
        _state.value = State.Empty
    }
}