package com.example.todoapp.ui.fragment.noteupdate

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.R
import com.example.todoapp.database.AppDatabase
import com.example.todoapp.database.model.NoteDb
import com.example.todoapp.database.repository.NoteRepository
import com.example.todoapp.extensions.toDateInMillis
import com.example.todoapp.ui.fragment.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateNoteViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    val nameNote = MutableStateFlow("")
    val textNote = MutableStateFlow("")

    private val _state = MutableStateFlow<State>(State.Empty)
    val state: StateFlow<State> = _state

    private val _isColorsVisible = MutableStateFlow(false)
    val isColorsVisible: StateFlow<Boolean> = _isColorsVisible

    private val _buttonColors = MutableStateFlow(
        listOf(
            1 to R.drawable.button_background_yellow,
            2 to R.drawable.button_background_light_blue,
            3 to R.drawable.button_background_pink,
            4 to R.drawable.button_background_orange
        )
    )
    val buttonColors: StateFlow<List<Pair<Int, Int>>> = _buttonColors

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

    private val _layoutBackgroundColor = MutableStateFlow(R.drawable.rounded_background_orange)
    val layoutBackgroundColor: StateFlow<Int> = _layoutBackgroundColor

    private val _editTextBackgroundColor = MutableStateFlow(R.drawable.rounded_background_orange)
    val editTextBackgroundColor: StateFlow<Int> = _editTextBackgroundColor

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

    fun setInitialButtonColor(noteColor: String) {
        val drawableResId = getDrawableResIdByName(noteColor)
        // Перемещаем выбранный цвет в 4-ю позицию
        _buttonColors.value = _buttonColors.value.toMutableList().apply {
            val currentColors = this.map { it.second } // Текущие цвета кнопок
            val reorderedColors = currentColors
                .filter { it != drawableResId } // Убираем выбранный цвет
                .toMutableList()
            // Устанавливаем выбранный цвет в позицию 4
            if (drawableResId != null) {
                reorderedColors.add(drawableResId)
            }
            // Перезаписываем список цветов
            reorderedColors.forEachIndexed { index, color ->
                this[index] = this[index].copy(second = color)
            }
        }
        // Обновляем фоны
        if (drawableResId != null) {
            updateBackgroundsFromButton(drawableResId)
        }
    }

    private val resourceNameToDrawableMap = mapOf(
        "button_background_orange" to R.drawable.button_background_orange,
        "button_background_yellow" to R.drawable.button_background_yellow,
        "button_background_pink" to R.drawable.button_background_pink,
        "button_background_light_blue" to R.drawable.button_background_light_blue
    )

    private fun getDrawableResIdByName(resourceName: String): Int? {
        return resourceNameToDrawableMap[resourceName]
    }

    fun updateNote(
        idNote: Long,
        nameNote: String,
        textNote: String,
        dateCreateNote: String,
        dateUpdateNote: String,
        noteColor: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsert(
                NoteDb(
                    id = idNote,
                    noteName = nameNote,
                    noteText = textNote,
                    dateCreate = dateCreateNote.toDateInMillis(),
                    dateUpdate = dateUpdateNote.toDateInMillis(),
                    noteColor = noteColor
                )
            )
        }
        _state.value = State.Success("Note was updated!")
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