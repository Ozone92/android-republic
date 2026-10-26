package com.epita.republic.domain

import com.epita.republic.domain.DiceFace.DAMAGE
import com.epita.republic.domain.DiceFace.HEAL
import com.epita.republic.domain.DiceFace.MONEY
import com.epita.republic.domain.DiceFace.ONE
import com.epita.republic.domain.DiceFace.THREE
import com.epita.republic.domain.DiceFace.TWO
import com.epita.republic.repository.Player
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameRulesTest {
    private val engine = GameEngine(bots(), Random(1))
    private val me = engine.players[0]
    private val other = engine.players[1]

    @Test
    fun seriesOfThreeGivesVotesAndInfluence() = runBlocking {
        engine.setDice(ONE, ONE, ONE, ONE, MONEY, HEAL)
        engine.resolveDice()

        assertEquals(2, me.vote) // 1 + 1 dé supplémentaire
        assertEquals(2, me.influence)
    }

    @Test
    fun twoSeriesInTheSameRollBothCount() = runBlocking {
        engine.setDice(ONE, ONE, ONE, TWO, TWO, TWO)
        engine.resolveDice()

        assertEquals(1 + 2, me.vote)
        assertEquals(2 + 1, me.influence)
    }

    @Test
    fun seriesOfThreesGivesNoInfluence() = runBlocking {
        engine.setDice(THREE, THREE, THREE, ONE, TWO, MONEY)
        engine.resolveDice()

        assertEquals(3, me.vote)
        assertEquals(0, me.influence)
    }

    @Test
    fun moneyFacesGiveMoney() = runBlocking {
        engine.setDice(MONEY, MONEY, ONE, TWO, THREE, HEAL)
        engine.resolveDice()

        assertEquals(2, me.money)
    }

    @Test
    fun healIsCappedAtMaxPopularity() = runBlocking {
        me.popularity = 8
        engine.setDice(HEAL, HEAL, HEAL, ONE, TWO, THREE)
        engine.resolveDice()

        assertEquals(10, me.popularity)
    }

    @Test
    fun noHealInsideTheElysee() = runBlocking {
        engine.enterCity(me)
        me.popularity = 5
        engine.setDice(HEAL, HEAL, ONE, TWO, THREE, MONEY)
        engine.resolveDice()

        assertEquals(5, me.popularity)
    }

    @Test
    fun attackFromOutsideOnlyHitsTheOccupant() = runBlocking {
        engine.enterCity(other)
        engine.setDice(DAMAGE, DAMAGE, ONE, TWO, THREE, MONEY)
        engine.resolveDice()

        assertEquals(8, other.popularity)
        assertEquals(10, me.popularity)
        assertEquals(10, engine.players[2].popularity)
    }

    @Test
    fun attackFromInsideHitsEveryoneElseButNotTheAttacker() = runBlocking {
        engine.enterCity(me)
        engine.setDice(DAMAGE, ONE, ONE, TWO, THREE, MONEY)
        engine.resolveDice()

        assertEquals(10, me.popularity)
        engine.players.drop(1).forEach { assertEquals(9, it.popularity) }
    }

    @Test
    fun attackWithEmptyElyseeDoesNothing() = runBlocking {
        engine.setDice(DAMAGE, DAMAGE, DAMAGE, ONE, TWO, THREE)
        engine.resolveDice()

        engine.players.forEach { assertEquals(10, it.popularity) }
    }

    @Test
    fun botLeavesTheElyseeUnderHalfPopularity() = runBlocking {
        engine.enterCity(other)
        other.popularity = 6
        engine.setDice(DAMAGE, DAMAGE, ONE, TWO, THREE, MONEY)
        engine.resolveDice()

        assertEquals(4, other.popularity)
        assertNull(engine.inCity)
    }

    @Test
    fun humanIsAskedToStayOrLeave() = runBlocking {
        val human = Player(isBot = false)
        val asked = mutableListOf<Player>()
        val game = GameEngine(listOf(Player(isBot = true), human), Random(1))
        game.listener = object : GameListener {
            override suspend fun stayInCity(player: Player): Boolean {
                asked.add(player)
                return false
            }
        }
        game.enterCity(human)
        game.setDice(DAMAGE, ONE, ONE, TWO, THREE, MONEY)
        game.resolveDice()

        assertEquals(listOf(human), asked)
        assertNull(game.inCity)
    }

    @Test
    fun eliminatedOccupantLeavesTheElysee() = runBlocking {
        engine.enterCity(other)
        other.popularity = 2
        engine.setDice(DAMAGE, DAMAGE, DAMAGE, ONE, TWO, THREE)
        engine.resolveDice()

        assertEquals(0, other.popularity)
        assertFalse(other.isAlive())
        assertNull(engine.inCity)
    }

    @Test
    fun enteringTheElyseeGivesOneVoteAndStartingThereGivesTwo() {
        engine.endTurn()
        assertSame(me, engine.inCity)
        assertEquals(POINT_CITY_ENTER, me.vote)

        repeat(engine.players.size) { engine.nextTurn() }
        engine.startTurn()
        assertEquals(POINT_CITY_ENTER + POINT_CITY_TURN, me.vote)
    }

    @Test
    fun rollRespectsLocksAndRerollCount() {
        engine.startTurn()
        assertEquals(REROLL_PER_TURN - 1, engine.rerollLeft)
        val kept = engine.dice[0]
        engine.toggleLock(0)

        repeat(5) { engine.roll() }

        assertEquals(0, engine.rerollLeft)
        assertEquals(kept, engine.dice[0])
        assertFalse(engine.roll())
    }

    @Test
    fun gameEndsAtTwentyVotesOrWithOneCandidateLeft() {
        assertFalse(engine.isFinished())

        me.vote = POINT_FOR_VICTORY
        assertTrue(engine.isFinished())
        assertSame(me, engine.winner())

        me.vote = 0
        engine.players.drop(1).forEach { it.popularity = 0 }
        assertTrue(engine.isFinished())
        assertSame(me, engine.winner())
    }

    @Test
    fun deadCandidateWithTwentyVotesDoesNotWin() {
        other.vote = POINT_FOR_VICTORY
        other.popularity = 0

        assertFalse(engine.isFinished())
    }
}
