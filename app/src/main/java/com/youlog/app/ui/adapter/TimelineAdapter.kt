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
import com.youlog.app.ui.TimelineItem
import java.io.File

class TimelineAdapter(
    private val onImageClick: (ImageEntity) -> Unit
) : ListAdapter<TimelineItem, RecyclerView.ViewHolder>(TimelineDiffCallback()) {
    
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TimelineItem.DateHeader -> TYPE_HEADER
            is TimelineItem.Image -> TYPE_IMAGE
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_timeline_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_timeline_card, parent, false)
                ImageViewHolder(view, onImageClick)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TimelineItem.DateHeader -> (holder as HeaderViewHolder).bind(item.date)
            is TimelineItem.Image -> (holder as ImageViewHolder).bind(item.image)
        }
    }
    
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        
        fun bind(date: String) {
            dateText.text = date
        }
    }
    
    class ImageViewHolder(
        itemView: View,
        private val onImageClick: (ImageEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val noteTextView: TextView = itemView.findViewById(R.id.noteTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val weekdayTextView: TextView = itemView.findViewById(R.id.weekdayTextView)
        
        fun bind(image: ImageEntity) {
            Glide.with(itemView.context)
                .load(File(image.filePath))
                .centerCrop()
                .into(imageView)
            
            // 显示笔记(最多2行)
            if (!image.note.isNullOrBlank()) {
                noteTextView.text = image.note
                noteTextView.visibility = View.VISIBLE
            } else {
                noteTextView.visibility = View.GONE
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
    
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_IMAGE = 1
    }
}

class TimelineDiffCallback : DiffUtil.ItemCallback<TimelineItem>() {
    override fun areItemsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
        return when {
            oldItem is TimelineItem.DateHeader && newItem is TimelineItem.DateHeader ->
                oldItem.date == newItem.date
            oldItem is TimelineItem.Image && newItem is TimelineItem.Image ->
                oldItem.image.id == newItem.image.id
            else -> false
        }
    }
    
    override fun areContentsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
        return oldItem == newItem
    }
}

