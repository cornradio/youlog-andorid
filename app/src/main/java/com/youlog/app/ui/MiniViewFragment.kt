package com.youlog.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import com.youlog.app.R
import com.youlog.app.data.AppDatabase
import com.youlog.app.data.ImageEntity
import com.youlog.app.repository.ImageRepository
import com.youlog.app.ui.adapter.MiniViewAdapter
import com.youlog.app.ui.viewmodel.MainViewModel
import com.youlog.app.ui.viewmodel.MainViewModelFactory

class MiniViewFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MiniViewAdapter
    private lateinit var viewModel: MainViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mini_view, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val database = AppDatabase.getDatabase(requireContext())
        val repository = ImageRepository(database.imageDao())
        val owner: ViewModelStoreOwner = requireActivity()
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(owner, factory)[MainViewModel::class.java]
        
        recyclerView = view.findViewById(R.id.recyclerView)
        val bulkActionBar = view.findViewById<android.view.View>(R.id.bulkActionBar)
        val btnDeleteSelected = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDeleteSelected)
        val btnCancelSelect = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancelSelect)
        
        adapter = MiniViewAdapter(
            onImageClick = { image -> openImageDetail(image) },
            onSelectionChanged = { isSelectionMode ->
                bulkActionBar.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            }
        )
        
        btnCancelSelect.setOnClickListener {
            adapter.exitSelectionMode()
        }
        
        btnDeleteSelected.setOnClickListener {
            val selectedIds = adapter.getSelectedIds()
            if (selectedIds.isNotEmpty()) {
                com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete)
                    .setMessage("确定要删除选中的 ${selectedIds.size} 张图片吗？")
                    .setPositiveButton(R.string.delete) { _, _ ->
                        lifecycleScope.launch {
                            viewModel.deleteImagesByIds(selectedIds)
                            adapter.exitSelectionMode()
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }
        
        // 使用更多的列数来实现紧凑的缩略图流
        recyclerView.layoutManager = GridLayoutManager(context, 4)
        recyclerView.adapter = adapter
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allImages.collect { images ->
                adapter.submitList(images)
            }
        }
    }
    
    private fun openImageDetail(image: ImageEntity) {
        val intent = Intent(context, ImageDetailActivity::class.java)
        intent.putExtra("image_id", image.id)
        startActivity(intent)
    }
}

