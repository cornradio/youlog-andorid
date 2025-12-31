package com.youlog.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {
    private const val IMAGE_DIR = "images"
    
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageDir = File(storageDir, IMAGE_DIR)
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }
        return File.createTempFile(imageFileName, ".jpg", imageDir)
    }
    
    suspend fun saveImageFromUri(context: Context, uri: Uri, compress: Boolean = false): File? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    val outputFile = createImageFile(context)
                    
                    val finalBitmap = if (compress) {
                        compressBitmap(bitmap)
                    } else {
                        bitmap
                    }
                    
                    FileOutputStream(outputFile).use { out ->
                        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    
                    bitmap.recycle()
                    if (finalBitmap != bitmap) {
                        finalBitmap.recycle()
                    }
                    
                    return@withContext outputFile
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    suspend fun saveImageFromFile(context: Context, sourceFile: File, compress: Boolean = false): File? {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
                val outputFile = createImageFile(context)
                
                val finalBitmap = if (compress) {
                    compressBitmap(bitmap)
                } else {
                    bitmap
                }
                
                FileOutputStream(outputFile).use { out ->
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                
                bitmap.recycle()
                if (finalBitmap != bitmap) {
                    finalBitmap.recycle()
                }
                
                return@withContext outputFile
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    private suspend fun compressBitmap(bitmap: Bitmap): Bitmap {
        return withContext(Dispatchers.IO) {
            // 简单的压缩：如果图片太大，缩小尺寸
            val maxSize = 1920
            val width = bitmap.width
            val height = bitmap.height
            
            if (width <= maxSize && height <= maxSize) {
                return@withContext bitmap
            }
            
            val scale = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height)
            val newWidth = (width * scale).toInt()
            val newHeight = (height * scale).toInt()
            
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }
    }
}

