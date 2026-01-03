package com.youlog.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.youlog.app.R
import com.youlog.app.data.AppDatabase
import com.youlog.app.repository.ImageRepository
import com.youlog.app.ui.adapter.MainPagerAdapter
import com.youlog.app.ui.viewmodel.MainViewModel
import com.youlog.app.ui.viewmodel.MainViewModelFactory
import com.youlog.app.utils.ImageUtils
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.view.View
import android.widget.LinearLayout
import java.io.File
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var filterLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var viewPager: ViewPager2
    
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imagePath = result.data?.getStringExtra("image_path")
            if (imagePath != null) {
                handleImageImportFromFile(File(imagePath), compress = true)
            }
        }
    }
    
    private val photoPickerLauncher = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) {
            showImportOptionsDialog(uris)
        }
    }
    
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        val database = AppDatabase.getDatabase(this)
        val repository = ImageRepository(database.imageDao(), database.tagDao())
        val owner: ViewModelStoreOwner = this
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(owner, factory)[MainViewModel::class.java]
        
        setupFilterLauncher()
        setupViewPager()
        setupEmptyState()
        handleShareIntent(intent)
    }

    private fun setupEmptyState() {
        val emptyStateLayout = findViewById<LinearLayout>(R.id.emptyStateLayout)
        lifecycleScope.launch {
            viewModel.allImages.collectLatest { images ->
                if (images.isEmpty()) {
                    emptyStateLayout.visibility = View.VISIBLE
                    viewPager.visibility = View.GONE
                } else {
                    emptyStateLayout.visibility = View.GONE
                    viewPager.visibility = View.VISIBLE
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }
    
    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
        
        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false 
        
        // 底部导航切换
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_timeline -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_add -> {
                    showAddImageOptions()
                    false 
                }
                R.id.nav_mini_view -> {
                    viewPager.currentItem = 1
                    true
                }
                else -> false
            }
        }
        
        // ViewPager 页面变化同步到底部导航
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val itemId = when (position) {
                    0 -> R.id.nav_timeline
                    1 -> R.id.nav_mini_view
                    else -> R.id.nav_timeline
                }
                bottomNavigation.menu.findItem(itemId).isChecked = true
            }
        })
    }
    
    private fun showAddImageOptions() {
        val options = arrayOf(getString(R.string.camera), getString(R.string.photo_library))
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_image))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openPhotoLibrary()
                }
            }
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_filter -> {
                val intent = Intent(this, DateFilterActivity::class.java).apply {
                    putExtra("start_date", viewModel.startDate.value?.time ?: -1L)
                    putExtra("end_date", viewModel.endDate.value?.time ?: -1L)
                    putExtra("selected_tags", viewModel.selectedTags.value.toTypedArray())
                }
                filterLauncher.launch(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupFilterLauncher() {
        filterLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val start = data?.getLongExtra("start_date", -1L)?.let { if (it == -1L) null else java.util.Date(it) }
                val end = data?.getLongExtra("end_date", -1L)?.let { if (it == -1L) null else java.util.Date(it) }
                val tags = data?.getStringArrayExtra("selected_tags")?.toSet() ?: emptySet()
                
                viewModel.setFilters(tags, start, end)
            }
        }
    }
    
    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun openCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        cameraLauncher.launch(intent)
    }
    
    private fun openPhotoLibrary() {
        photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun showImportOptionsDialog(uris: List<Uri>) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("照片导入选项")
            .setMessage("已选择 ${uris.size} 张照片。导入后是否删除系统图库中的原图以节省空间？")
            .setPositiveButton("导入并删除原图") { _, _ ->
                uris.forEach { handleImageImport(it, compress = true, deleteOriginal = true) }
            }
            .setNegativeButton("仅导入") { _, _ ->
                uris.forEach { handleImageImport(it, compress = true, deleteOriginal = false) }
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }
    
    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            }
            imageUri?.let {
                handleImageImport(it, compress = true, deleteOriginal = false)
            }
        }
    }
    
    private fun handleImageImport(uri: Uri, compress: Boolean, deleteOriginal: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val savedFile = ImageUtils.saveImageFromUri(this@MainActivity, uri, compress)
                if (savedFile != null) {
                    val imageEntity = com.youlog.app.data.ImageEntity(
                        filePath = savedFile.absolutePath,
                        dateCreated = Date()
                    )
                    viewModel.insertImage(imageEntity)
                    
                    if (deleteOriginal && uri.scheme == "content") {
                        // 尝试删除原图（需要权限）
                        try {
                            contentResolver.delete(uri, null, null)
                        } catch (e: Exception) {
                            // 忽略删除失败
                        }
                    }
                    
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "图片已导入", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "导入失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun handleImageImportFromFile(file: File, compress: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val savedFile = ImageUtils.saveImageFromFile(this@MainActivity, file, compress)
                if (savedFile != null) {
                    val imageEntity = com.youlog.app.data.ImageEntity(
                        filePath = savedFile.absolutePath,
                        dateCreated = Date()
                    )
                    viewModel.insertImage(imageEntity)
                    
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "图片已导入", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "导入失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

