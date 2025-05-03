package com.fashion.fashionapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var welcomeText: TextView
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        welcomeText = findViewById(R.id.welcomeText)

        val logoutButton: Button = findViewById(R.id.buttonLogOut)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        checkUserProfile()
    }

    private fun checkUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // User profile exists, go to chat list
                        startActivity(Intent(this, ChatListActivity::class.java))
                        finish()
                    } else {
                        // User profile doesn't exist, go to profile setup
                        startActivity(Intent(this, UserProfileActivity::class.java))
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    // Handle error
                    welcomeText.text = "Error: ${e.message}"
                }
        }
    }
}
