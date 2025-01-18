package com.example.newsapplication

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapplication.admin.AdminActivity
import com.example.newsapplication.editor.EditorActivity
import com.example.newsapplication.reporter.ReporterActivity
import com.example.newsapplication.user.UserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val loginEmailEditText = findViewById<EditText>(R.id.et_email)
        val loginPasswordEditText = findViewById<EditText>(R.id.et_password)
        val loginButton = findViewById<Button>(R.id.btn_login)
        val registerTextView = findViewById<TextView>(R.id.tv_register)

        // Login Button Logic
        loginButton.setOnClickListener {
            val email = loginEmailEditText.text.toString().trim()
            val password = loginPasswordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        database.reference.child("users").child(it).get()
                            .addOnSuccessListener { dataSnapshot ->
                                val role = dataSnapshot.child("role").value.toString()
                                when (role) {
                                    "admin" -> startActivity(Intent(this, AdminActivity::class.java))
                                    "reporter" -> startActivity(Intent(this, ReporterActivity::class.java))
                                    "editor" -> startActivity(Intent(this, EditorActivity::class.java))
                                    else -> startActivity(Intent(this, UserActivity::class.java))
                                }
                                finish()
                            }
                    }
                } else {
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Redirect to RegisterActivity when clicking the TextView
        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        // Check if the back button is pressed within 2 seconds
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed() // Exit the activity
            return
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }

        // Update backPressedTime to current time
        backPressedTime = System.currentTimeMillis()
    }
}
