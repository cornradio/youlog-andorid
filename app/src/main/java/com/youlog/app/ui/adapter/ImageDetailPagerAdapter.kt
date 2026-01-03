package com.youlog.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.chrisbanes.photoview.PhotoView
import com.bumptech.glide.Glide
import com.youlog.app.R
import com.youlog.app.data.ImageEntity
import com.youlog.app.ui.viewmodel.ImageDetailViewModel
import java.io.File

class ImageDetailPagerAdapter(
    private val activity: androidx.appcompat.app.AppCompatActivity,
    private val images: List<ImageEntity>,
    private val viewModel: ImageDetailViewModel
) : RecyclerView.Adapter<ImageDetailPagerAdapter.ImageViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_detail, parent, false)
        return ImageViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }
    
    override fun getItemCount(): Int = images.size
    
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photoView: PhotoView = itemView.findViewById(R.id.photoView)
        
        fun bind(image: ImageEntity) {
            Glide.with(itemView.context)
                .load(File(image.filePath))
                .into(photoView)
        }
    }
}

