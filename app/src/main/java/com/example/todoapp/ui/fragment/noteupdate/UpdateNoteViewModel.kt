package com.example.todoapp.ui.fragment.noteupdate

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.database.AppDatabase
import com.example.todoapp.database.model.NoteDb
import com.example.todoapp.database.repository.NoteRepository
import com.example.todoapp.extensions.toDateInMillis
import com.example.todoapp.ui.fragment.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateNoteViewModel @Inject constructor(private val repository: NoteRepository): ViewModel() {

    val nameNote = MutableLiveData<String>()
    val textNote = MutableLiveData<String>()

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    fun updateNote(
        idNote: Long,
        nameNote: String,
        textNote: String,
        dateCreateNote: String,
        dateUpdateNote: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsert(
                NoteDb(
                    id = idNote,
                    noteName = nameNote,
                    noteText = textNote,
                    dateCreate = dateCreateNote.toDateInMillis(),
                    dateUpdate = dateUpdateNote.toDateInMillis()
                )
            )
        }
        _state.postValue(State.Success("Note was updated!"))
    }

    fun clearState() {
        _state.value = State.Empty
    }
}