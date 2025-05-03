package com.fashion.fashionapp

import android.content.Intent
import android.os.Bundle
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
                        return@addSnapshotListener
                    }

                    val chats = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Chat::class.java)?.copy(id = doc.id)
                    } ?: listOf()

                    chatAdapter.submitList(chats)
                }
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
                        val user = document.toObject(User::class.java)
                        usernameText.text = user?.username ?: "Unknown User"
                        lastMessageText.text = chat.lastMessage
                        
                        // Load profile image if available
                        user?.profileImageUrl?.let { url ->
                            Glide.with(itemView.context)
                                .load(url)
                                .circleCrop()
                                .into(profileImage)
                        }
                    }
            }

            itemView.setOnClickListener { onChatClick(chat) }
        }
    }
} 