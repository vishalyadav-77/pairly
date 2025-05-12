package com.fashion.fashionapp

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GameWebViewActivity : AppCompatActivity() {
    private lateinit var gameWebView: WebView
    private lateinit var toolbarTitle: TextView
    private lateinit var chatFragmentContainer: FrameLayout
    private lateinit var openChatFab: ImageButton

    private var isChatVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_webview)

        val gameTitle = intent.getStringExtra("gameTitle") ?: "Game"
        val gameUrl = intent.getStringExtra("gameUrl") ?: ""
        val chatId = intent.getStringExtra("chatId")
        val otherUserId = intent.getStringExtra("otherUserId")

        if (!chatId.isNullOrEmpty() && !otherUserId.isNullOrEmpty()) {
            loadChatFragment(chatId, otherUserId)
        }

        toolbarTitle = findViewById(R.id.toolbarTitle)
        toolbarTitle.text = gameTitle

        chatFragmentContainer = findViewById(R.id.chatFragmentContainer)
        openChatFab = findViewById(R.id.openChatFab)

        gameWebView = findViewById(R.id.gameWebView)
        setupWebView(gameUrl)
        openChatFab.setOnClickListener {
            toggleChatVisibility()
        }
    }

    private fun setupWebView(url: String) {
        gameWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        gameWebView.webViewClient = WebViewClient()
        gameWebView.loadUrl(url)
    }
    private fun toggleChatVisibility() {
        isChatVisible = if (isChatVisible) {
            // Hide the chat container
            chatFragmentContainer.visibility = View.GONE
            false
        } else {
            // Show the chat container
            chatFragmentContainer.visibility = View.VISIBLE
            true
        }
    }
    private fun loadChatFragment(chatId: String, otherUserId: String) {
        if (supportFragmentManager.findFragmentByTag("chatFragment") == null) {
            val chatFragment = ChatFragmentBottom.newInstance(chatId,otherUserId) // Replace with the actual fragment class
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.chatFragmentContainer, chatFragment, "chatFragment")
            transaction.commit()
        }
    }

    override fun onBackPressed() {
        if (gameWebView.canGoBack()) {
            gameWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }
} 