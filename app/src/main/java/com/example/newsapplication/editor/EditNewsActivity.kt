package com.example.newsapplication.editor

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapplication.News
import com.example.newsapplication.R
import com.google.firebase.database.FirebaseDatabase

class EditNewsActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button
    private var news: News? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_news)

        // Initialize views
        etTitle = findViewById(R.id.et_Title)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etDescription = findViewById(R.id.et_Description)
        btnSave = findViewById(R.id.btn_Save)

        // Create a list of categories for the Spinner
        val categories = listOf("Local", "International", "Business", "Sports")

        // Set up the Spinner with an ArrayAdapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Retrieve the news object passed from the previous activity
        news = intent.getSerializableExtra("news") as? News

        // Set existing data to the EditTexts
        news?.let {
            etTitle.setText(it.title)
            val categoryPosition = categories.indexOf(it.category)
            spinnerCategory.setSelection(categoryPosition)  // Set the current category
            etDescription.setText(it.description)
        }

        // Handle Save Button click
        btnSave.setOnClickListener {
            val updatedTitle = etTitle.text.toString().trim()
            val updatedCategory = spinnerCategory.selectedItem.toString().trim()
            val updatedDescription = etDescription.text.toString().trim()

            if (updatedTitle.isNotEmpty() && updatedCategory.isNotEmpty() && updatedDescription.isNotEmpty()) {
                // Update the news object with the new values
                news?.let { newsItem ->
                    newsItem.title = updatedTitle
                    newsItem.category = updatedCategory
                    newsItem.description = updatedDescription

                    // Update the news in Firebase Realtime Database
                    val newsRef = FirebaseDatabase.getInstance().getReference("news")
                    newsRef.child(newsItem.newsId).setValue(newsItem)
                        .addOnSuccessListener {
                            Toast.makeText(this, "News updated successfully", Toast.LENGTH_SHORT).show()
                            finish() // Close the EditNewsActivity and return to the previous screen
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to update news", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
