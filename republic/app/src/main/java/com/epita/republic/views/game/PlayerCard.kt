package com.epita.republic.views.game

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.epita.republic.R
import com.epita.republic.domain.Character
import com.epita.republic.repository.Player
import com.google.android.material.card.MaterialCardView

class PlayerCard(
    private val context: Context,
    private var playerIndex: Int,
    private var player: Player?,
    private var playerCharacter: Character?,

    private val cardView: MaterialCardView,
    private val imageView: ImageView,
    private val nameTextView: TextView,
    private val influenceTextView: TextView,
    private val popularityTextView: TextView,
    private val moneyTextView: TextView,
    private val voteTextView: TextView,

    private val isMiddle: Boolean = false,
) {
    init {
        val iconSize = (18 * context.resources.displayMetrics.density).toInt()
        val iconPadding = (4 * context.resources.displayMetrics.density).toInt()
        listOf(
            voteTextView to R.drawable.stat_vote,
            moneyTextView to R.drawable.dice_money,
            popularityTextView to R.drawable.stat_popularity,
            influenceTextView to R.drawable.stat_influence
        ).forEach { (label, resource) ->
            val icon = androidx.appcompat.content.res.AppCompatResources
                .getDrawable(context, resource)?.mutate()
            icon?.setBounds(0, 0, iconSize, iconSize)
            label.setCompoundDrawablesRelative(icon, null, null, null)
            label.compoundDrawablePadding = iconPadding
            label.gravity = android.view.Gravity.CENTER_VERTICAL
        }
    }

    fun setVisibility(visibility: Int) {
        cardView.visibility = visibility
    }

    fun render() {
        if (player != null) {
            if (player!!.isBot) {
                cardView.strokeColor = context.getColor(R.color.card_ia_border)
            } else {
                cardView.strokeColor = context.getColor(R.color.card_player_border)
            }

            playerCharacter?.let {
                if (isMiddle) {
                    imageView.setImageResource(it.centerImageRes)
                } else {
                    imageView.setImageResource(it.defaultImageRes)
                }
            }

            nameTextView.text = context.getString(R.string.game_player_card_name, playerIndex + 1)
            influenceTextView.text =
                context.getString(R.string.game_player_card_influence_value, player!!.influence)
            popularityTextView.text = context.getString(
                R.string.game_player_card_popularity_value,
                player!!.popularity,
                player!!.maxPopularity
            )
            influenceTextView.contentDescription = context.getString(
                R.string.game_player_card_influence, player!!.influence
            )
            popularityTextView.contentDescription = context.getString(
                R.string.game_player_card_popularity, player!!.popularity, player!!.maxPopularity
            )
            moneyTextView.text = context.getString(R.string.game_player_card_money_value, player!!.money)
            voteTextView.text = context.getString(R.string.game_player_card_vote_value, player!!.vote)
            moneyTextView.contentDescription = context.getString(R.string.game_player_card_money, player!!.money)
            voteTextView.contentDescription = context.getString(R.string.game_player_card_vote, player!!.vote)
        }
    }

    fun getPlayer(): Player? {
        return player
    }

    fun getIndex(): Int {
        return playerIndex
    }

    fun getCharacter(): Character? {
        return playerCharacter
    }

    fun changePlayer(newPlayer: Player, newIndex: Int, newCharacter: Character?) {
        player = newPlayer
        playerIndex = newIndex
        playerCharacter = newCharacter
        render()
    }
}