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
    private val onImageClick: (ImageEntity) -> Unit
) : ListAdapter<ImageEntity, MiniViewAdapter.ImageViewHolder>(ImageDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mini_view, parent, false)
        return ImageViewHolder(view, onImageClick)
    }
    
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ImageViewHolder(
        itemView: View,
        private val onImageClick: (ImageEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        
        fun bind(image: ImageEntity) {
            Glide.with(itemView.context)
                .load(File(image.filePath))
                .centerCrop()
                .thumbnail(0.1f) // 使用缩略图优化性能
                .into(imageView)
            
            itemView.setOnClickListener {
                onImageClick(image)
            }
        }
    }
}

class ImageDiffCallback : DiffUtil.ItemCallback<ImageEntity>() {
    override fun areItemsTheSame(oldItem: ImageEntity, newItem: ImageEntity): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: ImageEntity, newItem: ImageEntity): Boolean {
        return oldItem == newItem
    }
}

