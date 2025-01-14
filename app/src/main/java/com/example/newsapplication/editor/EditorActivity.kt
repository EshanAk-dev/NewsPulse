package com.example.newsapplication.editor

import android.content.Intent
import android.os.Bundle
import android.widget.*
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

                // Adjust the height of the RecyclerViews based on the number of items
                adjustRecyclerViewHeight(publishedRecyclerView, publishedNewsList.size)
                adjustRecyclerViewHeight(draftRecyclerView, draftNewsList.size)

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

    // Function to adjust the height of the RecyclerView based on the number of items
    private fun adjustRecyclerViewHeight(recyclerView: RecyclerView, itemCount: Int) {
        val itemHeight = recyclerView.getChildAt(0)?.height ?: 0 // Get the height of a single item
        val twoItemsHeight = itemHeight * 2  // Height for 2 items

        // Set the height of the RecyclerView to show at least 2 items (no scroll)
        val layoutParams = recyclerView.layoutParams
        layoutParams.height = when {
            itemCount > 1 -> twoItemsHeight  // Show 2 items without scroll
            itemCount > 0 -> itemHeight // Show as many items as available
            else -> 0 // No items to show
        }
        recyclerView.layoutParams = layoutParams

        // Set up scrolling for more than 2 items
        recyclerView.isNestedScrollingEnabled = itemCount > 2
    }

    // Override the back button press to show a confirmation dialog
    override fun onBackPressed() {
        // Create a confirmation dialog
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to logout?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                // Logout and go to LoginActivity
                logoutUser()
            }
            .setNegativeButton("No") { dialog, id ->
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
        finish()
    }
}
