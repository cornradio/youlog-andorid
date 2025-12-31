package com.youlog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.youlog.app.data.ImageEntity
import com.youlog.app.repository.ImageRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ImageRepository) : ViewModel() {
    val allImages = repository.getAllImages()
    val distinctDates = repository.getDistinctDates()
    
    fun getImagesByDateRange(startDate: java.util.Date, endDate: java.util.Date) = 
        repository.getImagesByDateRange(startDate, endDate)
    
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

