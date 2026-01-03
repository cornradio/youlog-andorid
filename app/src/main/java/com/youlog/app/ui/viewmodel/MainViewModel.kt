package com.youlog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.youlog.app.data.ImageEntity
import com.youlog.app.data.TagEntity
import com.youlog.app.repository.ImageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ImageRepository) : ViewModel() {
    private val _allImages = repository.getAllImages()
    val allTags = repository.getAllTags()
    val distinctDates = repository.getDistinctDates()

    // 筛选状态
    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags

    private val _startDate = MutableStateFlow<java.util.Date?>(null)
    val startDate: StateFlow<java.util.Date?> = _startDate

    private val _endDate = MutableStateFlow<java.util.Date?>(null)
    val endDate: StateFlow<java.util.Date?> = _endDate

    // 组合后的过滤流
    val filteredImages = combine(_allImages, _selectedTags, _startDate, _endDate) { images, tags, start, end ->
        images.filter { image ->
            // 标签过滤
            val tagMatch = if (tags.isEmpty()) {
                true
            } else {
                val imgTags = image.tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
                tags.any { it in imgTags }
            }

            // 日期过滤
            val dateMatch = when {
                start != null && end != null -> {
                    val endCal = java.util.Calendar.getInstance().apply {
                        time = end
                        set(java.util.Calendar.HOUR_OF_DAY, 23)
                        set(java.util.Calendar.MINUTE, 59)
                        set(java.util.Calendar.SECOND, 59)
                    }
                    image.dateCreated.after(start) && image.dateCreated.before(endCal.time)
                }
                start != null -> image.dateCreated.after(start)
                end != null -> {
                    val endCal = java.util.Calendar.getInstance().apply {
                        time = end
                        set(java.util.Calendar.HOUR_OF_DAY, 23)
                        set(java.util.Calendar.MINUTE, 59)
                        set(java.util.Calendar.SECOND, 59)
                    }
                    image.dateCreated.before(endCal.time)
                }
                else -> true
            }

            tagMatch && dateMatch
        }
    }

    fun setFilters(tags: Set<String>, start: java.util.Date?, end: java.util.Date?) {
        _selectedTags.value = tags
        _startDate.value = start
        _endDate.value = end
    }

    fun clearFilters() {
        _selectedTags.value = emptySet()
        _startDate.value = null
        _endDate.value = null
    }

    // 之前的操作方法
    val allImages = _allImages
    
    fun insertImage(image: ImageEntity) = viewModelScope.launch {
        repository.insertImage(image)
    }
    
    fun updateImage(image: ImageEntity) = viewModelScope.launch {
        repository.updateImage(image)
    }
    
    fun deleteImage(image: ImageEntity) = viewModelScope.launch {
        repository.deleteImage(image)
    }
    
    fun deleteImagesByIds(ids: List<Long>) = viewModelScope.launch {
        repository.deleteImagesByIds(ids)
    }

    fun addTag(name: String) = viewModelScope.launch {
        repository.addTag(name)
    }

    fun deleteTag(tag: TagEntity) = viewModelScope.launch {
        repository.deleteTag(tag)
    }
}

class MainViewModelFactory(private val repository: ImageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

