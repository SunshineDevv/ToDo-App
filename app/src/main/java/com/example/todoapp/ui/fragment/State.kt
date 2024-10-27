package com.example.todoapp.ui.fragment

sealed class State {
    data class Success(val successMsg: String) : State()
    data class Error(val errorMsg: String?) : State()
    data object Empty : State()
}