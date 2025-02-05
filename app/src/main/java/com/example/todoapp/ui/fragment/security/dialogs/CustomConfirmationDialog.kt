package com.example.todoapp.ui.fragment.security.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.example.todoapp.databinding.DialogCustomConfirmationBinding

class CustomConfirmationDialog : DialogFragment() {

    private var binding: DialogCustomConfirmationBinding? = null

    private var onYes: (() -> Unit)? = null
    private var onNo: (() -> Unit)? = null

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"

        fun newInstance(title: String, message: String, onYes: () -> Unit, onNo: (() -> Unit)? = null): CustomConfirmationDialog {
            return CustomConfirmationDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_MESSAGE, message)
                }
                this.onYes = onYes
                this.onNo = onNo
            }
        }
    }

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        binding = DialogCustomConfirmationBinding.inflate(LayoutInflater.from(requireContext()))
        binding?.root?.let { dialog.setContentView(it) }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val title = arguments?.getString(ARG_TITLE) ?: "Confirmation"
        val message = arguments?.getString(ARG_MESSAGE) ?: "Are you sure?"

        binding?.dialogTitle?.text = title
        binding?.dialogMessage?.text = message

        binding?.buttonYes?.setOnClickListener {
            onYes?.invoke()
            dismiss()
        }

        binding?.buttonNo?.setOnClickListener {
            onNo?.invoke()
            dismiss()
        }

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
