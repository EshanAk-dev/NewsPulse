package com.example.newsapplication

import android.content.Intent
import android.os.Bundle
import android.widget.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                                    // If role == admin to AdminActivity
                                    "admin" -> startActivity(Intent(this, AdminActivity::class.java))
                                    // If role == reporter to ReporterActivity
                                    "reporter" -> startActivity(Intent(this, ReporterActivity::class.java))
                                    // If role == editor to EditorActivity
                                    "editor" -> startActivity(Intent(this, EditorActivity::class.java))
                                    // This for other roles(user)
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

        // New: Redirect to RegisterActivity when clicking the TextView
        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}