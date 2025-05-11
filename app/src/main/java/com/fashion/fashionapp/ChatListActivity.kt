package com.fashion.fashionapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fashion.fashionapp.models.Chat
import com.fashion.fashionapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var chatListRecyclerView: RecyclerView
    private lateinit var addUserButton: ImageButton
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        chatListRecyclerView = findViewById(R.id.chatListRecyclerView)
        addUserButton = findViewById(R.id.addUserButton)

        chatAdapter = ChatAdapter { chat ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", chat.id)
            intent.putExtra("otherUserId", chat.participants.find { it != auth.currentUser?.uid })
            startActivity(intent)
        }

        chatListRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatListActivity)
            adapter = chatAdapter
        }

        addUserButton.setOnClickListener {
            startActivity(Intent(this, AddUserActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadChats()
    }

    private fun loadChats() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("chats")
                .whereArrayContains("participants", currentUser.uid)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Toast.makeText(this, "Error loading chats: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.d("ChatDebug","Error loading chats: ${e.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot == null || snapshot.isEmpty) {
                        Toast.makeText(this, "No chats found", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    val chats = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Chat::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (chats.isEmpty()) {
                        Toast.makeText(this, "No chats found", Toast.LENGTH_SHORT).show()
                    }

                    chatAdapter.submitList(chats)
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

class ChatAdapter(private val onChatClick: (Chat) -> Unit) : 
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var chats: List<Chat> = listOf()

    fun submitList(newChats: List<Chat>) {
        chats = newChats
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position])
    }

    override fun getItemCount() = chats.size

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        private val usernameText: TextView = itemView.findViewById(R.id.usernameText)
        private val lastMessageText: TextView = itemView.findViewById(R.id.lastMessageText)

        fun bind(chat: Chat) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val otherUserId = chat.participants.find { it != currentUser?.uid }

            if (otherUserId != null) {
                FirebaseFirestore.getInstance().collection("users")
                    .document(otherUserId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val user = document.toObject(User::class.java)
                            usernameText.text = user?.username ?: "Unknown User"
                            lastMessageText.text = chat.lastMessage
                            
                            // Load profile image if available
                            user?.profileImageUrl?.let { url ->
                                Glide.with(itemView.context)
                                    .load(url)
                                    .placeholder(R.drawable.account_icon)
                                    .circleCrop()
                                    .into(profileImage)
                            }
                        } else {
                            usernameText.text = "Unknown User"
                            lastMessageText.text = chat.lastMessage
                        }
                    }
                    .addOnFailureListener { e ->
                        usernameText.text = "Error loading user"
                        lastMessageText.text = chat.lastMessage
                    }
            }

            itemView.setOnClickListener { onChatClick(chat) }
        }
    }
} 