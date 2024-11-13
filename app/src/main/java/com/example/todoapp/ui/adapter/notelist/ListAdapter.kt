package com.example.todoapp.ui.adapter.notelist

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.ui.fragment.note.NoteModel
import com.example.todoapp.databinding.ItemNoteBinding

class ListAdapter(
    private val itemClickedListener: RecyclerItemClicked,
    private val selectionModeListener: SelectionModeListener,
) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    private var isSelectionMode = false

    private var noteList: List<NoteModel> = emptyList()

    class ListViewHolder(val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val note = noteList[position]

        val noteText = note.noteText
        val noteName = note.noteName
        val dateCreateNote = note.noteDateCreate
        val dateUpdateNote = note.noteDateUpdate

        holder.binding.textNote.text = noteText
        holder.binding.noteName.text = noteName
        holder.binding.dateCreateNote.text = dateCreateNote
        holder.binding.dateUpdateNote.text = dateUpdateNote

        holder.binding.checkBox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE

        holder.itemView.setOnLongClickListener {
            itemClickedListener.onLongClickedItem(note)
            toggleSelectionMode()
            true
        }

        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                note.isSelected.value = !note.isSelected.value
                holder.binding.checkBox.isChecked = note.isSelected.value
                Log.i("CHECK_LOG", "${note.isSelected.value} and ${note.id}")

            } else {
                itemClickedListener.onClickedItem(note)
            }
        }

        holder.binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            note.isSelected.value = isChecked
            itemClickedListener.isCheckedItem(note)

        }

        holder.binding.checkBox.setOnCheckedChangeListener(null)
        holder.binding.checkBox.isChecked = note.isSelected.value
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateNoteList(newNoteList: List<NoteModel>) {
        noteList = newNoteList
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun toggleSelectionMode() {
        selectionModeListener.onSelectionModeChanged(isSelectionMode)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun exitSelectionMode() {
        isSelectionMode = false
        noteList.forEach { it.isSelected.value = false }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<NoteModel> {
        return noteList.filter { it.isSelected.value }
    }

    fun setSelectionMode(condition: Boolean){
        isSelectionMode = condition
    }

    interface SelectionModeListener {
        fun onSelectionModeChanged(isSelectionMode: Boolean)
    }

    interface RecyclerItemClicked {
        fun onClickedItem(note: NoteModel)
        fun onLongClickedItem(note: NoteModel)
        fun isCheckedItem(note: NoteModel)
    }

}