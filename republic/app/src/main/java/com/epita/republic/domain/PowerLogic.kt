package com.epita.republic.domain

import com.epita.republic.repository.Player
import com.epita.republic.repository.PowerCard
import com.epita.republic.repository.PowerCardType
import com.epita.republic.repository.PowerEffect

sealed interface BotShopAction {
    data class Buy(val slotIndex: Int) : BotShopAction
    data object Reroll : BotShopAction
    data object Stop : BotShopAction
}

const val BOT_MIN_MONEY_AFTER_REROLL = 3

class PowerLogic {
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

    fun isPower(card: PowerCard): Boolean = card.type == PowerCardType.POWER

    fun discardEliminatedPlayer(player: Player, shop: PowerShop) {
        shop.discard(player.powerCards)
        player.powerCards.clear()
        player.influenceCards.clear()
        player.money = 0
    }

    // Permanent bonuses: summed over the player's POWER cards, so duplicates stack.
    fun bonusDamage(player: Player): Int = sumOf<PowerEffect.BonusDamage>(player)
    fun bonusHeal(player: Player): Int = sumOf<PowerEffect.BonusHeal>(player)
    fun bonusMoney(player: Player): Int = sumOf<PowerEffect.BonusMoney>(player)
    fun extraRerolls(player: Player): Int = sumOf<PowerEffect.ExtraReroll>(player)

    fun chooseBotShopAction(bot: Player, shop: PowerShop): BotShopAction {
        val cards = shop.visibleCards
        val bestSlot = cards.indices
            .filter { shop.canBuy(bot, it) && isUsefulFor(cards[it]!!, bot) }
            .maxByOrNull { cards[it]!!.cost }
        if (bestSlot != null) return BotShopAction.Buy(bestSlot)
        if (shop.canReroll(bot) && bot.money - POWER_SHOP_REROLL_COST >= BOT_MIN_MONEY_AFTER_REROLL) {
            return BotShopAction.Reroll
        }
        return BotShopAction.Stop
    }

    private fun isUsefulFor(card: PowerCard, bot: Player): Boolean =
        !(card.effect is PowerEffect.Heal && bot.popularity >= bot.maxPopularity)

    private inline fun <reified T : PowerEffect> sumOf(player: Player): Int =
        player.powerCards.map { it.effect }.filterIsInstance<T>().sumOf { it.amount }
}
