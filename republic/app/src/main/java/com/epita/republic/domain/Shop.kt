package com.epita.republic.domain

import kotlin.random.Random

const val SHOP_SIZE = 3
const val SHOP_REFRESH_COST = 2

// Pioche + défausse + 3 cartes visibles. Pioche vide => on mélange la défausse pour refaire une pioche.
class Shop(private val random: Random = Random.Default, cards: Collection<Card> = Card.entries) {
    private val deck = ArrayDeque(cards.shuffled(random))
    private val discardPile = mutableListOf<Card>()
    private val slots = MutableList<Card?>(SHOP_SIZE) { null }

    val visible: List<Card?> get() = slots
    val deckSize: Int get() = deck.size
    val discardSize: Int get() = discardPile.size

    init {
        for (i in slots.indices) {
            slots[i] = draw()
        }
    }

    // Toutes les cartes encore dans le shop (pioche, défausse, visibles)
    fun cardsInShop(): List<Card> {
        return deck + discardPile + slots.filterNotNull()
    }

    fun take(slot: Int): Card {
        val card = slots[slot] ?: error("Empty shop slot $slot")
        slots[slot] = draw()
        return card
    }

    fun discard(card: Card) {
        discardPile.add(card)
    }

    fun refresh() {
        val old = slots.filterNotNull()
        slots.fill(null)
        for (i in slots.indices) {
            slots[i] = draw()
        }
        // Pas assez de cartes : les anciennes passent en défausse et on finit de remplir avec
        discardPile.addAll(old)
        for (i in slots.indices) {
            if (slots[i] == null) {
                slots[i] = draw()
            }
        }
    }

    private fun draw(): Card? {
        if (deck.isEmpty()) {
            deck.addAll(discardPile.shuffled(random))
            discardPile.clear()
        }
        return deck.removeFirstOrNull()
    }
}
