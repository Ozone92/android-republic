package com.epita.republic.views.game

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.epita.republic.R
import com.epita.republic.domain.Character
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Reads the selected player's existing state without changing the game or its navigation. */
class PlayerDetailsDialogFragment : DialogFragment() {
    private val gameViewModel: GameViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )
    private val playerIndex: Int get() = requireArguments().getInt(ARG_PLAYER)
    private val character: Character get() = Character.valueOf(
        requireArguments().getString(ARG_CHARACTER)!!
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(24), dp(8), dp(24), dp(8))
        }
        val portrait = ImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = character.name
        }
        content.addView(portrait, LinearLayout.LayoutParams(dp(96), dp(120)))
        val stats = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), 0, 0, 0)
        }
        content.addView(stats, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val details = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), 0, 0, 0)
        }
        content.addView(details, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        fun label(text: CharSequence, heading: Boolean = false, target: LinearLayout = details) {
            target.addView(TextView(context).apply {
                this.text = text
                setTextAppearance(if (heading) com.google.android.material.R.style.TextAppearance_Material3_TitleMedium
                    else com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                setPadding(0, dp(if (heading) 12 else 4), 0, dp(4))
            })
        }
        fun render() {
            val player = gameViewModel.players[playerIndex]
            portrait.setImageResource(if (gameViewModel.inCity.value == player)
                character.centerImageRes else character.defaultImageRes)
            details.removeAllViews()
            stats.removeAllViews()
            label(getString(R.string.game_player_card_popularity, player.popularity, player.maxPopularity), target = stats)
            label(getString(R.string.game_player_card_vote, player.vote), target = stats)
            label(getString(R.string.game_player_card_influence, player.influence), target = stats)
            label(getString(R.string.game_player_card_money, player.money), target = stats)
            label(getString(R.string.player_details_powers), heading = true)
            if (player.powerCards.isEmpty()) label(getString(R.string.player_details_empty))
            player.powerCards.forEach { card ->
                label(getString(card.nameRes), heading = true)
                label(getString(R.string.power_placeholder_description))
            }
            label(getString(R.string.player_details_influence), heading = true)
            if (player.influenceCards.isEmpty()) label(getString(R.string.player_details_empty))
            player.influenceCards.forEach { card ->
                label(getString(card.title), heading = true)
                label(getString(card.description))
            }
        }
        render()
        // The game can continue during inspection (notably bot turns).
        gameViewModel.currentPlayer.observe(this) { render() }
        gameViewModel.currentPhase.observe(this) { render() }
        gameViewModel.inCity.observe(this) { render() }
        gameViewModel.powerShop.observe(this) { render() }

        return MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.player_details_title, playerIndex + 1, character.name))
            .setView(ScrollView(context).apply { addView(content) })
            .setPositiveButton(R.string.player_details_close, null)
            .create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        const val TAG = "PlayerDetailsDialog"
        private const val ARG_PLAYER = "player_index"
        private const val ARG_CHARACTER = "character"

        fun newInstance(playerIndex: Int, character: Character) = PlayerDetailsDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PLAYER, playerIndex)
                putString(ARG_CHARACTER, character.name)
            }
        }
    }
}
