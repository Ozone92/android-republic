package com.epita.republic.views.game

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.epita.republic.R

class PowerShopDialogFragment : DialogFragment(R.layout.dialog_power_shop) {
    private val gameViewModel: GameViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val status = view.findViewById<TextView>(R.id.power_shop_status)
        val counts = view.findViewById<TextView>(R.id.power_shop_counts)
        val reroll = view.findViewById<Button>(R.id.power_shop_reroll)
        val names = listOf(
            view.findViewById<TextView>(R.id.power_name_1),
            view.findViewById<TextView>(R.id.power_name_2),
            view.findViewById<TextView>(R.id.power_name_3)
        )
        val descriptions = listOf(
            view.findViewById<TextView>(R.id.power_description_1),
            view.findViewById<TextView>(R.id.power_description_2),
            view.findViewById<TextView>(R.id.power_description_3)
        )
        val buttons = listOf(
            view.findViewById<Button>(R.id.power_buy_1),
            view.findViewById<Button>(R.id.power_buy_2),
            view.findViewById<Button>(R.id.power_buy_3)
        )

        fun renderShop() {
            val state = gameViewModel.powerShop.value ?: return
            val canAct = gameViewModel.canUsePowerShop()
            status.setText(if (canAct) R.string.power_shop_allowed else R.string.power_shop_blocked)
            counts.text = getString(R.string.power_shop_counts,
                state.deckCount, state.discardCount, state.ownedCount)
            names.indices.forEach { index ->
                val card = state.slots[index]
                names[index].setText(card?.nameRes ?: R.string.power_shop_empty)
                descriptions[index].visibility = if (card == null) View.GONE else View.VISIBLE
                buttons[index].isEnabled = canAct && card != null
            }
            reroll.isEnabled = canAct && (state.slots.any { it != null } ||
                state.deckCount > 0 || state.discardCount > 0)
        }

        gameViewModel.powerShop.observe(viewLifecycleOwner) { renderShop() }
        gameViewModel.currentPlayer.observe(viewLifecycleOwner) { renderShop() }
        gameViewModel.currentPhase.observe(viewLifecycleOwner) { renderShop() }
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener { gameViewModel.buyPowerCard(index) }
        }
        reroll.setOnClickListener { gameViewModel.rerollPowerShop() }
        view.findViewById<Button>(R.id.power_shop_close).setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        const val TAG = "PowerShopDialog"
    }
}
