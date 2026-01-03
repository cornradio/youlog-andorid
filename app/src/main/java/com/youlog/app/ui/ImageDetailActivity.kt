package com.youlog.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.youlog.app.R
import com.youlog.app.data.AppDatabase
import com.youlog.app.data.ImageEntity
import com.youlog.app.repository.ImageRepository
import com.youlog.app.ui.adapter.ImageDetailPagerAdapter
import com.youlog.app.ui.viewmodel.ImageDetailViewModel
import com.youlog.app.ui.viewmodel.ImageDetailViewModelFactory
import com.youlog.app.ui.viewmodel.MainViewModel
import com.youlog.app.ui.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch

class ImageDetailActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var detailViewModel: ImageDetailViewModel
    private lateinit var mainViewModel: MainViewModel
    private var currentImageId: Long = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)
        
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        currentImageId = intent.getLongExtra("image_id", -1)
        if (currentImageId == -1L) {
            finish()
            return
        }
        
        val database = AppDatabase.getDatabase(this)
        val repository = ImageRepository(database.imageDao(), database.tagDao())
        val owner: ViewModelStoreOwner = this
        val detailFactory = ImageDetailViewModelFactory(repository)
        val mainFactory = MainViewModelFactory(repository)
        detailViewModel = ViewModelProvider(owner, detailFactory)[ImageDetailViewModel::class.java]
        mainViewModel = ViewModelProvider(owner, mainFactory)[MainViewModel::class.java]
        
        setupViewPager()
        setupBottomBar()
    }
    
    private fun setupBottomBar() {
        findViewById<android.view.View>(R.id.btnNote).setOnClickListener {
            openNoteEditor()
        }
        findViewById<android.view.View>(R.id.btnTag).setOnClickListener {
            openTagEditor()
        }
        findViewById<android.view.View>(R.id.btnDelete).setOnClickListener {
            confirmDelete()
        }
    }
    
    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        observeCurrentImage()
    }
    
    private fun observeCurrentImage() {
        lifecycleScope.launch {
            detailViewModel.allImages.collect { images ->
                val currentPosition = images.indexOfFirst { it.id == currentImageId }
                
                val adapter = ImageDetailPagerAdapter(
                    this@ImageDetailActivity,
                    images,
                    detailViewModel
                )
                viewPager.adapter = adapter
                
                if (currentPosition >= 0 && viewPager.currentItem == 0) {
                    viewPager.setCurrentItem(currentPosition, false)
                }
                
                viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        if (position < images.size) {
                            currentImageId = images[position].id
                            invalidateOptionsMenu()
                        }
                    }
                })
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.image_detail_menu, menu)
        // 既然底部有了，我们可以隐藏顶部的部分按钮，保留分享
        menu.findItem(R.id.menu_edit_note)?.isVisible = false
        menu.findItem(R.id.menu_edit_tags)?.isVisible = false
        menu.findItem(R.id.menu_delete)?.isVisible = false
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.menu_share -> {
                shareImage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun openNoteEditor() {
        lifecycleScope.launch {
            val image = detailViewModel.getImageById(currentImageId)
            if (image != null) {
                val intent = Intent(this@ImageDetailActivity, NoteEditActivity::class.java).apply {
                    putExtra("image_id", image.id)
                    putExtra("initial_note", image.note ?: "")
                }
                startActivity(intent)
            }
        }
    }
    
    private fun openTagEditor() {
        lifecycleScope.launch {
            val image = detailViewModel.getImageById(currentImageId)
            if (image != null) {
                TagEditDialogFragment.newInstance(image.id, image.tags ?: "")
                    .show(supportFragmentManager, "TagEditDialog")
            }
        }
    }
    
    private fun shareImage() {
        lifecycleScope.launch {
            val image = detailViewModel.getImageById(currentImageId)
            if (image != null) {
                val file = java.io.File(image.filePath)
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    this@ImageDetailActivity,
                    "${packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share_image)))
            }
        }
    }
    
    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_confirm)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteImage()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun deleteImage() {
        lifecycleScope.launch {
            val image = detailViewModel.getImageById(currentImageId)
            if (image != null) {
                mainViewModel.deleteImage(image)
                // 删除文件
                java.io.File(image.filePath).delete()
                finish()
            }
        }
    }
}

