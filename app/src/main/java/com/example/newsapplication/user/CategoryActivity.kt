package com.example.newsapplication.user

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapplication.News
import com.example.newsapplication.NewsAdapter
import com.example.newsapplication.R
import com.google.firebase.database.*

class CategoryActivity : AppCompatActivity() {

    private lateinit var categoryTitleTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var newsList: MutableList<News>
    private lateinit var database: DatabaseReference
    private lateinit var userRole: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category)

        // Initialize views
        categoryTitleTextView = findViewById(R.id.tv_categoryTitle)
        recyclerView = findViewById(R.id.rv_category)

        // Get the category and user role from the Intent
        val category = intent.getStringExtra("category") ?: "Default Category"
        userRole = intent.getStringExtra("userRole") ?: ""  // Default to "" if no role is passed

        // Set the category title in the TextView
        categoryTitleTextView.text = "$category News"

        // Initialize RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        newsList = mutableListOf()

        newsAdapter = NewsAdapter(newsList, userRole) { news ->
            // Handle click on news item (open the detailed view)
            val intent = Intent(this, UserNewsDetailsActivity::class.java)
            intent.putExtra("news", news)  // Pass the selected news object to the detail page
            startActivity(intent)
        }

        recyclerView.adapter = newsAdapter

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        // Load the news for the selected category and filter by status
        loadNewsByCategory(category, userRole)
    }

    private fun loadNewsByCategory(category: String, userRole: String) {
        // Reference to the news in the database and filter by category and status "published"
        val newsRef = database.child("news").orderByChild("category").equalTo(category)

        // Fetch news from Firebase
        newsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                newsList.clear()

                // Add news to list
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val news = dataSnapshot.getValue(News::class.java)
                        news?.let {
                            // Check if the status is "published"
                            if (it.status == "published") {
                                newsList.add(it)
                            }
                        }
                    }
                    newsAdapter.notifyDataSetChanged()

                    // If the role is "user", show the "No news found" message when the list is empty
                    if (userRole == "user" && newsList.isEmpty()) {
                        Toast.makeText(this@CategoryActivity, "No news found in this category", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // For editor, do not show this message
                    if (userRole == "user") {
                        Toast.makeText(this@CategoryActivity, "No news found in this category", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CategoryActivity, "Failed to load news: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
