package com.fashion.fashionapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.fashion.fashionapp.models.User

class AddUserActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var emailInput: TextInputEditText
    private lateinit var searchButton: Button
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var userAdapter: UserSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        emailInput = findViewById(R.id.emailInput)
        searchButton = findViewById(R.id.searchButton)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)

        userAdapter = UserSearchAdapter { user ->
            createChat(user)
        }

        searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AddUserActivity)
            adapter = userAdapter
        }

        searchButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isNotEmpty()) {
                searchUsers(email)
            } else {
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchUsers(email: String) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.mapNotNull { doc ->
                    doc.toObject(User::class.java).copy(uid = doc.id)
                }
                userAdapter.submitList(users)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createChat(otherUser: User) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val chatId = if (currentUser.uid < otherUser.uid) {
                "${currentUser.uid}_${otherUser.uid}"
            } else {
                "${otherUser.uid}_${currentUser.uid}"
            }

            // First check if chat already exists
            firestore.collection("chats")
                .document(chatId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Toast.makeText(this, "Chat already exists", Toast.LENGTH_SHORT).show()
                        // Start ChatActivity for existing chat
                        val intent = Intent(this, ChatActivity::class.java)
                        intent.putExtra("chatId", chatId)
                        intent.putExtra("otherUserId", otherUser.uid)
                        startActivity(intent)
                        finish()
                    } else {
                        // Create new chat
                        val chat = hashMapOf(
                            "id" to chatId,
                            "participants" to listOf(currentUser.uid, otherUser.uid),
                            "lastMessage" to "Chat started",
                            "lastMessageTime" to System.currentTimeMillis(),
                            "lastMessageSenderId" to currentUser.uid
                        )

                        // Create chat document
                        firestore.collection("chats")
                            .document(chatId)
                            .set(chat)
                            .addOnSuccessListener {
                                // Create initial message
                                val initialMessage = hashMapOf(
                                    "senderId" to currentUser.uid,
                                    "receiverId" to otherUser.uid,
                                    "message" to "Chat started",
                                    "timestamp" to System.currentTimeMillis(),
                                    "isRead" to false
                                )

                                firestore.collection("chats")
                                    .document(chatId)
                                    .collection("messages")
                                    .add(initialMessage)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Chat created successfully", Toast.LENGTH_SHORT).show()
                                        // Start ChatActivity
                                        val intent = Intent(this, ChatActivity::class.java)
                                        intent.putExtra("chatId", chatId)
                                        intent.putExtra("otherUserId", otherUser.uid)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error creating initial message: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error creating chat: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error checking chat: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

class UserSearchAdapter(private val onUserClick: (User) -> Unit) :
    RecyclerView.Adapter<UserSearchAdapter.UserViewHolder>() {

    private var users: List<User> = listOf()

    fun submitList(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(user: User) {
            textView.text = "${user.username} (${user.email})"
            itemView.setOnClickListener { onUserClick(user) }
        }
    }
} 