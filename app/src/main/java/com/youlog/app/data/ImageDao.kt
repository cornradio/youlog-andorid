package com.youlog.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ImageDao {
    @Query("SELECT * FROM images ORDER BY dateCreated DESC")
    fun getAllImages(): Flow<List<ImageEntity>>
    
    @Query("SELECT * FROM images WHERE dateCreated >= :startDate AND dateCreated < :endDate ORDER BY dateCreated DESC")
    fun getImagesByDateRange(startDate: Date, endDate: Date): Flow<List<ImageEntity>>
    
    @Query("SELECT * FROM images WHERE id = :id")
    suspend fun getImageById(id: Long): ImageEntity?
    
    @Query("SELECT DISTINCT DATE(dateCreated/1000, 'unixepoch') as date FROM images ORDER BY date DESC")
    fun getDistinctDates(): Flow<List<String>>
    
    @Query("SELECT * FROM images WHERE tags LIKE '%' || :tag || '%' ORDER BY dateCreated DESC")
    fun getImagesByTag(tag: String): Flow<List<ImageEntity>>
    
    @Insert
    suspend fun insertImage(image: ImageEntity): Long
    
    @Update
    suspend fun updateImage(image: ImageEntity)
    
    @Delete
    suspend fun deleteImage(image: ImageEntity)
    
    @Query("DELETE FROM images WHERE id IN (:ids)")
    suspend fun deleteImagesByIds(ids: List<Long>)
}

