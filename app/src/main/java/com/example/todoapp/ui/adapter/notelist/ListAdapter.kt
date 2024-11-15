package com.example.todoapp.ui.adapter.notelist

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.ItemHeaderBinding
import com.example.todoapp.ui.fragment.note.NoteModel
import com.example.todoapp.databinding.ItemNoteBinding


class ListAdapter(
    private val itemClickedListener: RecyclerItemClicked
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_NOTE = 1
    }

    private var isSelectionMode = false
    private var noteList: List<NoteModel> = emptyList()

    class HeaderViewHolder(val binding: ItemHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    class NoteViewHolder(val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        // Определение типа элемента: 0 для хедера, 1 для заметок
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_NOTE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_NOTE -> {
                val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                NoteViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return noteList.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        marginOnSecondElement(holder, position)
        if (holder is HeaderViewHolder) {
            holder.binding.headerTitle.text = "All Notes"
            holder.binding.sizeOfList.text = "(${noteList.size})"
        } else if (holder is NoteViewHolder) {
            val note = noteList[position - 1]

            holder.binding.textNote.text = note.noteText
            holder.binding.noteName.text = note.noteName
            holder.binding.dateCreateNote.text = note.noteDateCreate
            holder.binding.dateUpdateNote.text = note.noteDateUpdate

            holder.binding.checkBox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            holder.binding.checkBox.isChecked = note.isSelected.value

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
        }
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

    private fun marginOnSecondElement(holder: RecyclerView.ViewHolder, position: Int){
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        if (position != 0 && position % 2 == 0) {
            layoutParams.topMargin = 50
        } else {
            layoutParams.topMargin = 0
        }
        holder.itemView.layoutParams = layoutParams
    }
}
