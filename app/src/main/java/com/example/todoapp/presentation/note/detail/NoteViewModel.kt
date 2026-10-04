package com.example.todoapp.presentation.note.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.R
import com.example.todoapp.data.note.repository.NoteRepository
import com.example.todoapp.presentation.note.state.NoteState
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

    val nameNote = MutableStateFlow("")
    val textNote = MutableStateFlow("")

    private val _Note_state = MutableStateFlow<NoteState>(NoteState.Empty)
    val state = _Note_state.asStateFlow()

    private val _isColorsVisible = MutableStateFlow(false)
    val isColorsVisible = _isColorsVisible.asStateFlow()

    private val _buttonColors = MutableStateFlow(
        listOf(
            1 to R.drawable.button_background_green,
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
        R.drawable.button_background_green to R.drawable.rounded_background_green,
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

            if (index1 in indices && index2 in indices) {
                val tempValue = this[index1].second
                this[index1] = this[index1].copy(second = this[index2].second)
                this[index2] = this[index2].copy(second = tempValue)
            }
        }
    }

    fun addNote(
        noteId: String,
        nameNote: String,
        textNote: String,
        noteColor: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.createNote(
                    id = noteId,
                    noteName = nameNote,
                    noteText = textNote,
                    noteColor = noteColor
                )

                _Note_state.value = NoteState.Success("New note was added!")
            } catch (e: Exception) {
                _Note_state.value = NoteState.Error("Failed to add note: ${e.message}")
            }
        }
    }

    fun clearState() {
        _Note_state.value = NoteState.Empty
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