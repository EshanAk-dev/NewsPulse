package com.example.newsapplication.reporter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapplication.LoginActivity
import com.example.newsapplication.News
import com.example.newsapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ReporterActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDescription: EditText
    private lateinit var btnUploadImage: Button
    private lateinit var imgPreview: ImageView
    private lateinit var btnSubmit: Button
    private lateinit var logoutButton: Button
    private var selectedImageUri: Uri? = null

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val selectImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                imgPreview.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reporter)

        // Initialize views
        etTitle = findViewById(R.id.et_Title)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etDescription = findViewById(R.id.et_Description)
        btnUploadImage = findViewById(R.id.btn_UploadImage)
        imgPreview = findViewById(R.id.img_Preview)
        btnSubmit = findViewById(R.id.btn_Submit)
        logoutButton = findViewById(R.id.btn_logout)

        // Set up the Spinner with categories
        val categories = arrayOf("Local", "International", "Business", "Sports", "Science", "Technology", "Entertainment", "Lifestyle")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        btnUploadImage.setOnClickListener {
            selectImage.launch("image/*")
        }

        // Set click listener with dialog box
        btnSubmit.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to submit this news?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    val title = etTitle.text.toString().trim()
                    val category = spinnerCategory.selectedItem.toString()
                    val description = etDescription.text.toString().trim()

                    if (title.isNotEmpty() && category.isNotEmpty() && description.isNotEmpty() && selectedImageUri != null) {
                        uploadNewsToFirebase(title, category, description)
                    } else {
                        Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun uploadNewsToFirebase(title: String, category: String, description: String) {
        val newsRef = storage.reference.child("news_images/${UUID.randomUUID()}.jpg")

        val uploadTask = selectedImageUri?.let {
            newsRef.putFile(it)
        }

        uploadTask?.addOnSuccessListener {
            newsRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()

                val reporterEmail = auth.currentUser?.email ?: "unknown"

                val newsDatabaseRef = FirebaseDatabase.getInstance().getReference("news")
                val newsId = newsDatabaseRef.push().key ?: UUID.randomUUID().toString()

                val newsData = News(title, category, description, imageUrl, newsId, reporterEmail)
                saveNewsToDatabase(newsData, newsId)
            }
        }?.addOnFailureListener {
            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveNewsToDatabase(news: News, newsId: String) {
        val newsRef = FirebaseDatabase.getInstance().getReference("news")

        newsRef.child(newsId).setValue(news)
            .addOnSuccessListener {
                Toast.makeText(this, "News uploaded successfully", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload news", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        etTitle.text.clear()
        spinnerCategory.setSelection(0)
        etDescription.text.clear()
        imgPreview.setImageResource(0)
        selectedImageUri = null
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to logout?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                logoutUser()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        val alert = builder.create()
        alert.show()
    }

    private fun logoutUser() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }
}
