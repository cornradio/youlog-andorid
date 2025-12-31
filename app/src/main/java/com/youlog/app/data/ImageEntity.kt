package com.youlog.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val dateCreated: Date,
    val note: String? = null,
    val tags: String? = null // 逗号分隔的标签列表
)

