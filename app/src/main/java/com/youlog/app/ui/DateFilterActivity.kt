package com.youlog.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.youlog.app.R
import com.youlog.app.data.AppDatabase
import com.youlog.app.data.ImageEntity
import com.youlog.app.repository.ImageRepository
import com.youlog.app.ui.adapter.BulkSelectAdapter
import com.youlog.app.ui.viewmodel.MainViewModel
import com.youlog.app.ui.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class DateFilterActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BulkSelectAdapter
    private lateinit var viewModel: MainViewModel
    private var startDate: Date? = null
    private var endDate: Date? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_filter)
        
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        val database = AppDatabase.getDatabase(this)
        val repository = ImageRepository(database.imageDao())
        val factory = MainViewModelFactory(repository)
        val owner: ViewModelStoreOwner = this
        viewModel = ViewModelProvider(owner, factory)[MainViewModel::class.java]
        
        recyclerView = findViewById(R.id.recyclerView)
        adapter = BulkSelectAdapter { image ->
            openImageDetail(image)
        }
        
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
        
        // 默认显示今天的照片
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        startDate = calendar.time
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        endDate = calendar.time
        
        loadFilteredImages()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.date_filter_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.menu_select_all -> {
                adapter.selectAll()
                true
            }
            R.id.menu_deselect_all -> {
                adapter.deselectAll()
                true
            }
            R.id.menu_delete_selected -> {
                deleteSelected()
                true
            }
            R.id.menu_bulk_edit_tags -> {
                bulkEditTags()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun loadFilteredImages() {
        if (startDate != null && endDate != null) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.getImagesByDateRange(startDate!!, endDate!!).collect { images ->
                        adapter.submitList(images)
                    }
                }
            }
        }
    }
    
    private fun openImageDetail(image: ImageEntity) {
        val intent = Intent(this, ImageDetailActivity::class.java)
        intent.putExtra("image_id", image.id)
        startActivity(intent)
    }
    
    private fun deleteSelected() {
        val selectedIds = adapter.getSelectedIds()
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "请先选择要删除的图片", Toast.LENGTH_SHORT).show()
            return
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete)
            .setMessage(getString(R.string.delete_multiple_confirm, selectedIds.size))
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    viewModel.deleteImagesByIds(selectedIds)
                    adapter.clearSelection()
                    Toast.makeText(this@DateFilterActivity, "已删除 ${selectedIds.size} 张图片", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun bulkEditTags() {
        val selectedIds = adapter.getSelectedIds()
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "请先选择要编辑的图片", Toast.LENGTH_SHORT).show()
            return
        }
        
        // TODO: 实现批量标签编辑对话框
        Toast.makeText(this, "批量标签编辑功能开发中", Toast.LENGTH_SHORT).show()
    }
}

