package com.fashion.fashionapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GamesListActivity : AppCompatActivity() {
    private lateinit var gamesRecyclerView: RecyclerView
    private lateinit var gamesAdapter: GamesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_list)

        gamesRecyclerView = findViewById(R.id.gamesRecyclerView)
        val chatId = intent.getStringExtra("chatId")
        val otherUserId = intent.getStringExtra("otherUserId")


        // Sample games list
        val games = listOf(
            Game("Skribble", "Draw and Guess with your friends", "https://skribbl.io/", R.drawable.skribble),
            Game("2048", "The 2048 online game", "https://play2048.co/", R.drawable.ic_launcher_background),
            Game("Code Name", "Play spy game with friends", "https://codenames.game/", R.drawable.codename),
            Game("Playing Cards", "Card games to play online", "https://playingcards.io/", R.drawable.playingcards)
        )

        gamesAdapter = GamesAdapter { game ->
            val intent = Intent(this, GameWebViewActivity::class.java)
            intent.putExtra("gameTitle", game.title)
            intent.putExtra("gameUrl", game.url)

            if (!chatId.isNullOrEmpty() && !otherUserId.isNullOrEmpty()) {
                intent.putExtra("chatId", chatId)
                intent.putExtra("otherUserId", otherUserId)
            }
            startActivity(intent)
        }

        gamesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@GamesListActivity)
            adapter = gamesAdapter
        }

        gamesAdapter.submitList(games)
    }
}

data class Game(
    val title: String,
    val description: String,
    val url: String,
    val imageResId: Int
)

class GamesAdapter(private val onGameClick: (Game) -> Unit) :
    RecyclerView.Adapter<GamesAdapter.GameViewHolder>() {

    private var games: List<Game> = listOf()

    fun submitList(newGames: List<Game>) {
        games = newGames
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(games[position])
    }

    override fun getItemCount() = games.size

    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameIcon: ImageView = itemView.findViewById(R.id.gameIcon)
        private val gameTitle: TextView = itemView.findViewById(R.id.gameTitle)
        private val gameDescription: TextView = itemView.findViewById(R.id.gameDescription)

        fun bind(game: Game) {
            gameTitle.text = game.title
            gameDescription.text = game.description
            gameIcon.setImageResource(game.imageResId)
            itemView.setOnClickListener { onGameClick(game) }
        }
    }
} 