package com.example.todoapp.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

fun <T : Any> Fragment.observe(flow: Flow<T?>, body: suspend (T?) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { value ->
                body.invoke(value)
            }
        }
    }
}

fun <T : Any> AppCompatActivity.observe(flow: Flow<T?>, body: suspend (T?) -> Unit) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { value ->
                body.invoke(value)
            }
        }
    }
}
