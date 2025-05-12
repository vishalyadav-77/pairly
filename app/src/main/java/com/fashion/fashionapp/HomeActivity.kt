package com.fashion.fashionapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var welcomeText: TextView
    private lateinit var logoutButton: Button
    private lateinit var playButton: Button
    private lateinit var chatButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set status bar color to white
        window.statusBarColor = Color.WHITE
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and 
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            
        setContentView(R.layout.activity_home)
        
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        welcomeText = findViewById(R.id.welcomeText)
        chatButton = findViewById(R.id.chatButton)
        playButton = findViewById(R.id.playButton)
        logoutButton = findViewById(R.id.buttonLogOut)

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        chatButton.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }

        playButton.setOnClickListener {
            startActivity(Intent(this, GamesListActivity::class.java))
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
                        // User profile exists, chat button visible
                        chatButton.visibility = View.VISIBLE
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

    override fun onBackPressed() {
        // Exit the app when back is pressed in HomeActivity
        finishAffinity()
    }
}
