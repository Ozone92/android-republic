package com.epita.republic.domain

import com.epita.republic.repository.Player
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.random.Random

// Parties complètes simulées : la partie doit toujours se terminer et l'état rester cohérent
class PlayabilityTest {
    private val maxTurns = 1000

    private fun checkInvariants(engine: GameEngine, context: String) {
        for (player in engine.players) {
            assertTrue("$context: popularity ${player.popularity}", player.popularity in 0..player.maxPopularity)
            assertTrue("$context: vote ${player.vote}", player.vote >= 0)
            assertTrue("$context: money ${player.money}", player.money >= 0)
            assertTrue("$context: influence ${player.influence}", player.influence >= 0)
        }
        engine.inCity?.let { assertTrue("$context: dead player in the Elysee", it.isAlive()) }

        // Chaque carte existe exactement une fois : dans le shop ou chez un joueur
        val allCards = engine.shop.cardsInShop() + engine.players.flatMap { it.cards }
        assertEquals("$context: cards ${allCards.sorted()}", Card.entries.sorted(), allCards.sorted())
    }

    private fun playGame(engine: GameEngine, seed: Int): Int = runBlocking {
        var turns = 0
        checkInvariants(engine, "seed $seed start")
        while (!engine.isFinished()) {
            engine.playNextTurn()
            turns++
            checkInvariants(engine, "seed $seed turn $turns")
            if (turns > maxTurns) {
                fail("seed $seed: game did not end after $maxTurns turns")
            }
        }
        turns
    }

    @Test
    fun botGamesAlwaysEndWithAConsistentState() {
        repeat(500) { seed ->
            val engine = GameEngine(bots(), Random(seed))
            playGame(engine, seed)

            val winner = engine.winner()
            val alive = engine.players.count { it.isAlive() }
            if (winner == null) {
                // Seul cas sans vainqueur : tout le monde éliminé en même temps (Dissolution)
                assertEquals("seed $seed", 0, alive)
            } else {
                assertTrue("seed $seed", winner.isAlive())
                assertTrue("seed $seed", winner.hasWin() || alive == 1)
            }
        }
    }

    @Test
    fun everyCardGetsBoughtDuringSimulatedGames() {
        val bought = mutableSetOf<Card>()
        repeat(300) { seed ->
            val engine = GameEngine(bots(), Random(seed))
            engine.listener = object : GameListener {
                override fun onCardBought(player: Player, card: Card) {
                    bought.add(card)
                }
            }
            playGame(engine, seed)
        }
        assertEquals(Card.entries.toSet(), bought)
    }

    @Test
    fun gamesWithAHumanPlayerUseEveryAction() {
        repeat(200) { seed ->
            val random = Random(seed)
            val human = Player(isBot = false)
            val engine = GameEngine(listOf(human) + bots(3), Random(seed))
            engine.listener = HumanLikeListener(engine, random)
            playGame(engine, seed)
            assertTrue("seed $seed", engine.isFinished())
        }
    }

    @Test
    fun runEndsTheGameAndAnnouncesIt() = runBlocking {
        val phases = mutableListOf<GamePhase>()
        val engine = GameEngine(bots(), Random(42))
        engine.listener = object : GameListener {
            override fun onPhaseChanged(phase: GamePhase) {
                phases.add(phase)
            }
        }

        engine.run()

        assertTrue(engine.isFinished())
        assertEquals(GamePhase.GAME_FINISH, phases.last())
        assertTrue(GamePhase.SHOP in phases)
    }

    // Joue comme un humain au hasard : verrouille des dés, relance, utilise les cartes de dés, achète, renouvelle
    private class HumanLikeListener(val engine: GameEngine, val random: Random) : GameListener {
        override suspend fun onDicePhase(player: Player) {
            if (player.isBot) return
            while (random.nextBoolean() && engine.rerollLeft > 0) {
                engine.toggleLock(random.nextInt(DICE_COUNT))
                engine.roll()
            }
            if (engine.canUseSpinDoctor()) {
                engine.useSpinDoctor(random.nextInt(DICE_COUNT), DiceFace.entries.random(random))
            }
            while (engine.roll()) Unit
            if (engine.canUseTalkingPoints()) {
                engine.useTalkingPoints(random.nextInt(DICE_COUNT))
            }
        }

        override suspend fun onShopPhase(player: Player) {
            if (player.isBot) return
            if (random.nextInt(4) == 0) {
                engine.refreshShop()
            }
            for (slot in 0 until SHOP_SIZE) {
                val card = engine.shop.visible[slot] ?: continue
                val target = if (card.needsTarget) engine.opponents().randomOrNull(random) else null
                engine.buy(slot, target)
            }
        }

        override suspend fun stayInCity(player: Player): Boolean {
            return random.nextBoolean()
        }
    }
}
