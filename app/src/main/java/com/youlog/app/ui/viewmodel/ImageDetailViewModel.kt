package com.youlog.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.youlog.app.data.ImageEntity
import com.youlog.app.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImageDetailViewModel(private val repository: ImageRepository) : ViewModel() {
    private val _allImages = MutableStateFlow<List<ImageEntity>>(emptyList())
    val allImages: StateFlow<List<ImageEntity>> = _allImages
    
    val allTags = repository.getAllTags()
    
    init {
        viewModelScope.launch {
            repository.getAllImages().collect { images ->
                _allImages.value = images
            }
        }
    }

    fun addTag(name: String) = viewModelScope.launch {
        repository.addTag(name)
    }

    fun deleteTag(tag: com.youlog.app.data.TagEntity) = viewModelScope.launch {
        repository.deleteTag(tag)
    }

    fun incrementTagUsage(name: String) = viewModelScope.launch {
        repository.incrementTagUsage(name)
    }

    fun decrementTagUsage(name: String) = viewModelScope.launch {
        repository.decrementTagUsage(name)
    }
    
    suspend fun getImageById(id: Long): ImageEntity? = repository.getImageById(id)
    
    fun updateImage(image: ImageEntity) = viewModelScope.launch {
        repository.updateImage(image)
    }
}

class ImageDetailViewModelFactory(private val repository: ImageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImageDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

