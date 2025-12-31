package com.youlog.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.youlog.app.R
import com.youlog.app.data.ImageEntity
import java.io.File

class TimelineAdapter(
    private val onImageClick: (ImageEntity) -> Unit
) : ListAdapter<ImageEntity, TimelineAdapter.ImageViewHolder>(ImageDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeline_card, parent, false)
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
        private val noteTextView: TextView = itemView.findViewById(R.id.noteTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val weekdayTextView: TextView = itemView.findViewById(R.id.weekdayTextView)
        private val tagTextView: TextView = itemView.findViewById(R.id.tagTextView)
        
        fun bind(image: ImageEntity) {
            Glide.with(itemView.context)
                .load(File(image.filePath))
                .centerCrop()
                .into(imageView)
            
            // 显示笔记
            if (!image.note.isNullOrBlank()) {
                noteTextView.text = image.note
                noteTextView.visibility = View.VISIBLE
            } else {
                noteTextView.visibility = View.GONE
            }
            
            // 显示标签
            if (!image.tags.isNullOrBlank()) {
                val formattedTags = image.tags.split(",")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { "#$it" }
                tagTextView.text = formattedTags
                tagTextView.visibility = View.VISIBLE
            } else {
                tagTextView.visibility = View.GONE
            }
            
            // 显示拍摄时间 (格式: 09-20 12:30)
            val dateFormat = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
            timeTextView.text = dateFormat.format(image.dateCreated)
            
            // 显示星期
            val weekdayFormat = java.text.SimpleDateFormat("EEEE", java.util.Locale.CHINESE)
            weekdayTextView.text = weekdayFormat.format(image.dateCreated)
            
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

