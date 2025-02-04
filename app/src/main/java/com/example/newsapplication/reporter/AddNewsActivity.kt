package com.example.newsapplication.reporter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapplication.DividerSpinnerAdapter
import com.example.newsapplication.News
import com.example.newsapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddNewsActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDescription: EditText
    private lateinit var btnUploadImage: Button
    private lateinit var imgPreview: ImageView
    private lateinit var btnSubmit: Button

    private var selectedImageUri: Uri? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // Activity result launcher for image selection
    private val selectImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                imgPreview.setImageURI(it)
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

        // Set up categories for Spinner
        val categories = listOf("Local", "International", "Business", "Sports", "Science", "Technology", "Entertainment", "Lifestyle")
        val adapter = DividerSpinnerAdapter(this, R.layout.spinner_item_with_divider, categories.toTypedArray()) // Use the custom DividerSpinnerAdapter for the Spinner
        spinnerCategory.adapter = adapter

        // Upload Image Button
        btnUploadImage.setOnClickListener {
            selectImage.launch("image/*")
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

    // Function to upload news
    private fun uploadNews() {
        val title = etTitle.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val description = etDescription.text.toString().trim()

        // Check input null values
        if (title.isEmpty() || category.isEmpty() || description.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val newsRef = storage.reference.child("news_images/${UUID.randomUUID()}.jpg")
        val uploadTask = newsRef.putFile(selectedImageUri!!)

        uploadTask.addOnSuccessListener {
            newsRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                val reporterEmail = auth.currentUser?.email ?: "unknown"
                val newsDatabaseRef = FirebaseDatabase.getInstance().getReference("news")
                val newsId = newsDatabaseRef.push().key ?: UUID.randomUUID().toString()

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
