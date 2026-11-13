package com.epita.republic.views.game

import com.epita.republic.views.common.playerName
import android.content.Context
import android.graphics.Matrix
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.ColorUtils
import com.epita.republic.R
import com.epita.republic.domain.Character
import com.epita.republic.repository.Player
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlin.math.abs
import kotlin.math.max

class PlayerCard(
    private val context: Context,
    private var playerIndex: Int,
    private var player: Player?,
    private var playerCharacter: Character?,
    private val cardView: MaterialCardView,
    private val isMiddle: Boolean = false,
) {
    private val content: ViewGroup = cardView.findViewById(R.id.player_content)
    private val imageView: ImageView = cardView.findViewById(R.id.player_image)
    private val popularityRing: CircularProgressIndicator = cardView.findViewById(R.id.player_popularity_ring)
    private val nameTextView: TextView = cardView.findViewById(R.id.player_name_text)
    private val voteProgress: LinearProgressIndicator = cardView.findViewById(R.id.player_vote_progress)
    private val voteTextView: TextView = cardView.findViewById(R.id.player_vote_text)
    private val popularityTextView: TextView = cardView.findViewById(R.id.player_popularity_text)
    private val moneyTextView: TextView = cardView.findViewById(R.id.player_money_text)
    private val influenceTextView: TextView = cardView.findViewById(R.id.player_influence_text)
    private val awayTextView: TextView = cardView.findViewById(R.id.player_away_text)
    private val youBadge: TextView = cardView.findViewById(R.id.player_you_badge)

    private var shownPlayer: Player? = null
    private var shownStats: IntArray? = null
    private var shownImageRes = 0

    init {
        val iconSize = context.resources.getDimensionPixelSize(R.dimen.size_stat_icon)
        val iconPadding = context.resources.getDimensionPixelSize(R.dimen.space_xxs)
        listOf(
            moneyTextView to R.drawable.dice_money,
            popularityTextView to R.drawable.stat_popularity,
            influenceTextView to R.drawable.stat_influence
        ).forEach { (label, resource) ->
            val icon = AppCompatResources.getDrawable(context, resource)?.mutate()
            icon?.setBounds(0, 0, iconSize, iconSize)
            label.setCompoundDrawablesRelative(icon, null, null, null)
            label.compoundDrawablePadding = iconPadding
            label.gravity = Gravity.CENTER_VERTICAL
        }
        if (isMiddle) {
            content.setPaddingRelative(
                content.paddingStart,
                content.paddingTop + context.resources.getDimensionPixelSize(R.dimen.space_sm),
                content.paddingEnd,
                content.paddingBottom
            )
        }
    }

    fun setOnPortraitClick(onClick: (PlayerCard) -> Unit) {
        imageView.setOnClickListener { onClick(this) }
    }

    fun setVisibility(visibility: Int) {
        cardView.visibility = visibility
    }

    // While the player sits in the Élysée, the seat stays in place as a dashed placeholder
    fun setAway(away: Boolean) {
        val contentVisibility = if (away) View.INVISIBLE else View.VISIBLE
        listOf(
            imageView, popularityRing, popularityTextView, voteProgress,
            voteTextView, moneyTextView, influenceTextView
        ).forEach { it.visibility = contentVisibility }
        youBadge.visibility = if (!away && player?.isBot == false) View.VISIBLE else View.GONE
        awayTextView.visibility = if (away) View.VISIBLE else View.GONE
        nameTextView.text = playerCharacter?.let { context.getString(it.displayName) } ?: ""

        val surface = MaterialColors.getColor(cardView, com.google.android.material.R.attr.colorSurfaceContainer)
        cardView.setCardBackgroundColor(if (away) ColorUtils.setAlphaComponent(surface, 0xD9) else surface)
        cardView.cardElevation =
            if (away) 0f else context.resources.getDimension(R.dimen.elevation_level1)
        cardView.strokeWidth = 0
        content.setBackgroundResource(if (away) R.drawable.bg_seat_away else 0)
        cardView.visibility = View.VISIBLE
    }

    fun render(isActive: Boolean) {
        val current = player ?: return
        val background = if (isMiddle) R.attr.colorElyseeContainer else com.google.android.material.R.attr.colorSurfaceContainer
        cardView.setCardBackgroundColor(MaterialColors.getColor(cardView, background))
        cardView.strokeColor = MaterialColors.getColor(cardView, androidx.appcompat.R.attr.colorPrimary)
        cardView.strokeWidth =
            if (isActive) context.resources.getDimensionPixelSize(R.dimen.border_emphasis) else 0
        cardView.cardElevation = context.resources.getDimension(
            if (isActive) R.dimen.elevation_level3 else R.dimen.elevation_level1
        )

        playerCharacter?.let {
            showPortrait(if (isMiddle) it.centerImageRes else it.defaultImageRes)
        }

        val name = playerCharacter?.let { playerName(context, it, current.isBot) } ?: ""
        imageView.contentDescription = context.getString(R.string.player_details_open, name)
        nameTextView.text = playerCharacter?.let { context.getString(it.displayName) } ?: ""
        youBadge.visibility = if (current.isBot) View.GONE else View.VISIBLE

        val animate = shownPlayer === current
        popularityRing.setIndicatorColor(
            MaterialColors.getColor(cardView, if (current.isBot) R.attr.colorPlayerBot else R.attr.colorPlayerHuman)
        )
        popularityRing.max = current.maxPopularity
        popularityRing.setProgressCompat(current.popularity, animate)
        voteProgress.setProgressCompat(current.vote, animate)

        popularityTextView.text = context.getString(R.string.game_player_card_money_value, current.popularity)
        voteTextView.text = context.getString(R.string.game_player_card_vote_value, current.vote)
        moneyTextView.text = context.getString(R.string.game_player_card_money_value, current.money)
        influenceTextView.text = context.getString(R.string.game_player_card_influence_value, current.influence)
        popularityTextView.contentDescription = context.getString(
            R.string.game_player_card_popularity, current.popularity, current.maxPopularity
        )
        voteTextView.contentDescription = context.getString(R.string.game_player_card_vote, current.vote)
        moneyTextView.contentDescription = context.getString(R.string.game_player_card_money, current.money)
        influenceTextView.contentDescription = context.getString(R.string.game_player_card_influence, current.influence)

        showStatChanges(current)
    }

    private fun showPortrait(imageRes: Int) {
        if (imageRes == shownImageRes) return
        shownImageRes = imageRes
        imageView.setImageResource(imageRes)
        val drawable = imageView.drawable ?: return
        val size = context.resources.getDimension(R.dimen.size_player_portrait)
        val scale = max(size / drawable.intrinsicWidth, size / drawable.intrinsicHeight)
        imageView.imageMatrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate((size - drawable.intrinsicWidth * scale) / 2f, 0f)
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
