package com.example.newsapplication.editor

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapplication.LoginActivity
import com.example.newsapplication.News
import com.example.newsapplication.NewsAdapter
import com.example.newsapplication.R
import com.example.newsapplication.user.UserNewsDetailsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditorActivity : AppCompatActivity() {

    private lateinit var publishedRecyclerView: RecyclerView
    private lateinit var draftRecyclerView: RecyclerView
    private lateinit var publishedNewsAdapter: NewsAdapter
    private lateinit var draftNewsAdapter: NewsAdapter
    private lateinit var publishedNewsList: MutableList<News>
    private lateinit var draftNewsList: MutableList<News>
    private lateinit var logoutButton: Button

    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("news")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private var userRole: String = "editor"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editor)

        // Initialize the RecyclerViews
        publishedRecyclerView = findViewById(R.id.rv_published)
        draftRecyclerView = findViewById(R.id.rv_draft)

        // Set the layout managers
        publishedRecyclerView.layoutManager = LinearLayoutManager(this)
        draftRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the news lists
        publishedNewsList = mutableListOf()
        draftNewsList = mutableListOf()

        // Initialize the adapters
        publishedNewsAdapter = NewsAdapter(publishedNewsList, userRole) { news ->
            // Open the appropriate detail activity based on the user role
            val intent = if (userRole == "editor") {
                Intent(this, NewsDetailActivity::class.java)
            } else {
                Intent(this, UserNewsDetailsActivity::class.java)
            }
            intent.putExtra("news", news)  // Pass the selected news object to the detail page
            startActivity(intent)
        }
        draftNewsAdapter = NewsAdapter(draftNewsList, userRole) { news ->
            // Open the appropriate detail activity based on the user role
            val intent = if (userRole == "editor") {
                Intent(this, NewsDetailActivity::class.java)
            } else {
                Intent(this, UserNewsDetailsActivity::class.java)
            }
            intent.putExtra("news", news)  // Pass the selected news object to the detail page
            startActivity(intent)
        }


        // Set the adapters
        publishedRecyclerView.adapter = publishedNewsAdapter
        draftRecyclerView.adapter = draftNewsAdapter

        loadNewsFromFirebase()

        // Logout button functionality
        logoutButton = findViewById(R.id.btn_logout)
        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadNewsFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the existing lists
                publishedNewsList.clear()
                draftNewsList.clear()

                // Loop through each news item and separate by status
                for (newsSnapshot in snapshot.children) {
                    val news = newsSnapshot.getValue(News::class.java)
                    news?.let {
                        if (it.status == "published") {
                            publishedNewsList.add(it)
                        } else {
                            draftNewsList.add(it)
                        }
                    }
                }

                // Notify adapters to update the RecyclerViews
                publishedNewsAdapter.notifyDataSetChanged()
                draftNewsAdapter.notifyDataSetChanged()

                // Hide RecyclerViews if there are no items
                if (publishedNewsList.isEmpty()) {
                    publishedRecyclerView.visibility = RecyclerView.GONE
                } else {
                    publishedRecyclerView.visibility = RecyclerView.VISIBLE
                }

                if (draftNewsList.isEmpty()) {
                    draftRecyclerView.visibility = RecyclerView.GONE
                } else {
                    draftRecyclerView.visibility = RecyclerView.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditorActivity, "Failed to load news", Toast.LENGTH_SHORT).show()
            }
        })
    }



    // Override the back button press to show a confirmation dialog
    override fun onBackPressed() {
        // Create a confirmation dialog
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to exit?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                // Exit  App
                exitApp()
            }
            .setNegativeButton("No") { dialog, _ ->
                // Dismiss the dialog, do nothing
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

    // Exit function
    private fun exitApp() {
        auth.signOut()
        finish()
    }
}
