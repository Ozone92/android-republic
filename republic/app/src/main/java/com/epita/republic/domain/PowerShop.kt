package com.epita.republic.domain

import com.epita.republic.repository.Player
import com.epita.republic.repository.PowerCard
import kotlin.random.Random

const val POWER_SHOP_REROLL_COST = 2
const val POWER_SHOP_SLOT_COUNT = 3

class PowerShop(
    cards: List<PowerCard>,
    private val random: Random = Random.Default,
    private val pricing: (Player, PowerCard) -> Int = { _, card -> card.cost }
) {
    private val deck = cards.toMutableList()
    private val discardPile = mutableListOf<PowerCard>()
    private val slots = MutableList<PowerCard?>(POWER_SHOP_SLOT_COUNT) { null }

    init {
        refillEmptySlots()
    }

    val visibleCards: List<PowerCard?> get() = slots.toList()
    val deckSize: Int get() = deck.size
    val discardSize: Int get() = discardPile.size

    fun priceFor(player: Player, card: PowerCard): Int = pricing(player, card)

    fun canBuy(player: Player, slotIndex: Int): Boolean {
        val card = slots.getOrNull(slotIndex) ?: return false
        return player.money >= priceFor(player, card)
    }

    fun buy(player: Player, slotIndex: Int): PowerCard? {
        if (!canBuy(player, slotIndex)) {
            return null
        }
        val card = slots[slotIndex]!!
        player.money -= priceFor(player, card)
        slots[slotIndex] = draw()
        return card
    }

    fun canReroll(player: Player): Boolean =
        player.money >= POWER_SHOP_REROLL_COST && (deck.isNotEmpty() || discardPile.isNotEmpty())

    fun reroll(player: Player): Boolean {
        if (!canReroll(player)) {
            return false
        }
        player.money -= POWER_SHOP_REROLL_COST
        val oldCards = slots.filterNotNull()
        slots.indices.forEach { slots[it] = draw() }
        discardPile.addAll(oldCards)
        return true
    }

    fun canAct(player: Player): Boolean =
        slots.indices.any { canBuy(player, it) } || canReroll(player)

    fun discard(cards: Collection<PowerCard>) {
        discardPile.addAll(cards)
        refillEmptySlots()
    }

    private fun refillEmptySlots() {
        slots.indices.filter { slots[it] == null }.forEach { slots[it] = draw() }
    }

    private fun draw(): PowerCard? {
        if (deck.isEmpty()) {
            deck.addAll(discardPile)
            discardPile.clear()
        }
        if (deck.isEmpty()) {
            return null
        }
        return deck.removeAt(random.nextInt(deck.size))
    }
}
