package com.example.todoapp.ui.fragment.noteaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentNoteBinding


class NoteFragment : Fragment() {

    private var binding: FragmentNoteBinding? = null

    private val noteViewModel: NoteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoteBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbarMenu()

        noteViewModel.onStart(requireContext())

        binding?.addNewNoteButton?.setOnClickListener {
            val dateCreateNote = System.currentTimeMillis()

            val nameNote = binding?.nameEditText?.text.toString()
            val textNote = binding?.textNoteEditText?.text.toString()

            noteViewModel.addNote(nameNote, textNote, dateCreateNote, 0)
            Toast.makeText(requireContext(),"New note was added!", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.navigate_noteFragment_to_listFragment)
        }
    }

    private fun setupToolbarMenu(){
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu_notefragment,menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                return true
            }
        }, viewLifecycleOwner)
    }
}