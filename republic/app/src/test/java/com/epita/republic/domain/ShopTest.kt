package com.epita.republic.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ShopTest {
    @Test
    fun startsWithThreeVisibleCardsAndTheRestInTheDeck() {
        val shop = Shop(Random(1))

        assertEquals(SHOP_SIZE, shop.visible.filterNotNull().size)
        assertEquals(Card.entries.size - SHOP_SIZE, shop.deckSize)
        assertEquals(0, shop.discardSize)
        assertEquals(Card.entries.toSet(), shop.cardsInShop().toSet())
    }

    @Test
    fun takingACardRefillsItsSlotFromTheDeck() {
        val shop = Shop(Random(1))
        val taken = shop.visible[1]!!

        assertEquals(taken, shop.take(1))
        assertTrue(shop.visible.all { it != null })
        assertTrue(taken !in shop.visible)
        assertEquals(Card.entries.size - SHOP_SIZE - 1, shop.deckSize)
    }

    @Test
    fun refreshDiscardsTheVisibleCards() {
        val shop = Shop(Random(1))
        val old = shop.visible.toList()

        shop.refresh()

        assertEquals(SHOP_SIZE, shop.discardSize)
        assertTrue(shop.visible.none { it in old })
        assertEquals(Card.entries.size, shop.cardsInShop().size)
    }

    @Test
    fun emptyDeckIsRebuiltFromTheDiscardPile() {
        val shop = Shop(Random(1), listOf(Card.PARTY_POLL, Card.PRESS_LEAK, Card.BUZZ, Card.SPIN_DOCTOR))
        val first = shop.take(0)
        shop.discard(first)
        // La pioche est vide : la carte suivante vient de la défausse mélangée
        val second = shop.take(0)
        shop.discard(second)

        assertEquals(first, shop.visible[0])
        assertEquals(0, shop.deckSize)
        assertEquals(1, shop.discardSize)
    }

    @Test
    fun refreshWithAnAlmostEmptyDeckStillShowsThreeCards() {
        val shop = Shop(Random(2), listOf(Card.PARTY_POLL, Card.PRESS_LEAK, Card.BUZZ, Card.SPIN_DOCTOR))
        assertEquals(1, shop.deckSize)

        shop.refresh()

        assertEquals(SHOP_SIZE, shop.visible.filterNotNull().size)
        assertEquals(4, shop.cardsInShop().size)
        assertEquals(4, shop.cardsInShop().toSet().size)
    }

    @Test
    fun slotStaysEmptyWhenNoCardIsLeft() {
        val shop = Shop(Random(1), listOf(Card.BUZZ, Card.SPIN_DOCTOR))
        assertNull(shop.visible[2])

        shop.take(0)

        assertNull(shop.visible[0])
        assertNotEquals(null, shop.visible[1])
    }
}
