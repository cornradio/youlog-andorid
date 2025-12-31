package com.youlog.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.youlog.app.R
import com.youlog.app.data.ImageEntity
import java.io.File

class MiniViewAdapter(
    private val onImageClick: (ImageEntity) -> Unit,
    private val onSelectionChanged: (Boolean) -> Unit
) : ListAdapter<ImageEntity, MiniViewAdapter.ImageViewHolder>(ImageDiffCallback()) {
    
    private var isSelectionMode = false
    private val selectedIds = mutableSetOf<Long>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mini_view, parent, false)
        return ImageViewHolder(view, onImageClick)
    }
    
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = getItem(position)
        holder.bind(image, isSelectionMode, selectedIds.contains(image.id))
        
        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) {
                enterSelectionMode(image.id)
                true
            } else false
        }
        
        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                toggleSelection(image.id)
            } else {
                onImageClick(image)
            }
        }
    }
    
    private fun enterSelectionMode(firstImageId: Long) {
        isSelectionMode = true
        selectedIds.add(firstImageId)
        notifyDataSetChanged()
        onSelectionChanged(true)
    }
    
    fun exitSelectionMode() {
        isSelectionMode = false
        selectedIds.clear()
        notifyDataSetChanged()
        onSelectionChanged(false)
    }
    
    private fun toggleSelection(imageId: Long) {
        if (selectedIds.contains(imageId)) {
            selectedIds.remove(imageId)
            if (selectedIds.isEmpty()) {
                exitSelectionMode()
            } else {
                notifyDataSetChanged()
            }
        } else {
            selectedIds.add(imageId)
            notifyDataSetChanged()
        }
    }
    
    fun getSelectedIds(): List<Long> = selectedIds.toList()
    
    class ImageViewHolder(
        itemView: View,
        val onImageClick: (ImageEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val checkBox: android.widget.CheckBox = itemView.findViewById(R.id.checkBox)
        private val selectionOverlay: View = itemView.findViewById(R.id.selectionOverlay)
        
        fun bind(image: ImageEntity, isSelectionMode: Boolean, isSelected: Boolean) {
            Glide.with(itemView.context)
                .load(File(image.filePath))
                .centerCrop()
                .thumbnail(0.1f)
                .into(imageView)
            
            if (isSelectionMode) {
                checkBox.visibility = View.VISIBLE
                checkBox.isChecked = isSelected
                selectionOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE
            } else {
                checkBox.visibility = View.GONE
                selectionOverlay.visibility = View.GONE
            }
        }
    }
}

