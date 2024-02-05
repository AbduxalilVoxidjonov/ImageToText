package com.example.imagetotext

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.imagetotext.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    lateinit var imageInput: InputImage
    lateinit var textRecognizer: TextRecognizer
    private var STORAGE_PERMISSION_CODE = 113

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        binding.btnChooseImage.setOnClickListener {
            val chooseIntent = Intent(Intent.ACTION_GET_CONTENT)
            chooseIntent.type = "image/*"
            startActivityForResult(chooseIntent, STORAGE_PERMISSION_CODE)
        }
        binding.btnCopyText.setOnClickListener {
            val text = binding.tvResult.text.toString()
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Text Copied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertImageToText(imageUri: Uri?) {
        try {
            binding.progressBar.visibility = android.view.View.VISIBLE
            val runnable = Runnable {
                imageInput = InputImage.fromFilePath(applicationContext, imageUri!!)
                binding.progressBar.visibility = android.view.View.GONE
                val result: Task<Text> = textRecognizer.process(imageInput)
                    .addOnSuccessListener {
                        binding.tvResult.text = it.text
                    }
                    .addOnFailureListener {
                        binding.tvResult.text = "Failed to convert image to text"
                    }
            }
            val handler = Handler()
            handler.postDelayed(runnable, 3000)
        } catch (e: Exception) {
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == STORAGE_PERMISSION_CODE) {
                val uri = data?.data
                convertImageToText(uri)
            }
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                requestCode
            )
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


}