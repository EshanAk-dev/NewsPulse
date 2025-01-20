package com.example.newsapplication.admin

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.example.newsapplication.DividerSpinnerAdapter
import com.example.newsapplication.LoginActivity
import com.example.newsapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdminActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var roleSpinner: Spinner
    private lateinit var usersListView: ListView
    private lateinit var usersListAdapter: ArrayAdapter<Pair<String, String>> // Stores email and UID
    private val userList = mutableListOf<Pair<String, String>>() // email and UID
    private val userRoles = mutableMapOf<String, String>() // Mapping userId to their role

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
        val roleAdapter = DividerSpinnerAdapter(this, R.layout.spinner_item_with_divider, roles) // Use the custom DividerSpinnerAdapter for the Spinner

        roleSpinner.adapter = roleAdapter // Set the adapter to the Spinner


        // Add New User Button Click
        addUserButton.setOnClickListener {
            val email = newUserEmailEditText.text.toString().trim()
            val password = newUserPasswordEditText.text.toString().trim()
            val role = roleSpinner.selectedItem.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a user in Firebase Authentication and set the role in the Realtime Database
            val auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        // Set the user role in the Firebase Database
                        val userMap = mapOf("email" to email, "role" to role)
                        FirebaseDatabase.getInstance().reference.child("users").child(userId).setValue(userMap)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(this, "User Added Successfully", Toast.LENGTH_SHORT).show()
                                    fetchAllUsers()
                                    newUserEmailEditText.setText("")
                                    newUserPasswordEditText.setText("")
                                    auth.signOut()
                                } else {
                                    Toast.makeText(this, "Failed to add user to database", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(this, "Failed to create user", Toast.LENGTH_SHORT).show()
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
        userRoles.clear() // Clear the userRoles map

        database.reference.child("users").get().addOnSuccessListener { dataSnapshot ->
            for (user in dataSnapshot.children) {
                val email = user.child("email").value.toString()
                val role = user.child("role").value.toString()
                val userId = user.key.toString()

                // Show only admin, reporter, and editor roles
                if (role == "admin" || role == "reporter" || role == "editor") {
                    userList.add(Pair(userId, email))  // Add userId and email to the list
                    userRoles[userId] = role  // Store the role with userId as the key
                }
            }

            // Create a custom adapter for displaying user data with delete button
            usersListAdapter = object : ArrayAdapter<Pair<String, String>>(this, R.layout.list_item_user, userList) {
                override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                    var view = convertView
                    if (view == null) {
                        view = layoutInflater.inflate(R.layout.list_item_user, parent, false)
                    }

                    val user = userList[position]
                    val userEmailTextView = view?.findViewById<TextView>(R.id.tv_userEmail)
                    val userRoleTextView = view?.findViewById<TextView>(R.id.tv_userRole)
                    val deleteButton = view?.findViewById<Button>(R.id.btn_deleteUser)

                    userEmailTextView?.text = user.second // Set email
                    userRoleTextView?.text = userRoles[user.first] // Get and set role based on userId
                    deleteButton?.setOnClickListener {
                        deleteUser(user.first)  // Pass userId to delete user
                    }

                    return view!!
                }
            }

            usersListView.adapter = usersListAdapter
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to delete a user
    private fun deleteUser(userId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete this user?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                // Check if trying to delete the currently authenticated user
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser?.uid == userId) {
                    Toast.makeText(this, "You cannot delete your own account", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Proceed to delete the user from Firebase Database
                database.reference.child("users").child(userId).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "User Deleted Successfully", Toast.LENGTH_SHORT).show()
                        fetchAllUsers()  // Refresh the list
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to Delete User from Database", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }

        val alert = builder.create()
        alert.show()
    }

    // Ask to logout when press the back button
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
