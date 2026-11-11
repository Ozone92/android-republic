package com.epita.republic.domain

import com.epita.republic.repository.Player
import com.epita.republic.repository.PowerCard
import kotlin.random.Random

const val POWER_SHOP_REROLL_COST = 2
const val POWER_SHOP_SLOT_COUNT = 3

/**
 * The power card market (rules p.7): three face-up cards, a deck and a discard pile.
 * A bought card leaves the market for good and is replaced by the next card of the deck.
 */
class PowerShop(cards: List<PowerCard>, private val random: Random = Random.Default) {
    private val deck = cards.toMutableList()
    private val discardPile = mutableListOf<PowerCard>()
    private val slots = MutableList<PowerCard?>(POWER_SHOP_SLOT_COUNT) { null }

    init {
        refillEmptySlots()
    }

    val visibleCards: List<PowerCard?> get() = slots.toList()
    val deckSize: Int get() = deck.size
    val discardSize: Int get() = discardPile.size

    fun canBuy(player: Player, slotIndex: Int): Boolean {
        val card = slots.getOrNull(slotIndex) ?: return false
        return player.money >= card.cost
    }

    /** Pays for the card in [slotIndex] and refills the slot. Returns null if it cannot be bought. */
    fun buy(player: Player, slotIndex: Int): PowerCard? {
        if (!canBuy(player, slotIndex)) return null
        val card = slots[slotIndex]!!
        player.money -= card.cost
        slots[slotIndex] = draw()
        return card
    }

    /** A reroll needs the money and at least one card left to reveal. */
    fun canReroll(player: Player): Boolean =
        player.money >= POWER_SHOP_REROLL_COST && (deck.isNotEmpty() || discardPile.isNotEmpty())

    /** Pays [POWER_SHOP_REROLL_COST] to discard the face-up cards and reveal new ones. */
    fun reroll(player: Player): Boolean {
        if (!canReroll(player)) return false
        player.money -= POWER_SHOP_REROLL_COST
        // Reveal the new cards before discarding the old ones, so they cannot be redrawn right away.
        val oldCards = slots.filterNotNull()
        slots.indices.forEach { slots[it] = draw() }
        discardPile.addAll(oldCards)
        return true
    }

    /** True if the player can buy a visible card or reroll the shop. */
    fun canAct(player: Player): Boolean =
        slots.indices.any { canBuy(player, it) } || canReroll(player)

    /** Puts cards on the discard pile, e.g. the Power cards of an eliminated player (rules p.6). */
    fun discard(cards: Collection<PowerCard>) {
        discardPile.addAll(cards)
        // An empty market shows the new cards right away.
        refillEmptySlots()
    }

    private fun refillEmptySlots() {
        slots.indices.filter { slots[it] == null }.forEach { slots[it] = draw() }
    }

    /** Draws a random card. When the deck is empty, the discard pile is shuffled back into it. */
    private fun draw(): PowerCard? {
        if (deck.isEmpty()) {
            deck.addAll(discardPile)
            discardPile.clear()
        }
        if (deck.isEmpty()) return null
        return deck.removeAt(random.nextInt(deck.size))
    }
}
