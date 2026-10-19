package com.epita.republic.views.game

import android.content.Context
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.epita.republic.R
import com.epita.republic.repository.Player

class PlayerCard(
    private val context: Context,
    private val playerIndex: Int,
    private var player: Player?,

    private val cardView: CardView,
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
            nameTextView.text = context.getString(R.string.game_player_card_name, playerIndex)
            influenceTextView.text = context.getString(R.string.game_player_card_influence, player!!.influence)
            popularityTextView.text = context.getString(R.string.game_player_card_popularity, player!!.popularity, player!!.maxPopularity)
            moneyTextView.text = context.getString(R.string.game_player_card_money, player!!.money)
            voteTextView.text = context.getString(R.string.game_player_card_vote, player!!.vote)
        }
    }

    fun getPlayer(): Player? {
        return player
    }

    fun setPlayer(newPlayer: Player) {
        player = newPlayer
        render()
    }
}