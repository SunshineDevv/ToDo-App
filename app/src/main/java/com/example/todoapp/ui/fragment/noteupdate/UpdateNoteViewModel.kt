package com.example.todoapp.ui.fragment.noteupdate

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.database.AppDatabase
import com.example.todoapp.database.model.NoteDb
import com.example.todoapp.database.repository.NoteRepository
import com.example.todoapp.extensions.toDateInMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateNoteViewModel : ViewModel() {

    private lateinit var repository: NoteRepository

    fun onStart(context: Context) {
        val contactDao = AppDatabase.getDatabase(context).getNoteDao()
        repository = NoteRepository(contactDao)
    }

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

    }

}