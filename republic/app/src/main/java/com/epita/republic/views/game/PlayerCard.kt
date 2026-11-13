package com.epita.republic.views.game

import com.epita.republic.views.common.playerName
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.epita.republic.R
import com.epita.republic.domain.Character
import com.epita.republic.repository.Player
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import kotlin.math.abs

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
    private var shownPlayer: Player? = null
    private var shownStats: IntArray? = null

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

    fun setOnPortraitClick(onClick: (PlayerCard) -> Unit) {
        imageView.isFocusable = true
        imageView.setOnClickListener { onClick(this) }
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

    fun render(isActive: Boolean) {
        if (player != null) {
            if (isMiddle) {
                cardView.setCardBackgroundColor(MaterialColors.getColor(cardView, R.attr.colorElyseeContainer))
            }
            val borderColor = when {
                isActive -> R.attr.colorPlayerHuman
                isMiddle -> R.attr.colorElysee
                player!!.isBot -> R.attr.colorPlayerBot
                else -> com.google.android.material.R.attr.colorOutlineVariant
            }
            cardView.strokeColor = MaterialColors.getColor(cardView, borderColor)
            cardView.strokeWidth = context.resources.getDimensionPixelSize(
                if (isActive || isMiddle) R.dimen.border_emphasis else R.dimen.border_default
            )

            playerCharacter?.let {
                if (isMiddle) {
                    imageView.setImageResource(it.centerImageRes)
                } else {
                    imageView.setImageResource(it.defaultImageRes)
                }
            }

            val playerName = playerCharacter?.let { playerName(context, it, player!!.isBot) } ?: ""
            imageView.contentDescription = context.getString(R.string.player_details_open, playerName)
            nameTextView.text = playerName
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

            showStatChanges(player!!)
        }
    }

    private fun showStatChanges(current: Player) {
        val stats = intArrayOf(current.vote, current.popularity, current.influence, current.money)
        val previous = shownStats
        if (previous != null && shownPlayer === current) {
            val views = listOf(voteTextView, popularityTextView, influenceTextView, moneyTextView)
            for (i in stats.indices) {
                val delta = stats[i] - previous[i]
                if (delta != 0) showDelta(views[i], delta)
            }
        }
        shownPlayer = current
        shownStats = stats
    }

    private fun showDelta(target: TextView, delta: Int) {
        val root = target.rootView as? ViewGroup ?: return
        if (target.visibility != View.VISIBLE) return
        val label = TextView(context).apply {
            text = context.getString(
                if (delta > 0) R.string.stat_gain else R.string.stat_loss,
                abs(delta)
            )
            setTextAppearance(R.style.TextAppearance_Republic_Title)
            setTextColor(
                MaterialColors.getColor(
                    target,
                    if (delta > 0) R.attr.colorFeedbackGain else R.attr.colorFeedbackLoss
                )
            )
        }
        label.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val targetLocation = IntArray(2)
        val rootLocation = IntArray(2)
        target.getLocationInWindow(targetLocation)
        root.getLocationInWindow(rootLocation)
        root.addView(label, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        label.x = (targetLocation[0] - rootLocation[0] + target.width).toFloat() +
            context.resources.getDimension(R.dimen.space_xxs)
        label.y = (targetLocation[1] - rootLocation[1] + (target.height - label.measuredHeight) / 2).toFloat()

        val rise = context.resources.getDimension(R.dimen.space_l)
        label.animate()
            .translationYBy(-rise)
            .alpha(0f)
            .setStartDelay(DELTA_HOLD_MS)
            .setDuration(DELTA_FADE_MS)
            .withEndAction { root.removeView(label) }
            .start()
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

    fun changePlayer(newPlayer: Player, newIndex: Int, newCharacter: Character?, isActive: Boolean) {
        player = newPlayer
        playerIndex = newIndex
        playerCharacter = newCharacter
        render(isActive)
    }

    companion object {
        private const val DELTA_HOLD_MS = 700L
        private const val DELTA_FADE_MS = 900L
    }
}
