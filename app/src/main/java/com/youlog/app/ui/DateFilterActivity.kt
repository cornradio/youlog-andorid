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
    private val selectedTags = mutableSetOf<String>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_filter)
        
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        val database = AppDatabase.getDatabase(this)
        val repository = ImageRepository(database.imageDao())
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
        
        setupUI()
        observeData()
    }
    
    private fun setupUI() {
        recyclerView = findViewById(R.id.recyclerView)
        adapter = BulkSelectAdapter { image ->
            openImageDetail(image)
        }
        
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
        
        val btnStart = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnStartDate)
        val btnEnd = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEndDate)
        
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        
        btnStart.setOnClickListener {
            showDatePicker("选择开始日期") { date ->
                startDate = date
                btnStart.text = sdf.format(date)
                loadFilteredImages()
            }
        }
        
        btnEnd.setOnClickListener {
            showDatePicker("选择结束日期") { date ->
                endDate = date
                btnEnd.text = sdf.format(date)
                loadFilteredImages()
            }
        }
    }
    
    private fun showDatePicker(title: String, onDateSelected: (Date) -> Unit) {
        val picker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        
        picker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(Date(selection))
        }
        picker.show(supportFragmentManager, "DatePicker")
    }
    
    private fun observeData() {
        // 获取所有标签
        lifecycleScope.launch {
            viewModel.allImages.collect { images ->
                val tags = images.flatMap { it.tags?.split(",") ?: emptyList() }
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                
                setupTagChips(tags)
                loadFilteredImages()
            }
        }
    }
    
    private fun setupTagChips(tags: List<String>) {
        val chipGroup = findViewById<com.google.android.material.chip.ChipGroup>(R.id.tagFilterChipGroup)
        chipGroup.removeAllViews()
        
        tags.forEach { tag ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = tag
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedTags.add(tag) else selectedTags.remove(tag)
                    loadFilteredImages()
                }
            }
            chipGroup.addView(chip)
        }
    }
    
    private fun loadFilteredImages() {
        lifecycleScope.launch {
            viewModel.allImages.collect { allImages ->
                val filtered = allImages.filter { image ->
                    // 1. 日期筛选
                    val dateMatch = when {
                        startDate != null && endDate != null -> {
                            val imgDate = image.dateCreated
                            // 将 endDate 设置为当天的 23:59:59
                            val endCal = Calendar.getInstance().apply { 
                                time = endDate!!
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                            }
                            imgDate.after(startDate) && imgDate.before(endCal.time)
                        }
                        startDate != null -> image.dateCreated.after(startDate)
                        endDate != null -> {
                            val endCal = Calendar.getInstance().apply { 
                                time = endDate!!
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                            }
                            image.dateCreated.before(endCal.time)
                        }
                        else -> true
                    }
                    
                    // 2. 标签筛选
                    val tagMatch = if (selectedTags.isEmpty()) {
                        true
                    } else {
                        val imgTags = image.tags?.split(",")?.map { it.trim() } ?: emptyList()
                        selectedTags.any { it in imgTags }
                    }
                    
                    dateMatch && tagMatch
                }
                adapter.submitList(filtered)
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

