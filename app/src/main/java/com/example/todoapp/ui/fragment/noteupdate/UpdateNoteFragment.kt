package com.example.todoapp.ui.fragment.noteupdate

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentUpdateNoteBinding
import com.example.todoapp.extensions.toFormattedDate
import com.example.todoapp.ui.fragment.State
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdateNoteFragment : Fragment() {

    private var binding: FragmentUpdateNoteBinding? = null

    private val args: UpdateNoteFragmentArgs by navArgs()

    private val updateNoteViewModel: UpdateNoteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_update_note,container,false)
        binding?.viewmodel = updateNoteViewModel
        binding?.lifecycleOwner = viewLifecycleOwner
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()

        val idNote = args.idNote
        val nameNote = args.nameNote
        val textNote = args.textNote
        val dateCreateNote = args.dateCrateNote

        if (updateNoteViewModel.nameNote.value.isNullOrEmpty() &&
            updateNoteViewModel.textNote.value.isNullOrEmpty()
        ) {
            setExistingData(nameNote, textNote)
        }

        binding?.updateNoteButton?.setOnClickListener {
            val newNameNote = binding?.nameEditText?.text.toString()
            val newTextNote = binding?.textNoteEditText?.text.toString()
            val dateUpdateNote = System.currentTimeMillis().toFormattedDate()

            updateNoteViewModel.updateNote(
                idNote,
                newNameNote,
                newTextNote,
                dateCreateNote,
                dateUpdateNote,
                R.color.orange.toString()
            )

            findNavController().navigate(R.id.navigate_updateNoteFragment_to_listFragment)
        }
    }

    private fun setExistingData(nameNote: String, textNote: String) {
        updateNoteViewModel.nameNote.value = nameNote
        updateNoteViewModel.textNote.value = textNote
    }

    private fun initObservers() {
        lifecycleScope.launch {
            updateNoteViewModel.state.flowWithLifecycle(lifecycle).collectLatest { state ->
                when (state) {
                    is State.Success -> {
                        Toast.makeText(requireContext(), state.successMsg, Toast.LENGTH_SHORT).show()
                        updateNoteViewModel.clearState()
                    }

                    is State.Error -> {

                    }

                    else -> {}
                }
            }
        }
    }
}