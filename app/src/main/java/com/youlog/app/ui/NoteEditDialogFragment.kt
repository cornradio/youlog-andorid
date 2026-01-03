package com.youlog.app.ui

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.youlog.app.R
import com.youlog.app.data.AppDatabase
import com.youlog.app.data.ImageEntity
import com.youlog.app.repository.ImageRepository
import com.youlog.app.ui.viewmodel.ImageDetailViewModel
import com.youlog.app.ui.viewmodel.ImageDetailViewModelFactory
import kotlinx.coroutines.launch

class NoteEditDialogFragment : DialogFragment() {
    private var imageId: Long = -1
    private lateinit var noteEditText: EditText
    private lateinit var viewModel: ImageDetailViewModel
    
    companion object {
        fun newInstance(imageId: Long, currentNote: String): NoteEditDialogFragment {
            return NoteEditDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong("image_id", imageId)
                    putString("current_note", currentNote)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageId = arguments?.getLong("image_id") ?: -1
        
        val database = AppDatabase.getDatabase(requireContext())
        val repository = ImageRepository(database.imageDao(), database.tagDao())
        val owner: ViewModelStoreOwner = requireActivity()
        val factory = ImageDetailViewModelFactory(repository)
        viewModel = ViewModelProvider(owner, factory)[ImageDetailViewModel::class.java]
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_note, null)
        noteEditText = view.findViewById(R.id.noteEditText)
        val currentNote = arguments?.getString("current_note") ?: ""
        noteEditText.setText(currentNote)
        noteEditText.setSelection(currentNote.length)
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_note)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                saveNote()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }
    
    private fun saveNote() {
        val note = noteEditText.text.toString().trim()
        lifecycleScope.launch {
            val image = viewModel.getImageById(imageId)
            if (image != null) {
                val updatedImage = image.copy(note = if (note.isEmpty()) null else note)
                viewModel.updateImage(updatedImage)
            }
        }
    }
}

