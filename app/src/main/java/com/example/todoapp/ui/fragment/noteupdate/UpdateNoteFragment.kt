package com.example.todoapp.ui.fragment.noteupdate

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentUpdateNoteBinding
import com.example.todoapp.extensions.toFormattedDate
import com.example.todoapp.ui.fragment.State

class UpdateNoteFragment : Fragment() {

    private var binding: FragmentUpdateNoteBinding? = null

    private val args: UpdateNoteFragmentArgs by navArgs()

    private val updateNoteViewModel: UpdateNoteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUpdateNoteBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateNoteViewModel.onStart(requireContext())

        initObservers()

        val idNote = args.idNote
        val nameNote = args.nameNote
        val textNote = args.textNote
        val dateCreateNote = args.dateCrateNote
        setExistingData(nameNote, textNote)

        binding?.updateNoteButton?.setOnClickListener {
            val newNameNote = binding?.nameEditText?.text.toString()
            val newTextNote = binding?.textNoteEditText?.text.toString()
            val dateUpdateNote = System.currentTimeMillis().toFormattedDate()

            updateNoteViewModel.updateNote(
                idNote,
                newNameNote,
                newTextNote,
                dateCreateNote,
                dateUpdateNote
            )

            findNavController().navigate(R.id.navigate_updateNoteFragment_to_listFragment)
        }

    }

    private fun setExistingData(nameNote: String, textNote: String) {
        binding?.nameEditText?.setText(nameNote)
        binding?.textNoteEditText?.setText(textNote)
    }

    private fun initObservers() {
        updateNoteViewModel.state.observe(viewLifecycleOwner) { state ->
            when(state){
                is State.Success -> {
                    Toast.makeText(requireContext(), state.successMsg, Toast.LENGTH_LONG).show()
                    updateNoteViewModel.clearState()
                }
                is State.Error -> {

                }
                else -> { }
            }
        }
    }

}