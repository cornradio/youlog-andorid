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
import com.youlog.app.ui.adapter.TimelineAdapter
import com.youlog.app.ui.viewmodel.MainViewModel
import com.youlog.app.ui.viewmodel.MainViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class TimelineFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimelineAdapter
    private lateinit var viewModel: MainViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_timeline, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val database = AppDatabase.getDatabase(requireContext())
        val repository = ImageRepository(database.imageDao(), database.tagDao())
        val owner: ViewModelStoreOwner = requireActivity()
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(owner, factory)[MainViewModel::class.java]
        
        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = TimelineAdapter { image ->
            openImageDetail(image)
        }
        
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.adapter = adapter
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredImages.collect { images ->
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

