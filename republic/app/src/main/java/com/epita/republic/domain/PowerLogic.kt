package com.epita.republic.domain

import com.epita.republic.repository.Player
import com.epita.republic.repository.PowerCard
import com.epita.republic.repository.PowerCardType
import com.epita.republic.repository.PowerEffect

class PowerLogic {
    /** Applies a DISCARD card's one-shot effect. */
    fun applyOnBuy(card: PowerCard, buyer: Player, players: Array<Player>) {
        when (val effect = card.effect) {
            is PowerEffect.Heal -> buyer.popularity += effect.amount
            is PowerEffect.GainMoney -> buyer.money += effect.amount
            is PowerEffect.GainVote -> buyer.vote += effect.amount
            is PowerEffect.DamageOthers -> players
                .filter { it !== buyer && it.isAlive() }
                .forEach { it.popularity -= effect.amount }

            is PowerEffect.BonusDamage,
            is PowerEffect.BonusHeal,
            is PowerEffect.BonusMoney,
            is PowerEffect.ExtraReroll -> Unit
        }
    }

    fun isKept(card: PowerCard): Boolean = card.type == PowerCardType.KEEP

    // Permanent bonuses: summed over the player's KEEP cards, so duplicates stack.
    fun bonusDamage(player: Player): Int = sumOf<PowerEffect.BonusDamage>(player)
    fun bonusHeal(player: Player): Int = sumOf<PowerEffect.BonusHeal>(player)
    fun bonusMoney(player: Player): Int = sumOf<PowerEffect.BonusMoney>(player)
    fun extraRerolls(player: Player): Int = sumOf<PowerEffect.ExtraReroll>(player)

    private inline fun <reified T : PowerEffect> sumOf(player: Player): Int =
        player.powerCards.map { it.effect }.filterIsInstance<T>().sumOf { it.amount }
}
