package com.example.todoapp.ui.adapter.notelist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.ui.fragment.note.NoteModel
import com.example.todoapp.databinding.ItemNoteBinding

class ListAdapter(
    private var noteList: List<NoteModel>,
    private val itemClickedListener: RecyclerItemClicked,
) :
    RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

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

        holder.itemView.setOnLongClickListener {
            itemClickedListener.onLongClickedItem(note)
            true
        }

        holder.itemView.setOnClickListener {
            itemClickedListener.onClickedItem(note)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateContactList(newNoteList: List<NoteModel>) {
        noteList = newNoteList
        notifyDataSetChanged()
    }

    interface RecyclerItemClicked {
        fun onLongClickedItem(note: NoteModel)
        fun onClickedItem(note: NoteModel)
    }
}