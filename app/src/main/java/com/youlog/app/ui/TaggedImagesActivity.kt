package com.youlog.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.youlog.app.R
import com.youlog.app.data.AppDatabase
import com.youlog.app.data.ImageEntity
import com.youlog.app.repository.ImageRepository
import com.youlog.app.ui.adapter.BulkSelectAdapter
import com.youlog.app.ui.viewmodel.MainViewModel
import com.youlog.app.ui.viewmodel.MainViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TaggedImagesActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: BulkSelectAdapter
    private var tagName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tagged_images)

        tagName = intent.getStringExtra("tag_name")
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "#$tagName"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val database = AppDatabase.getDatabase(this)
        val repository = ImageRepository(database.imageDao(), database.tagDao())
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = BulkSelectAdapter { image ->
            openImageDetail(image)
        }
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter

        observeBufferedImages()
    }

    private fun observeBufferedImages() {
        tagName?.let { tag ->
            lifecycleScope.launch {
                viewModel.allImages.collectLatest { allImages ->
                    val filtered = allImages.filter { image ->
                        val imgTags = image.tags?.split(",")?.map { it.trim() } ?: emptyList()
                        tag in imgTags
                    }
                    adapter.submitList(filtered)
                }
            }
        }
    }

    private fun openImageDetail(image: ImageEntity) {
        val intent = Intent(this, ImageDetailActivity::class.java)
        intent.putExtra("image_id", image.id)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
