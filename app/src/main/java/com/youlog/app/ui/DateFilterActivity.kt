package com.youlog.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.youlog.app.R
import com.youlog.app.data.AppDatabase
import com.youlog.app.repository.ImageRepository
import com.youlog.app.ui.viewmodel.MainViewModel
import com.youlog.app.ui.viewmodel.MainViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class DateFilterActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    
    private var startDate: Date? = null
    private var endDate: Date? = null
    private val selectedTags = mutableSetOf<String>()
    
    private lateinit var btnStart: com.google.android.material.button.MaterialButton
    private lateinit var btnEnd: com.google.android.material.button.MaterialButton
    private lateinit var tagChipGroup: ChipGroup
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_filter)
        
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "筛选记录"
        
        val database = AppDatabase.getDatabase(this)
        val repository = ImageRepository(database.imageDao(), database.tagDao())
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
        
        // 获取传入的当前筛选状态
        intent.apply {
            startDate = getLongExtra("start_date", -1).let { if (it == -1L) null else Date(it) }
            endDate = getLongExtra("end_date", -1).let { if (it == -1L) null else Date(it) }
            getStringArrayExtra("selected_tags")?.let { selectedTags.addAll(it) }
        }
        
        setupUI()
        observeTags()
    }
    
    private fun setupUI() {
        btnStart = findViewById(R.id.btnStartDate)
        btnEnd = findViewById(R.id.btnEndDate)
        tagChipGroup = findViewById(R.id.tagFilterChipGroup)
        
        val sdf = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
        
        startDate?.let { btnStart.text = sdf.format(it) }
        endDate?.let { btnEnd.text = sdf.format(it) }
        
        btnStart.setOnClickListener {
            showDatePicker("选择起始时间") { date ->
                startDate = date
                btnStart.text = sdf.format(date)
            }
        }
        
        btnEnd.setOnClickListener {
            showDatePicker("选择结束时间") { date ->
                endDate = date
                btnEnd.text = sdf.format(date)
            }
        }
        
        findViewById<View>(R.id.btnClearFilter).setOnClickListener {
            startDate = null
            endDate = null
            selectedTags.clear()
            btnStart.text = "开始日期"
            btnEnd.text = "结束日期"
            // 更新 ChipGroup 状态
            for (i in 0 until tagChipGroup.childCount) {
                (tagChipGroup.getChildAt(i) as? Chip)?.isChecked = false
            }
        }
        
        findViewById<View>(R.id.btnApplyFilter).setOnClickListener {
            applyAndFinish()
        }
    }
    
    private fun showDatePicker(title: String, onDateSelected: (Date) -> Unit) {
        val picker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setTheme(R.style.Theme_YouLog_DatePicker)
            .build()
        
        picker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(Date(selection))
        }
        picker.show(supportFragmentManager, "DatePicker")
    }
    
    private fun observeTags() {
        lifecycleScope.launch {
            viewModel.allTags.collectLatest { tags ->
                tagChipGroup.removeAllViews()
                tags.forEach { tagEntity ->
                    val chip = Chip(this@DateFilterActivity).apply {
                        text = tagEntity.name
                        isCheckable = true
                        isChecked = selectedTags.contains(tagEntity.name)
                        setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) selectedTags.add(tagEntity.name) else selectedTags.remove(tagEntity.name)
                        }
                    }
                    tagChipGroup.addView(chip)
                }
            }
        }
    }
    
    private fun applyAndFinish() {
        val resultIntent = Intent().apply {
            putExtra("start_date", startDate?.time ?: -1L)
            putExtra("end_date", endDate?.time ?: -1L)
            putExtra("selected_tags", selectedTags.toTypedArray())
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

