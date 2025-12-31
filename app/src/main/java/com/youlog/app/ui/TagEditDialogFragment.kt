package com.youlog.app.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.youlog.app.R
import com.youlog.app.data.AppDatabase
import com.youlog.app.repository.ImageRepository
import com.youlog.app.ui.viewmodel.ImageDetailViewModel
import com.youlog.app.ui.viewmodel.ImageDetailViewModelFactory
import kotlinx.coroutines.launch

class TagEditDialogFragment : DialogFragment() {
    private var imageId: Long = -1
    private lateinit var tagEditText: EditText
    private lateinit var tagChipGroup: ChipGroup
    private lateinit var viewModel: ImageDetailViewModel
    private val currentTags = mutableListOf<String>()
    
    companion object {
        fun newInstance(imageId: Long, currentTags: String): TagEditDialogFragment {
            return TagEditDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong("image_id", imageId)
                    putString("current_tags", currentTags)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageId = arguments?.getLong("image_id") ?: -1
        val tagsStr = arguments?.getString("current_tags") ?: ""
        if (tagsStr.isNotEmpty()) {
            currentTags.addAll(tagsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }
        
        val database = AppDatabase.getDatabase(requireContext())
        val repository = ImageRepository(database.imageDao())
        val owner: ViewModelStoreOwner = requireActivity()
        val factory = ImageDetailViewModelFactory(repository)
        viewModel = ViewModelProvider(owner, factory)[ImageDetailViewModel::class.java]
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_tags, null)
        tagEditText = view.findViewById(R.id.tagEditText)
        tagChipGroup = view.findViewById(R.id.tagChipGroup)
        
        updateTagChips()
        
        view.findViewById<View>(R.id.addTagButton).setOnClickListener {
            val tag = tagEditText.text.toString().trim()
            if (tag.isNotEmpty() && !currentTags.contains(tag)) {
                currentTags.add(tag)
                updateTagChips()
                tagEditText.text.clear()
            }
        }
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_tag)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                saveTags()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }
    
    private fun updateTagChips() {
        tagChipGroup.removeAllViews()
        currentTags.forEach { tag ->
            val chip = Chip(requireContext())
            chip.text = tag
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                currentTags.remove(tag)
                updateTagChips()
            }
            tagChipGroup.addView(chip)
        }
    }
    
    private fun saveTags() {
        val tagsStr = currentTags.joinToString(",")
        lifecycleScope.launch {
            val image = viewModel.getImageById(imageId)
            if (image != null) {
                val updatedImage = image.copy(tags = if (tagsStr.isEmpty()) null else tagsStr)
                viewModel.updateImage(updatedImage)
            }
        }
    }
}

