package com.example.newsapplication.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapplication.LoginActivity
import com.example.newsapplication.R
import com.google.firebase.auth.FirebaseAuth

class UserActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user)

        // Check if the user is logged in
        if (auth.currentUser == null) {
            // If not logged in, redirect to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Setup click listeners for each category icon
        findViewById<ImageView>(R.id.icon_local).setOnClickListener {
            navigateToCategoryActivity("Local", "user")
        }
        findViewById<ImageView>(R.id.icon_international).setOnClickListener {
            navigateToCategoryActivity("International", "user")
        }
        findViewById<ImageView>(R.id.icon_business).setOnClickListener {
            navigateToCategoryActivity("Business", "user")
        }
        findViewById<ImageView>(R.id.icon_sports).setOnClickListener {
            navigateToCategoryActivity("Sports", "user")
        }
        findViewById<ImageView>(R.id.icon_science).setOnClickListener {
            navigateToCategoryActivity("Science", "user")
        }
        findViewById<ImageView>(R.id.icon_technology).setOnClickListener {
            navigateToCategoryActivity("Technology", "user")
        }
        findViewById<ImageView>(R.id.icon_entertainment).setOnClickListener {
            navigateToCategoryActivity("Entertainment", "user")
        }
        findViewById<ImageView>(R.id.icon_lifestyle).setOnClickListener {
            navigateToCategoryActivity("Lifestyle", "user")
        }

        // Initialize Logout button
        logoutButton = findViewById(R.id.btn_logout)

        // Logout functionality
        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun navigateToCategoryActivity(category: String, userRole: String) {
        val intent = Intent(this, CategoryActivity::class.java)
        intent.putExtra("category", category)
        intent.putExtra("userRole", userRole)  // Pass user role to CategoryActivity
        startActivity(intent)
    }

    // Override the back button press to show a confirmation dialog
    override fun onBackPressed() {
        // Create a confirmation dialog
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to logout?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                // Logout and go to LoginActivity
                logoutUser()
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
}
