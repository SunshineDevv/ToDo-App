package com.example.todoapp.ui.fragment.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.R
import com.example.todoapp.database.model.NoteDb
import com.example.todoapp.database.repository.NoteRepository
import com.example.todoapp.ui.fragment.State
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository,
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    val nameNote = MutableStateFlow("")
    val textNote = MutableStateFlow("")

    private val _state = MutableStateFlow<State>(State.Empty)
    val state = _state.asStateFlow()

    private val _isColorsVisible = MutableStateFlow(false)
    val isColorsVisible = _isColorsVisible.asStateFlow()

    private val _buttonColors = MutableStateFlow(
        listOf(
            1 to R.drawable.button_background_yellow,
            2 to R.drawable.button_background_light_blue,
            3 to R.drawable.button_background_pink,
            4 to R.drawable.button_background_orange
        )
    )
    val buttonColors = _buttonColors.asStateFlow()

    private val _layoutBackgroundColor = MutableStateFlow(R.drawable.rounded_background_orange)
    val layoutBackgroundColor = _layoutBackgroundColor.asStateFlow()

    private val _editTextBackgroundColor = MutableStateFlow(R.drawable.rounded_background_orange)
    val editTextBackgroundColor = _editTextBackgroundColor.asStateFlow()

    private val buttonToBackgroundMap = mapOf(
        R.drawable.button_background_yellow to R.drawable.rounded_background_yellow,
        R.drawable.button_background_light_blue to R.drawable.rounded_background_light_blue,
        R.drawable.button_background_pink to R.drawable.rounded_background_pink,
        R.drawable.button_background_orange to R.drawable.rounded_background_orange
    )

    fun updateBackgroundsFromButton(buttonDrawableRes: Int) {
        val newBackgroundRes =
            buttonToBackgroundMap[buttonDrawableRes] ?: R.drawable.rounded_background_orange
        _layoutBackgroundColor.value = newBackgroundRes
        _editTextBackgroundColor.value = newBackgroundRes
    }

    fun swapButtonColors(position1: Int, position2: Int) {
        _buttonColors.value = _buttonColors.value.toMutableList().apply {
            val index1 = position1 - 1
            val index2 = position2 - 1

            if (index1 != -1 && index2 != -1) {
                val tempValue = this[index1].second
                this[index1] = this[index1].copy(second = this[index2].second)
                this[index2] = this[index2].copy(second = tempValue)
            }
        }
    }

    fun addNote(
        noteId: String,
        userOwnerId: String,
        nameNote: String,
        textNote: String,
        dateCreateNote: Long,
        dateUpdateNote: Long,
        noteColor: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsert(
                NoteDb(
                    id = noteId,
                    userOwnerId = userOwnerId,
                    noteName = nameNote,
                    noteText = textNote,
                    dateCreate = dateCreateNote,
                    dateUpdate = dateUpdateNote,
                    noteColor = noteColor
                )
            )
        }
        _state.value = State.Success("New note was added!")
    }

    fun addNoteToFirestore(
        noteId: String,
        noteOwnerId: String,
        nameNote: String,
        textNote: String,
        dateCreateNote: Long,
        dateUpdateNote: Long,
        noteColor: String
    ) {
        val note = hashMapOf(
            "nameNote" to nameNote,
            "textNote" to textNote,
            "dateCreateNote" to dateCreateNote,
            "dateUpdateNote" to dateUpdateNote,
            "noteColor" to noteColor
        )
        firestore.collection("users")
            .document(noteOwnerId)
            .collection("notes")
            .document(noteId)
            .set(note)
            .addOnSuccessListener {
                _state.value = State.Success("Note added to Firestore!")
            }
            .addOnFailureListener {
                _state.value = State.Error("Failed to add note: ${it.message}")
            }
    }

    fun clearState() {
        _state.value = State.Empty
    }

    fun setVisibleColor() {
        _isColorsVisible.value = true
    }

    fun unsetVisibleColor() {
        _isColorsVisible.value = false
    }

    fun toggleColorsVisibility() {
        _isColorsVisible.value = !_isColorsVisible.value
    }
}