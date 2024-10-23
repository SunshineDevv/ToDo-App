package com.example.todoapp.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun <T : Any> ViewModel.observeLiveData(liveData: LiveData<T>, result: (T) -> Unit) {
    liveData.observeForever { value ->
        value.let {
            viewModelScope.launch(Dispatchers.IO) {
                result.invoke(it)
            }
        }
    }
}