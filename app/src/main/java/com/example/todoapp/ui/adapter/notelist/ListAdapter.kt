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
    private val itemClickedListener: RecyclerItemClicked
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

        marginOnSecondElement(holder,position)

        val note = noteList[position]

        Log.i("CHECK_LOG", "viewholder list ${note.isSelected.value} and ${note.id}")

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
            itemClickedListener.isCheckedItem(note)
            true
        }

        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                itemClickedListener.isCheckedItem(note)
            } else {
                itemClickedListener.onClickedItem(note)
            }
        }

        holder.binding.checkBox.setOnClickListener {
            note.isSelected.value = holder.binding.checkBox.isChecked
        }

        holder.binding.checkBox.isChecked = note.isSelected.value

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateNoteList(newNoteList: List<NoteModel>) {
        noteList = newNoteList
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

    interface RecyclerItemClicked {
        fun onClickedItem(note: NoteModel)
        fun onLongClickedItem(note: NoteModel)
        fun isCheckedItem(note: NoteModel)
    }

    private fun marginOnSecondElement(holder: ListViewHolder, position: Int){
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        if (position % 2 == 1) {
            layoutParams.topMargin = 50
        } else {
            layoutParams.topMargin = 0
        }
        holder.itemView.layoutParams = layoutParams
    }
}