package com.epita.republic.views.game

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.epita.republic.R
import com.epita.republic.domain.Character
import com.epita.republic.repository.Player
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

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
    private val awayTextView: TextView? = null,

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

    // While the player sits in the Élysée, the seat stays in place as a dashed placeholder
    fun setAway(away: Boolean) {
        val contentVisibility = if (away) View.INVISIBLE else View.VISIBLE
        listOf(imageView, influenceTextView, popularityTextView, moneyTextView, voteTextView)
            .forEach { it.visibility = contentVisibility }
        awayTextView?.visibility = if (away) View.VISIBLE else View.GONE

        val surface = MaterialColors.getColor(cardView, com.google.android.material.R.attr.colorSurfaceContainer)
        cardView.setCardBackgroundColor(if (away) ColorUtils.setAlphaComponent(surface, 0xD9) else surface)
        cardView.cardElevation =
            if (away) 0f else context.resources.getDimension(R.dimen.elevation_level1)
        cardView.strokeWidth =
            if (away) 0 else context.resources.getDimensionPixelSize(R.dimen.border_default)
        cardView.getChildAt(0).setBackgroundResource(if (away) R.drawable.bg_seat_away else 0)
        cardView.visibility = View.VISIBLE
    }

    fun render() {
        if (player != null) {
            if (isMiddle) {
                // The Élysée slot keeps its gold look, whoever sits in it
                cardView.setCardBackgroundColor(MaterialColors.getColor(cardView, R.attr.colorElyseeContainer))
                cardView.strokeColor = MaterialColors.getColor(cardView, R.attr.colorElysee)
                cardView.strokeWidth = context.resources.getDimensionPixelSize(R.dimen.border_emphasis)
            } else if (player!!.isBot) {
                cardView.strokeColor = MaterialColors.getColor(cardView, R.attr.colorPlayerBot)
            } else {
                cardView.strokeColor = MaterialColors.getColor(cardView, R.attr.colorPlayerHuman)
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