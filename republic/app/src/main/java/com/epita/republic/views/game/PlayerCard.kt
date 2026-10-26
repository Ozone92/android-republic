package com.epita.republic.views.game

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.epita.republic.R
import com.epita.republic.repository.Player

class PlayerCard(
    private val context: Context,
    private var playerIndex: Int,
    private var player: Player?,
    private var playerImageRes: Int?,

    private val cardView: CardView,
    private val imageView: ImageView,
    private val nameTextView: TextView,
    private val influenceTextView: TextView,
    private val popularityTextView: TextView,
    private val moneyTextView: TextView,
    private val voteTextView: TextView
) {
    fun setVisibility(visibility: Int) {
        cardView.visibility = visibility
    }

    fun render() {
        if (player != null) {
            playerImageRes?.let { imageView.setImageResource(it) }

            nameTextView.text = context.getString(R.string.game_player_card_name, playerIndex + 1)
            influenceTextView.text = context.getString(R.string.game_player_card_influence, player!!.influence)
            popularityTextView.text = context.getString(R.string.game_player_card_popularity, player!!.popularity, player!!.maxPopularity)
            moneyTextView.text = context.getString(R.string.game_player_card_money, player!!.money)
            voteTextView.text = context.getString(R.string.game_player_card_vote, player!!.vote)
        }
    }

    fun getPlayer(): Player? {
        return player
    }

    fun getIndex(): Int {
        return playerIndex
    }

    fun getResId(): Int? {
        return playerImageRes
    }

    fun changePlayer(newPlayer: Player, newIndex: Int, newImageRes: Int?) {
        player = newPlayer
        playerIndex = newIndex
        playerImageRes = newImageRes
        render()
    }
}