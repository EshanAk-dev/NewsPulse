package com.example.newsapplication.admin

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.example.newsapplication.LoginActivity
import com.example.newsapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdminActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var roleSpinner: Spinner
    private lateinit var usersListView: ListView
    private lateinit var usersListAdapter: ArrayAdapter<String>
    private val userList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val newUserEmailEditText = findViewById<EditText>(R.id.et_newUserEmail)
        val newUserPasswordEditText = findViewById<EditText>(R.id.et_newUserPassword)
        val addUserButton = findViewById<Button>(R.id.btn_addUser)
        val logoutButton = findViewById<Button>(R.id.btn_logout)
        usersListView = findViewById(R.id.lv_users)
        roleSpinner = findViewById(R.id.roleSpinner)

        // Spinner for selecting roles
        val roles = arrayOf("admin", "editor", "reporter")
        roleSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        // Setting up ListView Adapter
        usersListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userList)
        usersListView.adapter = usersListAdapter

        // Add New User Button Click
        addUserButton.setOnClickListener {
            val email = newUserEmailEditText.text.toString().trim()
            val password = newUserPasswordEditText.text.toString().trim()
            val role = roleSpinner.selectedItem.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Creating a new user account with Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        database.reference.child("users").child(it).setValue(
                            mapOf("email" to email, "role" to role)
                        ).addOnSuccessListener {
                            Toast.makeText(this, "User Added Successfully", Toast.LENGTH_SHORT).show()
                            fetchAllUsers()
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to Add User", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Logout Button Click
        logoutButton.setOnClickListener {
            logoutUser()
        }

        // Fetch all users on start
        fetchAllUsers()
    }

    // Function to Fetch Only Admin, Reporter, and Editor Users
    private fun fetchAllUsers() {
        userList.clear()
        database.reference.child("users").get().addOnSuccessListener { dataSnapshot ->
            for (user in dataSnapshot.children) {
                val email = user.child("email").value.toString()
                val role = user.child("role").value.toString()

                // Show only admin, reporter, and editor roles
                if (role == "admin" || role == "reporter" || role == "editor") {
                    userList.add("$email - $role")
                }
            }
            usersListAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
        }
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



    // Function to logout the user
    private fun logoutUser() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}
