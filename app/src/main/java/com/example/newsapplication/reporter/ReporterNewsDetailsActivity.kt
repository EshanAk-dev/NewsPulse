package com.example.newsapplication.reporter

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.newsapplication.News
import com.example.newsapplication.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class ReporterNewsDetailsActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvDescription: TextView
    private lateinit var imgNews: ImageView
    private lateinit var tvPublishTime: TextView  // To show publish time
    private lateinit var btnDelete: Button

    private var news: News? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reporter_news_details)

        // Initialize UI elements
        tvTitle = findViewById(R.id.tv_Title)
        tvCategory = findViewById(R.id.tv_Category)
        tvDescription = findViewById(R.id.tv_Description)
        imgNews = findViewById(R.id.img_News)
        tvPublishTime = findViewById(R.id.tv_PublishTime)  // Initialize publish time text view
        btnDelete = findViewById(R.id.btn_Delete)

        // Retrieve the news object passed from the previous activity
        news = intent.getSerializableExtra("news") as? News

        news?.let {
            // Set the data to the views
            tvTitle.text = it.title
            tvCategory.text = it.category
            tvDescription.text = it.description

            // Load the image using Glide
            Glide.with(this)
                .load(it.imageUrl)
                .into(imgNews)

            // Show the publish time if news is published
            if (it.status == "published") {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(it.timestamp))
                tvPublishTime.text = "Published on: $formattedDate"
            } else {
                tvPublishTime.text = "Not Published Yet"
            }
        }

        // Delete Button click listener with confirmation dialog
        btnDelete.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Delete News")
                .setMessage("Are you sure you want to delete this news?")
                .setPositiveButton("Yes") { _, _ ->
                    news?.let {
                        val newsRef = FirebaseDatabase.getInstance().getReference("news")
                        val newsId = it.newsId // Get news id
                        val imageUrl = it.imageUrl // Get image URL of the news

                        // Delete the image from Firebase Storage if it exists
                        if (imageUrl.isNotEmpty()) {
                            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                            storageReference.delete()
                                .addOnSuccessListener {
                                    // If image deletion is successful, delete the news from Realtime Database
                                    newsRef.child(newsId).removeValue()
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "News and image deleted successfully", Toast.LENGTH_SHORT).show()
                                            refresh() // Refresh the RecyclerView in ReporterActivity
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Failed to delete news from database", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener {
                                    // If deleting the image fails, still delete the news
                                    Toast.makeText(this, "Failed to delete image from storage", Toast.LENGTH_SHORT).show()

                                    // Proceed to delete the news from the database
                                    newsRef.child(newsId).removeValue()
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "News deleted successfully, but image deletion failed", Toast.LENGTH_SHORT).show()
                                            refresh()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Failed to delete news from database", Toast.LENGTH_SHORT).show()
                                        }
                                }
                        } else {
                            // If there is no image associated, just delete the news from database
                            newsRef.child(newsId).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "News deleted successfully", Toast.LENGTH_SHORT).show()
                                    refresh()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to delete news from database", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
                .setNegativeButton("No", null)
                .create()
            alertDialog.show()
        }
    }

    private fun refresh() {
        // After deletion, refresh the RecyclerView in ReporterActivity
        val intent = Intent(this, ReporterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

}
