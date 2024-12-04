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
import androidx.activity.OnBackPressedCallback
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.window.layout.WindowMetricsCalculator
import com.example.todoapp.ui.fragment.note.NoteModel
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentListBinding
import com.example.todoapp.extensions.observe
import com.example.todoapp.ui.adapter.notelist.ListAdapter
import com.example.todoapp.ui.fragment.State
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListFragment : Fragment(), ListAdapter.RecyclerItemClicked {

    private var binding: FragmentListBinding? = null

    private lateinit var listAdapter: ListAdapter

    private val noteListViewModel: NoteListViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressed()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            noteListViewModel.onStart()
        }

        startWork()

        subscribeToObservables()

        binding?.actionButton?.setImageResource(R.drawable.baseline_add_52)

        binding?.actionButton?.setOnClickListener {
            if (noteListViewModel.isSelectionMode.value) {
                deleteSelectedNotes()
                listAdapter.exitSelectionMode()
                noteListViewModel.disableSelectionMode()
            } else {
                findNavController().navigate(R.id.navigate_listFragment_to_noteFragment)
            }
        }
    }

    private fun startWork() {
        setupToolbarMenu()
        initObservers()
        listAdapter = ListAdapter(this)
        setupAdaptiveLayout()
        binding?.recyclerView?.adapter = listAdapter
    }

    private fun setupAdaptiveLayout() {
        val windowSizeClass = computeWindowSizeClasses()

        val spanCount = when (windowSizeClass) {
            WindowWidthSizeClass.Compact -> 2
            WindowWidthSizeClass.Medium -> 3
            WindowWidthSizeClass.Expanded -> 4
            else -> {
                2
            }
        }

        val gridLayoutManager = GridLayoutManager(requireContext(), spanCount)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) spanCount else 1
            }
        }

        binding?.recyclerView?.layoutManager = gridLayoutManager
    }

    private fun setupToolbarMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu_listfragment, menu)
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

    private fun initObservers() {
        lifecycleScope.launch {
            noteListViewModel.state.flowWithLifecycle(lifecycle).collectLatest { state ->
                when (state) {
                    is State.Success -> {
                        Toast.makeText(requireContext(), state.successMsg, Toast.LENGTH_SHORT).show()
                        noteListViewModel.clearState()
                    }

                    is State.Error -> {

                    }

                    else -> {}
                }
            }
        }
    }

    private fun subscribeToObservables(){
        lifecycleScope.launch {
            noteListViewModel.notes.flowWithLifecycle(lifecycle).collectLatest { notes ->
                listAdapter.updateNoteList(notes)
            }
        }

        lifecycleScope.launch {
            noteListViewModel.isSelectionMode.flowWithLifecycle(lifecycle)
                .collectLatest { isSelectionMode ->
                    listAdapter.setSelectionMode(isSelectionMode)
                    if (isSelectionMode) {
                        binding?.actionButton?.setImageResource(R.drawable.baseline_delete_24)
                    } else {
                        binding?.actionButton?.setImageResource(R.drawable.baseline_add_52)
                    }
                }
        }
    }

    private fun deleteSelectedNotes() {
        val selectedNotes = listAdapter.getSelectedItems()
        noteListViewModel.deleteNote(selectedNotes)
    }

    override fun onClickedItem(note: NoteModel) {
        val idNote = note.id
        val nameNote = note.noteName.toString()
        val textNote = note.noteText.toString()
        val dateCrateNote = note.noteDateCreate.toString()
        val action = ListFragmentDirections.navigateListFragmentToUpdateNoteFragment(
            nameNote,
            textNote,
            idNote,
            dateCrateNote
        )
        view?.let { Navigation.findNavController(it).navigate(action) }
    }

    override fun onLongClickedItem(note: NoteModel) {
        noteListViewModel.enableSelectionMode()
        noteListViewModel.isSelectionMode.value.let { listAdapter.setSelectionMode(it) }
    }

    override fun isCheckedItem(note: NoteModel) {
        noteListViewModel.setSelected(note)
    }

    private fun onBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                listAdapter.exitSelectionMode()
                noteListViewModel.disableSelectionMode()
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(
            this,
            callback
        )
    }
}