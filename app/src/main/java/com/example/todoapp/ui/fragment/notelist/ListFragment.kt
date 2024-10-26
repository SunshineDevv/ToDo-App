package com.example.todoapp.ui.fragment.notelist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.window.layout.WindowMetricsCalculator
import com.example.todoapp.ui.fragment.noteaction.NoteModel
import com.example.todoapp.R
import com.example.todoapp.database.AppDatabase
import com.example.todoapp.databinding.FragmentListBinding
import com.example.todoapp.ui.adapter.notelist.ListAdapter

class ListFragment : Fragment(), ListAdapter.RecyclerItemClicked {

    private var binding: FragmentListBinding? = null

    private var noteList: List<NoteModel> = listOf()

    private lateinit var listAdapter: ListAdapter

    private lateinit var database: AppDatabase

    private val noteListViewModel: NoteListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbarMenu()

        noteListViewModel.onStart(requireContext())

        listAdapter = ListAdapter(noteList,this)

        setupAdaptiveLayout()

        binding?.recyclerView?.adapter = listAdapter

        noteListViewModel.notes.observe(viewLifecycleOwner){ notes ->
            listAdapter.updateContactList(notes)
        }

        database = AppDatabase.getDatabase(requireContext())

        binding?.addButton?.setOnClickListener {
            findNavController().navigate(R.id.navigate_listFragment_to_noteFragment)
        }
    }

    private fun setupAdaptiveLayout(){
        val windowSizeClass = computeWindowSizeClasses()

        val spanCount = when (windowSizeClass) {
            WindowWidthSizeClass.Compact -> 2
            WindowWidthSizeClass.Medium -> 3
            WindowWidthSizeClass.Expanded -> 4
            else -> {
                2
            }
        }

        binding?.recyclerView?.layoutManager = GridLayoutManager(requireContext(), spanCount)
    }

    private fun setupToolbarMenu(){
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu_listfragment,menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                return true
            }
        }, viewLifecycleOwner)
    }

    private fun computeWindowSizeClasses(): WindowWidthSizeClass {
        val metrics =
            WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(requireActivity())
        val width = metrics.bounds.width()
        val density = resources.displayMetrics.density
        val widthDp = width / density

        return getWindowSizeClass(widthDp)
    }

    private fun getWindowSizeClass(dpWidth: Float): WindowWidthSizeClass {
        return when {
            dpWidth < 600 -> WindowWidthSizeClass.Compact
            dpWidth < 840 -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        }
    }

    override fun onLongClickedItem(note: NoteModel) {
        noteListViewModel.deleteNote(note)
        Toast.makeText(requireContext(),"Note was deleted!", Toast.LENGTH_LONG).show()
    }
}