package com.fashion.fashionapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.fashion.fashionapp.models.Message
import com.fashion.fashionapp.models.User
import java.text.SimpleDateFormat
import java.util.*
import android.widget.FrameLayout


class ChatActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: TextInputEditText
    private lateinit var sendButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var toolbarTitle: TextView
    private lateinit var playButton: ImageButton
    private lateinit var messageAdapter: MessageAdapter
    private var chatId: String = ""
    private var otherUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        chatId = intent.getStringExtra("chatId") ?: ""
        otherUserId = intent.getStringExtra("otherUserId") ?: ""

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        backButton = findViewById(R.id.backButton)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        playButton = findViewById(R.id.playGameButton)

        messageAdapter = MessageAdapter()
        messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }

        loadOtherUserInfo()
        loadMessages()

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageInput.text?.clear()
            }
        }
        playButton.setOnClickListener {
            val intent = Intent(this, GamesListActivity::class.java).apply {
                putExtra("chatId", chatId)
                putExtra("otherUserId", otherUserId)
            }
            startActivity(intent)
        }
    }

    private fun loadOtherUserInfo() {
        firestore.collection("users")
            .document(otherUserId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                toolbarTitle.text = user?.username ?: "Unknown User"
            }
    }

    private fun loadMessages() {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: listOf()

                messageAdapter.submitList(messages)
                messagesRecyclerView.scrollToPosition(messages.size - 1) // Scroll to the newest message
            }
    }

    private fun sendMessage(messageText: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val message = hashMapOf(
                "senderId" to currentUser.uid,
                "receiverId" to otherUserId,
                "message" to messageText,
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false
            )

            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener { documentRef ->
                    // Update last message in chat document
                    firestore.collection("chats")
                        .document(chatId)
                        .update(
                            mapOf(
                                "lastMessage" to messageText,
                                "lastMessageTime" to message["timestamp"],
                                "lastMessageSenderId" to currentUser.uid
                            )
                        )
                }
        }
    }
}

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private var messages: List<Message> = listOf()
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun submitList(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageContainer: View = itemView.findViewById(R.id.messageContainer)
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageTime: TextView = itemView.findViewById(R.id.messageTime)

        fun bind(message: Message) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val isCurrentUser = message.senderId == currentUser?.uid

            messageContainer.layoutParams = (messageContainer.layoutParams as FrameLayout.LayoutParams).apply {
                gravity = if (isCurrentUser) android.view.Gravity.END else android.view.Gravity.START
            }
            if (isCurrentUser) {
                messageContainer.setBackgroundResource(R.drawable.bg_current_user)
                messageText.setTextColor(Color.WHITE)
                messageTime.setTextColor(Color.WHITE)
            } else {
                messageContainer.setBackgroundResource(R.drawable.bg_other_user)
                messageText.setTextColor(Color.BLACK)
                messageTime.setTextColor(Color.BLACK)
            }

            messageText.text = message.message
            messageTime.text = dateFormat.format(Date(message.timestamp))
        }
    }
} 