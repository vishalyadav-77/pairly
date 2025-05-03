package com.fashion.fashionapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usernameInput: TextInputEditText
    private lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        usernameInput = findViewById(R.id.usernameInput)
        continueButton = findViewById(R.id.continueButton)

        continueButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            if (username.isNotEmpty()) {
                saveUserProfile(username)
            } else {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserProfile(username: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val user = hashMapOf(
                "uid" to currentUser.uid,
                "email" to currentUser.email,
                "username" to username,
                "lastSeen" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(currentUser.uid)
                .set(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile created successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ChatListActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
} 