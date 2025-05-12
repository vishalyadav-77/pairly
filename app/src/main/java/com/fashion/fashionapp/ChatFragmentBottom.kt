package com.fashion.fashionapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.fashion.fashionapp.models.Message
import com.fashion.fashionapp.models.User

class ChatFragmentBottom : Fragment() {

    private lateinit var userName: TextView
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: TextInputEditText
    private lateinit var sendButton: ImageButton
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var chatId: String = ""
    private var otherUserId: String = ""

    companion object {
        fun newInstance(chatId: String, otherUserId: String): ChatFragmentBottom {
            val fragment = ChatFragmentBottom()
            fragment.arguments = Bundle().apply {
                putString("chatId", chatId)
                putString("otherUserId", otherUserId)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_bottom_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView)
        messageInput = view.findViewById(R.id.messageInput)
        sendButton = view.findViewById(R.id.sendButton)
        userName = view.findViewById(R.id.userName)
        chatId = arguments?.getString("chatId") ?: ""
        otherUserId = arguments?.getString("otherUserId") ?: ""

        messageAdapter = MessageAdapter()
        messagesRecyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        messagesRecyclerView.adapter = messageAdapter

        loadOtherUserInfo()
        loadMessages()

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageInput.text?.clear()
            }
        }
    }

    private fun loadMessages() {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: listOf()
                messageAdapter.submitList(messages)
                messagesRecyclerView.scrollToPosition(messages.size - 1)
//                if (messages.isNotEmpty()) {
//                    messagesRecyclerView.scrollToPosition(messages.size - 1)
//                }
            }
    }
    private fun loadOtherUserInfo() {
        firestore.collection("users")
            .document(otherUserId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                userName.text = user?.username ?: "Unknown User"
            }
    }

    private fun sendMessage(messageText: String) {
        val currentUser = auth.currentUser ?: return
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
            .addOnSuccessListener {
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

