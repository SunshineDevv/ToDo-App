package com.example.todoapp.ui.fragment.note

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
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
import com.example.todoapp.ui.fragment.State
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
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

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbarMenu()

        initObservers()
        binding?.addNewNoteButton?.setOnClickListener {
            val dateCreateNote = System.currentTimeMillis()

            val nameNote = binding?.nameEditText?.text.toString()
            val textNote = binding?.textNoteEditText?.text.toString()

            val backgroundId = noteViewModel.buttonColors.value.find { it.first == 4 }?.second
            val backgroundResName = backgroundId?.let { it1 -> resources.getResourceName(it1) }
            val resourceName = backgroundResName?.substringAfter(":drawable/")

            if (resourceName != null) {
                noteViewModel.addNote(nameNote, textNote, dateCreateNote, 0,resourceName)
            }
            findNavController().navigate(R.id.navigate_noteFragment_to_listFragment)
        }
        val buttonSpacing = setupAdaptiveColorAnimation()

        binding?.mainButton?.setOnClickListener {
            noteViewModel.toggleColorsVisibility()
            if(noteViewModel.isColorsVisible.value){
                noteViewModel.setVisibleColor()
                colorVisibleAnimation(buttonSpacing)
            } else {
                noteViewModel.unsetVisibleColor()
                colorInvisibleAnimation()
            }
        }

        lifecycleScope.launch {
            noteViewModel.isColorsVisible.flowWithLifecycle(lifecycle).collectLatest {
                if (it){
                    colorVisibleAnimation(buttonSpacing)
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
                    is State.Error -> {}
                    else -> {}
                }
            }
        }
        lifecycleScope.launch {
            noteViewModel.buttonColors.flowWithLifecycle(lifecycle).collectLatest { colors ->
                binding?.apply {
                    colors.forEach { (position, drawableRes) ->
                        Log.d("ButtonColor", "position = $position drawablesRws = $drawableRes")
                        when (position) {
                            1 -> colorButton1.setBackgroundResource(drawableRes)
                            2 -> colorButton2.setBackgroundResource(drawableRes)
                            3 -> colorButton3.setBackgroundResource(drawableRes)
                            4 -> mainButton.setBackgroundResource(drawableRes)
                        }
                    }
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
            dpWidth < 400 -> WindowWidthSizeClass.Compact
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

    @SuppressLint("SoonBlockedPrivateApi", "UseCompatLoadingForDrawables")
    private fun colorVisibleAnimation(buttonSpacing: Int) {
        binding?.apply {
            colorButton1.visibility = View.VISIBLE
            colorButton2.visibility = View.VISIBLE
            colorButton3.visibility = View.VISIBLE

            ObjectAnimator.ofFloat(colorButton3, "translationX", -(buttonSpacing * 2).toFloat())
                .apply {
                    duration = 300
                    start()
                }
            ObjectAnimator.ofFloat(colorButton2, "translationX", -(buttonSpacing * 4).toFloat())
                .apply {
                    duration = 300
                    start()
                }
            ObjectAnimator.ofFloat(colorButton1, "translationX", -(buttonSpacing * 6).toFloat())
                .apply {
                    duration = 300
                    start()
                }
        }
        setupColorButtonListeners()
    }

    private fun setupColorButtonListeners() {
        binding?.apply {
            colorButton1.setOnClickListener {
                Log.d("ButtonColors", "colorButton1\n")
                noteViewModel.swapButtonColors(1, 4)
            }
            colorButton2.setOnClickListener {
                Log.d("ButtonColors", "colorButton2\n")
                noteViewModel.swapButtonColors(2, 4)
            }
            colorButton3.setOnClickListener {
                Log.d("ButtonColors", "colorButton3\n")
                noteViewModel.swapButtonColors(3, 4)
            }
        }
    }

    private fun colorInvisibleAnimation(){
        binding?.apply {

            colorButton1.isClickable = false
            colorButton2.isClickable = false
            colorButton3.isClickable = false
            mainButton.isClickable = false

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

            lifecycleScope.launch {
                delay(300L)
                colorButton1.visibility = View.GONE
                colorButton2.visibility = View.GONE
                colorButton3.visibility = View.GONE

                colorButton1.isClickable = true
                colorButton2.isClickable = true
                colorButton3.isClickable = true
                mainButton.isClickable = true
            }
        }
    }
}