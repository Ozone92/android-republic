package com.epita.republic.domain

import com.epita.republic.repository.InfluenceCard
import com.epita.republic.repository.Player
import com.epita.republic.repository.PowerCard
import com.epita.republic.repository.PowerCardType
import com.epita.republic.repository.PowerEffect
import kotlin.math.max

sealed interface BotShopAction {
    data class Buy(val slotIndex: Int) : BotShopAction
    data object Reroll : BotShopAction
    data object Stop : BotShopAction
}

const val BOT_MIN_MONEY_AFTER_REROLL = 3

class PowerLogic {
    /**
     * Applies a purchase: the ACTION effect or the immediate part of a POWER card, plus the votes
     * the buyer gets per purchase. Taking the Élysée and the extra turn are done by the game.
     */
    fun applyOnBuy(card: PowerCard, buyer: Player, players: Array<Player>) {
        // Only cards already owned count, so a new VotesOnBuy card does not reward itself.
        buyer.vote += sumOf<PowerEffect.VotesOnBuy>(buyer)
        val others = players.filter { it !== buyer && it.isAlive() }
        when (val effect = card.effect) {
            is PowerEffect.Heal -> buyer.popularity += effect.amount
            is PowerEffect.GainMoney -> buyer.money += effect.amount
            is PowerEffect.GainVote, is PowerEffect.TakeCity -> buyer.vote += effect.amount
            is PowerEffect.DamageOthers -> others.forEach { inflictDamage(it, effect.amount) }
            is PowerEffect.VotesForPopularity -> {
                buyer.vote += effect.amount
                inflictDamage(buyer, effect.popularityLoss)
            }
            is PowerEffect.OthersLoseVotes -> others.forEach { it.vote -= effect.amount }
            is PowerEffect.DamageEveryone -> (others + buyer).forEach { inflictDamage(it, effect.amount) }
            is PowerEffect.VotesAndDamageOthers -> {
                buyer.vote += effect.amount
                others.forEach { inflictDamage(it, effect.damage) }
            }
            is PowerEffect.MaxPopularity -> {
                buyer.maxPopularity += effect.amount
                buyer.popularity += effect.amount
            }
            else -> Unit
        }
    }

    /** Removes popularity from [target] after its protections. Returns the popularity really lost. */
    fun inflictDamage(target: Player, amount: Int): Int {
        if (amount <= 0 || !target.isAlive()) return 0
        if (amount == 1 && owns<PowerEffect.IgnoreSmallDamage>(target)) return 0
        val before = target.popularity
        target.popularity -= amount
        val lost = before - target.popularity
        if (lost >= 2) target.money += sumOf<PowerEffect.MoneyWhenHurt>(target)
        return lost
    }

    fun priceFor(player: Player, card: PowerCard): Int {
        var discount = sumOf<PowerEffect.CardDiscount>(player)
        // Influence tile "Amie des lobbies": cards cost 1 less.
        if (InfluenceCard.LOBBY in player.influenceCards) discount += 1
        return max(0, card.cost - discount)
    }

    fun startOfTurnVotes(player: Player, inCity: Boolean): Int =
        if (inCity) sumOf<PowerEffect.CityStartVotes>(player) else 0

    /** Extra votes from the final dice (three 1s or more, all six faces). */
    fun diceVotes(player: Player, faces: List<DiceFace>): Int {
        var votes = 0
        if (faces.count { it == DiceFace.ONE } >= 3) votes += sumOf<PowerEffect.TripleOneBonus>(player)
        if (DiceFace.entries.all { it in faces }) votes += sumOf<PowerEffect.AllFacesVotes>(player)
        return votes
    }

    fun peacefulVotes(player: Player, dealtDamage: Boolean): Int =
        if (dealtDamage) 0 else sumOf<PowerEffect.PeacefulVotes>(player)

    fun applyEndOfTurn(player: Player, players: Array<Player>) {
        if (player.money == 0) player.money += sumOf<PowerEffect.MoneyWhenBroke>(player)
        effects<PowerEffect.VotesPerMoney>(player).forEach { player.vote += player.money / it.amount }
        // Last in votes, ties included.
        val isLast = players.none { it !== player && it.isAlive() && it.vote < player.vote }
        if (isLast) player.vote += sumOf<PowerEffect.UnderdogVotes>(player)
    }

    fun votesForEliminations(player: Player, eliminatedCount: Int): Int =
        sumOf<PowerEffect.VotesOnElimination>(player) * eliminatedCount

    fun isPower(card: PowerCard): Boolean = card.type == PowerCardType.POWER

    fun discardEliminatedPlayer(player: Player, shop: PowerShop) {
        shop.discard(player.powerCards)
        player.powerCards.clear()
        player.influenceCards.clear()
        player.money = 0
    }

    fun bonusDamage(player: Player): Int = sumOf<PowerEffect.BonusDamage>(player)
    fun bonusHeal(player: Player): Int = sumOf<PowerEffect.BonusHeal>(player)
    fun bonusMoney(player: Player): Int = sumOf<PowerEffect.BonusMoney>(player)
    fun extraRerolls(player: Player): Int = sumOf<PowerEffect.ExtraReroll>(player)

    fun chooseBotShopAction(bot: Player, shop: PowerShop): BotShopAction {
        if (!bot.isAlive()) return BotShopAction.Stop
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

    private fun isUsefulFor(card: PowerCard, bot: Player): Boolean = when (val effect = card.effect) {
        is PowerEffect.Heal -> bot.popularity < bot.maxPopularity
        // Never pay to knock yourself out.
        is PowerEffect.VotesForPopularity -> bot.popularity > effect.popularityLoss
        is PowerEffect.DamageEveryone -> bot.popularity > effect.amount
        // A bot that would leave the Élysée does not take it.
        is PowerEffect.TakeCity -> !bot.isBotGoOut()
        else -> true
    }

    private inline fun <reified T : PowerEffect> effects(player: Player): List<T> =
        player.powerCards.map { it.effect }.filterIsInstance<T>()

    private inline fun <reified T : PowerEffect> sumOf(player: Player): Int =
        effects<T>(player).sumOf { it.amount }

    private inline fun <reified T : PowerEffect> owns(player: Player): Boolean =
        effects<T>(player).isNotEmpty()
}
