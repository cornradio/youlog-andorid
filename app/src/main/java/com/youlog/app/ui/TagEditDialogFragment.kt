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
    private lateinit var newTagEditText: EditText
    private lateinit var currentTagChipGroup: ChipGroup
    private lateinit var availableTagChipGroup: ChipGroup
    private lateinit var viewModel: ImageDetailViewModel
    private val selectedTags = mutableListOf<String>()
    private var originalTags = listOf<String>()
    
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
            originalTags = tagsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            selectedTags.addAll(originalTags)
        }
        
        val database = AppDatabase.getDatabase(requireContext())
        val repository = ImageRepository(database.imageDao(), database.tagDao())
        val owner: ViewModelStoreOwner = requireActivity()
        val factory = ImageDetailViewModelFactory(repository)
        viewModel = ViewModelProvider(owner, factory)[ImageDetailViewModel::class.java]
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_tags, null)
        newTagEditText = view.findViewById(R.id.newTagEditText)
        currentTagChipGroup = view.findViewById(R.id.currentTagChipGroup)
        availableTagChipGroup = view.findViewById(R.id.availableTagChipGroup)
        
        setupObservers()
        
        view.findViewById<View>(R.id.btnCreateTag).setOnClickListener {
            val tag = newTagEditText.text.toString().trim()
            if (tag.isNotEmpty()) {
                if (!selectedTags.contains(tag)) {
                    selectedTags.add(tag)
                    updateCurrentTagChips()
                }
                viewModel.addTag(tag)
                newTagEditText.text.clear()
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

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.allTags.collect { tags ->
                updateAvailableTagChips(tags.map { it.name })
            }
        }
        updateCurrentTagChips()
    }
    
    private fun updateCurrentTagChips() {
        currentTagChipGroup.removeAllViews()
        selectedTags.forEach { tag ->
            val chip = Chip(requireContext())
            chip.text = tag
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                selectedTags.remove(tag)
                updateCurrentTagChips()
                // 刷新可用标签列表的显示状态（如果需要）
            }
            currentTagChipGroup.addView(chip)
        }
    }

    private fun updateAvailableTagChips(allTags: List<String>) {
        availableTagChipGroup.removeAllViews()
        allTags.forEach { tag ->
            if (!selectedTags.contains(tag)) {
                val chip = Chip(requireContext())
                chip.text = tag
                chip.setOnClickListener {
                    selectedTags.add(tag)
                    updateCurrentTagChips()
                    updateAvailableTagChips(allTags)
                }
                availableTagChipGroup.addView(chip)
            }
        }
    }
    
    private fun saveTags() {
        val tagsStr = selectedTags.joinToString(",")
        lifecycleScope.launch {
            val image = viewModel.getImageById(imageId)
            if (image != null) {
                // 处理标签使用次数
                val added = selectedTags.filter { it !in originalTags }
                val removed = originalTags.filter { it !in selectedTags }
                
                added.forEach { viewModel.incrementTagUsage(it) }
                removed.forEach { viewModel.decrementTagUsage(it) }

                val updatedImage = image.copy(tags = if (tagsStr.isEmpty()) null else tagsStr)
                viewModel.updateImage(updatedImage)
            }
        }
    }
}

