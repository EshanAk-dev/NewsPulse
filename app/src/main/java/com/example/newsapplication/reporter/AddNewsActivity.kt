package com.example.newsapplication.reporter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.newsapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import android.Manifest
import android.content.pm.PackageManager
import com.example.newsapplication.DividerSpinnerAdapter
import com.example.newsapplication.News
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream

class AddNewsActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDescription: EditText
    private lateinit var btnUploadImage: Button
    private lateinit var imgPreview: ImageView
    private lateinit var btnSubmit: Button
    private lateinit var btnCapturePhoto: Button

    private var selectedImageUri: Uri? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // Camera permission request code
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private val CAPTURE_IMAGE_REQUEST_CODE = 1002

    // Activity result launcher for image selection from gallery
    private val selectImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                imgPreview.setImageURI(it)  // Display selected image in ImageView
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_news)

        // Initialize UI elements
        etTitle = findViewById(R.id.et_Title)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etDescription = findViewById(R.id.et_Description)
        btnUploadImage = findViewById(R.id.btn_UploadImage)
        imgPreview = findViewById(R.id.img_Preview)
        btnSubmit = findViewById(R.id.btn_Submit)
        btnCapturePhoto = findViewById(R.id.btn_CapturePhoto)  // Capture photo button

        // Set up categories for Spinner
        val categories = listOf("Local", "International", "Business", "Sports", "Science", "Technology", "Entertainment", "Lifestyle")
        val adapter = DividerSpinnerAdapter(this, R.layout.spinner_item_with_divider, categories.toTypedArray()) // Use the custom DividerSpinnerAdapter for the Spinner
        spinnerCategory.adapter = adapter

        // Upload Image Button - open gallery to pick an image
        btnUploadImage.setOnClickListener {
            selectImage.launch("image/*") // Launch gallery to pick an image
        }

        // Capture Photo Button - open camera to capture a photo
        btnCapturePhoto.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        // Submit Button Click Listener
        btnSubmit.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to submit this news?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    uploadNews()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
    }

    // Check if camera permission is granted
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    // Request camera permission
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to capture photo", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // Open camera to capture image
    private fun openCamera() {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (captureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(captureIntent, CAPTURE_IMAGE_REQUEST_CODE)
        }
    }

    // Handle the captured image result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                val photo: Bitmap = data?.extras?.get("data") as Bitmap
                imgPreview.setImageBitmap(photo)

                // Store the image as a file in Firebase Storage
                val imageUri = getImageUriFromBitmap(photo)
                selectedImageUri = imageUri // Store the URI
            }
        }
    }

    // Convert Bitmap to URI for Firebase Storage upload
    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val file = File(cacheDir, "captured_image.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return Uri.fromFile(file)
    }

    // Function to upload news with the selected image
    private fun uploadNews() {
        val title = etTitle.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val description = etDescription.text.toString().trim()

        // Check if all required fields are filled
        if (title.isEmpty() || category.isEmpty() || description.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        // Upload the image to Firebase Storage
        val newsRef = storage.reference.child("news_images/${UUID.randomUUID()}.jpg")
        val uploadTask = newsRef.putFile(selectedImageUri!!)

        uploadTask.addOnSuccessListener {
            newsRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()  // Get the download URL of the uploaded image
                val reporterEmail = auth.currentUser?.email ?: "unknown"
                val newsDatabaseRef = FirebaseDatabase.getInstance().getReference("news")
                val newsId = newsDatabaseRef.push().key ?: UUID.randomUUID().toString()

                // Create a news object and save it to the database
                val newsData = News(title, category, description, imageUrl, newsId, reporterEmail, "In Progress", System.currentTimeMillis())

                newsDatabaseRef.child(newsId).setValue(newsData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "News submitted successfully", Toast.LENGTH_SHORT).show()

                        // After submission, go back to ReporterActivity and refresh the list
                        val intent = Intent(this, ReporterActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clear the stack
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to submit news", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
        }
    }
}
