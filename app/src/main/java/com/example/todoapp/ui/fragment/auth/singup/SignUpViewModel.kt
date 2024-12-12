package com.example.todoapp.ui.fragment.auth.singup

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(

): ViewModel(){

    val userName = MutableStateFlow("")
    val userEmail = MutableStateFlow("")
    val userPassword = MutableStateFlow("")
    val userConfirmPassword = MutableStateFlow("")
}