package com.example.todoapp.ui.fragment.noteupdate

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.window.layout.WindowMetricsCalculator
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentUpdateNoteBinding
import com.example.todoapp.extensions.toFormattedDate
import com.example.todoapp.ui.fragment.State
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_update_note, container, false)
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
        val noteColor = args.noteColor

        if (updateNoteViewModel.nameNote.value.isEmpty() &&
            updateNoteViewModel.textNote.value.isEmpty()
        ) {
            setExistingData(nameNote, textNote, noteColor)
        }

        binding?.updateNoteButton?.setOnClickListener {
            val newNameNote = binding?.nameEditText?.text.toString()
            val newTextNote = binding?.textNoteEditText?.text.toString()
            val dateUpdateNote = System.currentTimeMillis().toFormattedDate()
            val resourceName = getResourceName()

            if (resourceName != null) {
                updateNoteViewModel.updateNote(
                    idNote,
                    newNameNote,
                    newTextNote,
                    dateCreateNote,
                    dateUpdateNote,
                    resourceName
                )
            }
            findNavController().navigate(R.id.navigate_updateNoteFragment_to_listFragment)
        }

        val buttonSpacing = setupAdaptiveColorAnimation()

        checkColorsVisibility(buttonSpacing)

        enableScrollEditTextInScrollView()

        enableAdaptiveSizeOfButtons(buttonSpacing)
    }

    private fun setExistingData(nameNote: String, textNote: String, noteColor: String) {
        updateNoteViewModel.nameNote.value = nameNote
        updateNoteViewModel.textNote.value = textNote
        updateNoteViewModel.setInitialButtonColor(noteColor)
    }

    private fun initObservers() {
        lifecycleScope.launch {
            updateNoteViewModel.state.flowWithLifecycle(lifecycle).collectLatest { state ->
                when (state) {
                    is State.Success -> {
                        Toast.makeText(requireContext(), state.successMsg, Toast.LENGTH_SHORT)
                            .show()
                        updateNoteViewModel.clearState()
                    }

                    is State.Error -> {}
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            updateNoteViewModel.buttonColors.flowWithLifecycle(lifecycle).collectLatest { colors ->
                binding?.apply {
                    colors.forEach { (position, drawableRes) ->
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

        lifecycleScope.launch {
            updateNoteViewModel.layoutBackgroundColor.flowWithLifecycle(lifecycle)
                .collectLatest { backgroundRes ->
                    binding?.linearLayout?.setBackgroundResource(backgroundRes)
                }
        }

        lifecycleScope.launch {
            updateNoteViewModel.editTextBackgroundColor.flowWithLifecycle(lifecycle)
                .collectLatest { backgroundRes ->
                    binding?.nameEditText?.setBackgroundResource(backgroundRes)
                    binding?.textNoteEditText?.setBackgroundResource(backgroundRes)
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
            dpWidth < 350 -> WindowWidthSizeClass.Compact
            dpWidth < 600 -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        }
    }

    private fun setupAdaptiveColorAnimation(): Int {
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

            enableButtonsVisibility()

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
            setupColorButtonListeners()
        }
    }

    private fun colorInvisibleAnimation() {
        binding?.apply {

            disableButtonsClickable()

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

                disableButtonsVisibility()

                enableButtonsClickable()
            }
        }
    }

    private fun setupColorButtonListeners() {
        binding?.apply {
            colorButton1.setOnClickListener {
                updateNoteViewModel.swapButtonColors(1, 4)
                updateBackgrounds()
            }
            colorButton2.setOnClickListener {
                updateNoteViewModel.swapButtonColors(2, 4)
                updateBackgrounds()
            }
            colorButton3.setOnClickListener {
                updateNoteViewModel.swapButtonColors(3, 4)
                updateBackgrounds()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableScrollEditTextInScrollView() {
        binding?.textNoteEditText?.setOnTouchListener { v, event ->
            if (binding?.textNoteEditText?.hasFocus() == true) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_SCROLL -> {
                        v.parent.requestDisallowInterceptTouchEvent(false)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun enableAdaptiveSizeOfButtons(buttonSpacing: Int) {
        binding?.apply {
            if (buttonSpacing == 150) {
                val layoutParamsMain = mainButton.layoutParams
                layoutParamsMain.width = 120
                layoutParamsMain.height = 120
                mainButton.layoutParams = layoutParamsMain

                val layoutParamsColor1 = colorButton1.layoutParams
                layoutParamsColor1.width = 120
                layoutParamsColor1.height = 120
                colorButton1.layoutParams = layoutParamsColor1

                val layoutParamsColor2 = colorButton2.layoutParams
                layoutParamsColor2.width = 120
                layoutParamsColor2.height = 120
                colorButton2.layoutParams = layoutParamsColor2

                val layoutParamsColor3 = colorButton3.layoutParams
                layoutParamsColor3.width = 120
                layoutParamsColor3.height = 120
                colorButton3.layoutParams = layoutParamsColor3

                val layoutParamAdd = updateNoteButton.layoutParams
                layoutParamAdd.width = 250
                layoutParamAdd.height = 250
                updateNoteButton.layoutParams = layoutParamAdd

                val drawable = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.baseline_done_52
                ) as VectorDrawable
                val bitmap = Bitmap.createBitmap(132, 132, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                updateNoteButton.setImageBitmap(bitmap)
            }
        }
    }

    private fun getResourceName(): String? {
        val backgroundId = updateNoteViewModel.buttonColors.value.find { it.first == 4 }?.second
        val backgroundResName = backgroundId?.let { it1 -> resources.getResourceName(it1) }
        return backgroundResName?.substringAfter(":drawable/")
    }

    private fun checkColorsVisibility(buttonSpacing: Int) {
        binding?.mainButton?.setOnClickListener {
            updateNoteViewModel.toggleColorsVisibility()
            if (updateNoteViewModel.isColorsVisible.value) {
                updateNoteViewModel.setVisibleColor()
                colorVisibleAnimation(buttonSpacing)
            } else {
                updateNoteViewModel.unsetVisibleColor()
                colorInvisibleAnimation()
            }
        }

        lifecycleScope.launch {
            updateNoteViewModel.isColorsVisible.flowWithLifecycle(lifecycle).collectLatest {
                if (it) {
                    colorVisibleAnimation(buttonSpacing)
                }
            }
        }
    }

    private fun updateBackgrounds() {
        val currentMainButtonColor =
            updateNoteViewModel.buttonColors.value.find { it.first == 4 }?.second
        if (currentMainButtonColor != null) {
            updateNoteViewModel.updateBackgroundsFromButton(currentMainButtonColor)
        }
    }

    private fun enableButtonsClickable() {
        binding?.apply {
            colorButton1.isClickable = true
            colorButton2.isClickable = true
            colorButton3.isClickable = true
            mainButton.isClickable = true
        }
    }

    private fun disableButtonsClickable() {
        binding?.apply {
            colorButton1.isClickable = false
            colorButton2.isClickable = false
            colorButton3.isClickable = false
            mainButton.isClickable = false
        }
    }

    private fun enableButtonsVisibility() {
        binding?.apply {
            colorButton1.visibility = View.VISIBLE
            colorButton2.visibility = View.VISIBLE
            colorButton3.visibility = View.VISIBLE
        }
    }

    private fun disableButtonsVisibility() {
        binding?.apply {
            colorButton1.visibility = View.GONE
            colorButton2.visibility = View.GONE
            colorButton3.visibility = View.GONE
        }
    }
}