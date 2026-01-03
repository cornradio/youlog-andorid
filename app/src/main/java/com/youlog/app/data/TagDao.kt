package com.youlog.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY usageCount DESC, name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity)

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("UPDATE tags SET usageCount = usageCount + 1 WHERE name = :name")
    suspend fun incrementUsage(name: String)

    @Query("UPDATE tags SET usageCount = CASE WHEN usageCount > 0 THEN usageCount - 1 ELSE 0 END WHERE name = :name")
    suspend fun decrementUsage(name: String)
}
