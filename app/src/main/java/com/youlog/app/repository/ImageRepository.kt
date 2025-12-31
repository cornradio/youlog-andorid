package com.youlog.app.repository

import com.youlog.app.data.ImageDao
import com.youlog.app.data.ImageEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

class ImageRepository(private val imageDao: ImageDao) {
    fun getAllImages(): Flow<List<ImageEntity>> = imageDao.getAllImages()
    
    fun getImagesByDateRange(startDate: Date, endDate: Date): Flow<List<ImageEntity>> =
        imageDao.getImagesByDateRange(startDate, endDate)
    
    suspend fun getImageById(id: Long): ImageEntity? = imageDao.getImageById(id)
    
    fun getDistinctDates(): Flow<List<String>> = imageDao.getDistinctDates()
    
    fun getImagesByTag(tag: String): Flow<List<ImageEntity>> = imageDao.getImagesByTag(tag)
    
    suspend fun insertImage(image: ImageEntity): Long = imageDao.insertImage(image)
    
    suspend fun updateImage(image: ImageEntity) = imageDao.updateImage(image)
    
    suspend fun deleteImage(image: ImageEntity) = imageDao.deleteImage(image)
    
    suspend fun deleteImagesByIds(ids: List<Long>) = imageDao.deleteImagesByIds(ids)
}

