package com.example.newsapplication.reporter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapplication.LoginActivity
import com.example.newsapplication.News
import com.example.newsapplication.NewsAdapter
import com.example.newsapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ReporterActivity : AppCompatActivity() {

    // Declare variables for UI components
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NewsAdapter
    private lateinit var newsList: MutableList<News>
    private lateinit var btnAddNews: Button
    private lateinit var logoutButton: Button

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("news")
    private val currentUserEmail = auth.currentUser?.email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reporter)

        recyclerView = findViewById(R.id.recyclerView)
        btnAddNews = findViewById(R.id.btn_add_news)
        logoutButton = findViewById(R.id.btn_logout)

        recyclerView.layoutManager = LinearLayoutManager(this)
        newsList = mutableListOf()
        adapter = NewsAdapter(newsList, "reporter") { news -> openNewsDetails(news) }
        recyclerView.adapter = adapter

        btnAddNews.setOnClickListener {
            val intent = Intent(this, AddNewsActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            logoutUser()
        }

        loadReporterNews()
    }

    // Load news for reporter submitted by him
    private fun loadReporterNews() {
        database.orderByChild("reporterEmail").equalTo(currentUserEmail).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                newsList.clear()
                for (newsSnapshot in snapshot.children) {
                    val news = newsSnapshot.getValue(News::class.java)
                    news?.let { newsList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReporterActivity, "Failed to load news", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Open ReporterNewsDetailsActivity
    private fun openNewsDetails(news: News) {
        val intent = Intent(this, ReporterNewsDetailsActivity::class.java)
        intent.putExtra("news", news)
        startActivity(intent)
    }

    // Override the back button press to show a confirmation dialog
    override fun onBackPressed() {
        // Create a confirmation dialog
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to exit?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                // Exit App
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
        startActivity(Intent(this, LoginActivity::class.java))
        Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    // Exit function
    private fun exitApp() {
        auth.signOut()
        finish()
    }
}
