package com.youlog.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.youlog.app.R
import com.youlog.app.data.ImageEntity
import java.io.File

class BulkSelectAdapter(
    private val onImageClick: (ImageEntity) -> Unit
) : ListAdapter<ImageEntity, BulkSelectAdapter.ImageViewHolder>(ImageDiffCallback()) {
    
    private val selectedIds = mutableSetOf<Long>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bulk_select, parent, false)
        return ImageViewHolder(view, onImageClick) { imageId, isSelected ->
            if (isSelected) {
                selectedIds.add(imageId)
            } else {
                selectedIds.remove(imageId)
            }
        }
    }
    
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = getItem(position)
        holder.bind(image, selectedIds.contains(image.id))
    }
    
    fun getSelectedIds(): List<Long> = selectedIds.toList()
    
    fun selectAll() {
        currentList.forEach { selectedIds.add(it.id) }
        notifyDataSetChanged()
    }
    
    fun deselectAll() {
        selectedIds.clear()
        notifyDataSetChanged()
    }
    
    fun clearSelection() {
        selectedIds.clear()
        notifyDataSetChanged()
    }
    
    class ImageViewHolder(
        itemView: View,
        private val onImageClick: (ImageEntity) -> Unit,
        private val onSelectionChanged: (Long, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        
        fun bind(image: ImageEntity, isSelected: Boolean) {
            Glide.with(itemView.context)
                .load(File(image.filePath))
                .centerCrop()
                .into(imageView)
            
            checkBox.isChecked = isSelected
            
            itemView.setOnClickListener {
                if (checkBox.isChecked) {
                    checkBox.isChecked = false
                    onSelectionChanged(image.id, false)
                } else {
                    checkBox.isChecked = true
                    onSelectionChanged(image.id, true)
                }
            }
            
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onSelectionChanged(image.id, isChecked)
            }
        }
    }
}

