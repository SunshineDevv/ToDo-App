package com.example.todoapp.ui.fragment.note

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.window.layout.WindowMetricsCalculator
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentNoteBinding
import com.example.todoapp.extensions.observe
import com.example.todoapp.ui.fragment.State
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteFragment : Fragment() {

    private var binding: FragmentNoteBinding? = null

    private val noteViewModel: NoteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_note, container, false)
        binding?.viewmodel = noteViewModel
        binding?.lifecycleOwner = viewLifecycleOwner
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbarMenu()

        initObservers()
        binding?.addNewNoteButton?.setOnClickListener {
            val dateCreateNote = System.currentTimeMillis()

            val nameNote = binding?.nameEditText?.text.toString()
            val textNote = binding?.textNoteEditText?.text.toString()

            noteViewModel.addNote(nameNote, textNote, dateCreateNote, 0)
            findNavController().navigate(R.id.navigate_noteFragment_to_listFragment)
        }
        val buttonSpacing = setupAdaptiveColorAnimation()

        binding?.mainButton?.setOnClickListener {
            noteViewModel.toggleColorsVisibility()
            Log.i("BUTTONVISIBLE", "before IF ${noteViewModel.isColorsVisible.value}")
            if(noteViewModel.isColorsVisible.value){
                noteViewModel.setVisibleColor()
                Log.i("BUTTONVISIBLE", "after set ${noteViewModel.isColorsVisible.value}")
                colorVisible(buttonSpacing)
            } else {
                noteViewModel.unsetVisibleColor()
                Log.i("BUTTONVISIBLE", "after unset ${noteViewModel.isColorsVisible.value}")
                colorInvisible()
            }
            Log.i("BUTTONVISIBLE", "after toggle ${noteViewModel.isColorsVisible.value}")
        }

        lifecycleScope.launch {
            noteViewModel.isColorsVisible.flowWithLifecycle(lifecycle).collectLatest {
                if (it){
                    colorVisible(buttonSpacing)
                }
            }
        }

    }

    private fun setupToolbarMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu_notefragment, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                return true
            }
        }, viewLifecycleOwner)
    }

    private fun initObservers() {
        lifecycleScope.launch {
            noteViewModel.state.flowWithLifecycle(lifecycle).collectLatest { state ->
                when (state) {
                    is State.Success -> {
                        Toast.makeText(requireContext(), state.successMsg, Toast.LENGTH_SHORT)
                            .show()
                        noteViewModel.clearState()
                    }

                    is State.Error -> {

                    }

                    else -> {}
                }
            }
        }
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
            dpWidth < 300 -> WindowWidthSizeClass.Compact
            dpWidth < 600 -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        }
    }

    private fun setupAdaptiveColorAnimation(): Int{
        val windowSizeClass = computeWindowSizeClasses()
        val buttonSpacing = when (windowSizeClass) {
            WindowWidthSizeClass.Compact -> 50
            WindowWidthSizeClass.Medium -> 100
            WindowWidthSizeClass.Expanded -> 150
            else -> {
                100
            }
        }
        return buttonSpacing
    }

    private fun colorVisible(buttonSpacing: Int) {
        val background = binding?.mainButton?.background as GradientDrawable
        background.setColor(Color.parseColor("#E8774E"))
        binding?.apply {
            colorButton1.visibility = View.VISIBLE
            colorButton2.visibility = View.VISIBLE
            colorButton3.visibility = View.VISIBLE

            setButtonsBackground()

            ObjectAnimator.ofFloat(colorButton1, "translationX", -(buttonSpacing * 2).toFloat())
                .apply {
                    duration = 300
                    start()
                }
            ObjectAnimator.ofFloat(colorButton2, "translationX", -(buttonSpacing * 4).toFloat())
                .apply {
                    duration = 300
                    start()
                }
            ObjectAnimator.ofFloat(colorButton3, "translationX", -(buttonSpacing * 6).toFloat())
                .apply {
                    duration = 300
                    start()
                }
        }

        binding?.apply {
            colorButton1.setOnClickListener {

            }
            colorButton2.setOnClickListener {

            }
            colorButton3.setOnClickListener {

            }
        }
    }

    private fun colorInvisible(){
        binding?.apply {

            ObjectAnimator.ofFloat(colorButton1, "translationX", 0f)
                .apply {
                    duration = 300
                    start()
                }
            ObjectAnimator.ofFloat(colorButton2, "translationX", 0f)
                .apply {
                    duration = 300
                    start()
                }
            ObjectAnimator.ofFloat(colorButton3, "translationX", 0f)
                .apply {
                    duration = 300
                    start()
                }

            colorButton1.visibility = View.GONE
            colorButton2.visibility = View.GONE
            colorButton3.visibility = View.GONE

        }
    }

    private fun setButtonsBackground(){
        binding?.apply {
            val background1 = colorButton1.background as GradientDrawable
            val background2 = colorButton2.background as GradientDrawable
            val background3 = colorButton3.background as GradientDrawable

            background1.setColor(Color.parseColor("#F8D34E"))
            background2.setColor(Color.parseColor("#98B8DE"))
            background3.setColor(Color.parseColor("#FFA6DB"))
        }
    }
}