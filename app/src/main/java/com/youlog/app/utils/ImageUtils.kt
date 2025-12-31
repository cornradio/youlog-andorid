package com.youlog.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
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
                    var bitmap = BitmapFactory.decodeStream(stream)
                    // 处理 EXIF 旋转
                    val orientation = getOrientation(context, uri)
                    if (orientation != 0) {
                        val rotated = rotateBitmap(bitmap, orientation)
                        if (rotated != bitmap) {
                            bitmap.recycle()
                            bitmap = rotated
                        }
                    }
                    
                    val outputFile = createImageFile(context)
                    
                    val finalBitmap = if (compress) {
                        compressBitmap(bitmap)
                    } else {
                        bitmap
                    }
                    
                    saveBitmap(finalBitmap, outputFile, 80)
                    
                    if (finalBitmap != bitmap) {
                        finalBitmap.recycle()
                    }
                    bitmap.recycle()
                    
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
                val uri = Uri.fromFile(sourceFile)
                var bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
                // 处理 EXIF 旋转
                val orientation = getOrientation(context, uri)
                if (orientation != 0) {
                    val rotated = rotateBitmap(bitmap, orientation)
                    if (rotated != bitmap) {
                        bitmap.recycle()
                        bitmap = rotated
                    }
                }
                
                val outputFile = createImageFile(context)
                
                val finalBitmap = if (compress) {
                    compressBitmap(bitmap)
                } else {
                    bitmap
                }
                
                saveBitmap(finalBitmap, outputFile, 80)
                
                if (finalBitmap != bitmap) {
                    finalBitmap.recycle()
                }
                bitmap.recycle()
                
                return@withContext outputFile
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun getOrientation(context: Context, uri: Uri): Int {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exifInterface = android.media.ExifInterface(inputStream!!)
            val orientation = exifInterface.getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_NORMAL
            )
            return when (orientation) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    private suspend fun compressBitmap(bitmap: Bitmap): Bitmap {
        return withContext(Dispatchers.IO) {
            // 均衡的压缩：1280px 足够清晰，且文件体积小
            val maxSize = 1280
            val width = bitmap.width
            val height = bitmap.height
            
            if (width <= maxSize && height <= maxSize) {
                return@withContext bitmap
            }
            
            val scale = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height)
            val newWidth = (width * scale).toInt()
            val newHeight = (height * scale).toInt()
            
            return@withContext Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }
    }

    // 辅助方法：保存时降低质量
    private fun saveBitmap(bitmap: Bitmap, file: File, quality: Int = 80) {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
    }
}

