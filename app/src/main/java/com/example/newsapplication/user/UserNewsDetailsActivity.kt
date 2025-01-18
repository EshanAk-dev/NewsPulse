package com.example.newsapplication.user

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.newsapplication.News
import com.example.newsapplication.R

class UserNewsDetailsActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvDescription: TextView
    private lateinit var imgPreview: ImageView
    private lateinit var tvReporterName: TextView  // Added TextView for reporter's name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_news_details)

        // Initialize views
        tvTitle = findViewById(R.id.tv_Title)
        tvCategory = findViewById(R.id.tv_Category)
        tvDescription = findViewById(R.id.tv_Description)
        imgPreview = findViewById(R.id.img_Preview)
        tvReporterName = findViewById(R.id.tv_ReporterName)  // Initialize the new TextView

        // Retrieve the news object passed via Intent
        val news = intent.getSerializableExtra("news") as News

        // Display the news details
        tvTitle.text = news.title
        tvCategory.text = news.category
        tvDescription.text = news.description

        // Load the image using Glide
        Glide.with(this)
            .load(news.imageUrl)
            .into(imgPreview)

        // Set the reporter's name at the end
        val reporterName = news.reporterEmail.split("@")[0]
        tvReporterName.text = "Reported by: $reporterName"
    }
}

