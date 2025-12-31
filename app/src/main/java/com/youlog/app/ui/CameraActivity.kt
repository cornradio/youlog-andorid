package com.youlog.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.youlog.app.R
import com.youlog.app.utils.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class CameraActivity : AppCompatActivity() {
    private var photoUri: Uri? = null
    private lateinit var photoFile: File
    
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            handlePhotoTaken()
        } else {
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        openCamera()
    }
    
    private fun openCamera() {
        try {
            photoFile = ImageUtils.createImageFile(this)
            photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            
            takePictureLauncher.launch(photoUri)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开相机: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun handlePhotoTaken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 将照片保存到应用内部存储
                val savedFile = ImageUtils.saveImageFromUri(this@CameraActivity, photoUri!!, compress = true)
                if (savedFile != null) {
                    // 删除临时文件
                    photoFile.delete()
                    
                    // 通过Intent返回结果
                    val resultIntent = Intent().apply {
                        putExtra("image_path", savedFile.absolutePath)
                    }
                    setResult(RESULT_OK, resultIntent)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@CameraActivity, "保存照片失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            finish()
        }
    }
}

