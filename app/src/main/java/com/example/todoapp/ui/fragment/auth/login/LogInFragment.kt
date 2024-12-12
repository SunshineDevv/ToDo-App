package com.example.todoapp.ui.fragment.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentLogInBinding
import com.example.todoapp.ui.fragment.note.NoteViewModel

class LogInFragment : Fragment() {

    private var binding : FragmentLogInBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogInBinding.inflate(layoutInflater, container, false)
        return inflater.inflate(R.layout.fragment_log_in, container, false)
    }
}