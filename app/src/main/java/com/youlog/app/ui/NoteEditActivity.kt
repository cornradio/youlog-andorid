package com.youlog.app.ui

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.youlog.app.R
import com.youlog.app.data.AppDatabase
import com.youlog.app.repository.ImageRepository
import com.youlog.app.ui.viewmodel.ImageDetailViewModel
import com.youlog.app.ui.viewmodel.ImageDetailViewModelFactory
import kotlinx.coroutines.launch

class NoteEditActivity : AppCompatActivity() {
    private var imageId: Long = -1
    private lateinit var noteEditText: EditText
    private lateinit var viewModel: ImageDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)

        imageId = intent.getLongExtra("image_id", -1)
        val initialNote = intent.getStringExtra("initial_note") ?: ""

        if (imageId == -1L) {
            finish()
            return
        }

        val database = AppDatabase.getDatabase(this)
        val repository = ImageRepository(database.imageDao())
        val factory = ImageDetailViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ImageDetailViewModel::class.java]

        setupUI(initialNote)
    }

    private fun setupUI(initialNote: String) {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        noteEditText = findViewById(R.id.noteEditText)
        noteEditText.setText(initialNote)
        noteEditText.setSelection(initialNote.length)

        findViewById<MaterialButton>(R.id.btnCopy).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("note", noteEditText.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, R.string.note_copied, Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnClear).setOnClickListener {
            noteEditText.setText("")
        }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            saveNote()
        }
    }

    private fun saveNote() {
        val note = noteEditText.text.toString().trim()
        lifecycleScope.launch {
            val image = viewModel.getImageById(imageId)
            if (image != null) {
                val updatedImage = image.copy(note = if (note.isEmpty()) null else note)
                viewModel.updateImage(updatedImage)
                Toast.makeText(this@NoteEditActivity, R.string.note_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
