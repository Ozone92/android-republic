package com.epita.republic.domain

import com.epita.republic.domain.DiceFace.DAMAGE
import com.epita.republic.domain.DiceFace.HEAL
import com.epita.republic.domain.DiceFace.MONEY
import com.epita.republic.domain.DiceFace.ONE
import com.epita.republic.domain.DiceFace.THREE
import com.epita.republic.domain.DiceFace.TWO
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CardEffectsTest {

    // ------------------------------------------------------------------ achat

    @Test
    fun buyingCostsTheCardPrice() {
        val engine = engineWith(Card.PARTY_POLL)
        engine.current.money = 5

        assertTrue(engine.buy(engine.shop.visible.indexOf(Card.PARTY_POLL)))
        assertEquals(5 - Card.PARTY_POLL.cost, engine.current.money)
    }

    @Test
    fun cannotBuyWithoutEnoughMoney() {
        val engine = engineWith(Card.SPIN_DOCTOR)
        engine.current.money = Card.SPIN_DOCTOR.cost - 1

        assertFalse(engine.buy(0))
        assertTrue(engine.current.cards.isEmpty())
        assertEquals(Card.SPIN_DOCTOR, engine.shop.visible[0])
    }

    @Test
    fun targetedCardNeedsAnAliveOpponent() {
        val engine = engineWith(Card.PRESS_LEAK)
        val dead = engine.players[1].apply { popularity = 0 }

        assertFalse(engine.buyWithExactMoney(Card.PRESS_LEAK))
        assertFalse(engine.buyWithExactMoney(Card.PRESS_LEAK, engine.current))
        assertFalse(engine.buyWithExactMoney(Card.PRESS_LEAK, dead))
        assertTrue(engine.buyWithExactMoney(Card.PRESS_LEAK, engine.players[2]))
    }

    @Test
    fun immediateCardGoesToDiscardAndPermanentCardToPlayer() {
        val engine = engineWith(Card.PARTY_POLL, Card.BUZZ)

        engine.buyWithExactMoney(Card.PARTY_POLL)
        assertEquals(1, engine.shop.discardSize)
        assertTrue(engine.current.cards.isEmpty())

        engine.buyWithExactMoney(Card.BUZZ)
        // Pioche vide : la défausse (le sondage) est remélangée pour remplir la case
        assertEquals(listOf(Card.BUZZ), engine.current.cards)
        assertTrue(Card.PARTY_POLL in engine.shop.visible)
    }

    @Test
    fun refreshingTheShopCostsTwo() {
        val engine = engineWith(*Card.entries.toTypedArray())
        engine.current.money = 3

        assertTrue(engine.refreshShop())
        assertEquals(1, engine.current.money)
        assertFalse(engine.refreshShop())
    }

    // ------------------------------------------------------------------ immédiates

    @Test
    fun partyPoll() {
        val engine = engineWith(Card.PARTY_POLL)
        engine.buyWithExactMoney(Card.PARTY_POLL)
        assertEquals(2, engine.current.vote)
    }

    @Test
    fun ministerPoachingStealsTwoVotes() {
        val engine = engineWith(Card.MINISTER_POACHING)
        val target = engine.players[1].apply { vote = 5 }

        engine.buyWithExactMoney(Card.MINISTER_POACHING, target)

        assertEquals(3, target.vote)
        assertEquals(2, engine.current.vote)
    }

    @Test
    fun ministerPoachingCannotStealMoreThanTheTargetHas() {
        val engine = engineWith(Card.MINISTER_POACHING)
        val target = engine.players[1].apply { vote = 1 }

        engine.buyWithExactMoney(Card.MINISTER_POACHING, target)

        assertEquals(0, target.vote)
        assertEquals(1, engine.current.vote)
    }

    @Test
    fun farmFairSelfieHealsThreeUpToMax() {
        val engine = engineWith(Card.FARM_FAIR_SELFIE)
        engine.current.popularity = 5
        engine.buyWithExactMoney(Card.FARM_FAIR_SELFIE)
        assertEquals(8, engine.current.popularity)

        val full = engineWith(Card.FARM_FAIR_SELFIE)
        full.current.popularity = 9
        full.buyWithExactMoney(Card.FARM_FAIR_SELFIE)
        assertEquals(10, full.current.popularity)
    }

    @Test
    fun fundraisingDinner() {
        val engine = engineWith(Card.FUNDRAISING_DINNER)
        engine.buyWithExactMoney(Card.FUNDRAISING_DINNER)
        assertEquals(6, engine.current.money)
    }

    @Test
    fun liveClash() {
        val engine = engineWith(Card.LIVE_CLASH)
        engine.buyWithExactMoney(Card.LIVE_CLASH)

        assertEquals(2, engine.current.vote)
        assertEquals(10, engine.current.popularity)
        engine.opponents().forEach { assertEquals(9, it.popularity) }
    }

    @Test
    fun palaceRevolutionTakesTheOccupantsPlace() {
        val engine = engineWith(Card.PALACE_REVOLUTION)
        val occupant = engine.players[1]
        engine.enterCity(occupant)

        engine.buyWithExactMoney(Card.PALACE_REVOLUTION)

        assertEquals(7, occupant.popularity)
        assertSame(engine.current, engine.inCity)
        assertEquals(POINT_CITY_ENTER, engine.current.vote)
    }

    @Test
    fun palaceRevolutionIsNotPlayableFromInsideTheElysee() {
        val engine = engineWith(Card.PALACE_REVOLUTION)
        engine.enterCity(engine.current)
        engine.current.money = 10

        assertFalse(engine.canBuy(0))
    }

    @Test
    fun pressLeak() {
        val engine = engineWith(Card.PRESS_LEAK)
        val target = engine.players[2].apply { vote = 4 }
        engine.buyWithExactMoney(Card.PRESS_LEAK, target)
        assertEquals(2, target.vote)
    }

    @Test
    fun dissolutionHurtsEveryoneIncludingTheBuyer() {
        val engine = engineWith(Card.DISSOLUTION)
        engine.buyWithExactMoney(Card.DISSOLUTION)

        engine.players.forEach { assertEquals(8, it.popularity) }
        assertEquals(2, engine.current.vote)
    }

    @Test
    fun nightTweet() {
        val engine = engineWith(Card.NIGHT_TWEET)
        val target = engine.players[1].apply { vote = 3 }
        engine.buyWithExactMoney(Card.NIGHT_TWEET, target)

        assertEquals(2, target.vote)
        assertEquals(9, engine.current.popularity)
    }

    @Test
    fun sunnySundayOnlyHitsOpponentsAndNeverGoesBelowZero() {
        val engine = engineWith(Card.SUNNY_SUNDAY)
        engine.current.vote = 5
        engine.players[1].vote = 5
        engine.players[2].vote = 1

        engine.buyWithExactMoney(Card.SUNNY_SUNDAY)

        assertEquals(5, engine.current.vote)
        assertEquals(3, engine.players[1].vote)
        assertEquals(0, engine.players[2].vote)
    }

    @Test
    fun theoduleCommitteeRenewsTheShop() {
        val engine = engineWith(*Card.entries.toTypedArray())
        // On place le Comité Théodule dans le shop
        while (Card.THEODULE_COMMITTEE !in engine.shop.visible) {
            engine.shop.refresh()
        }
        val others = engine.shop.visible.filter { it != Card.THEODULE_COMMITTEE }

        engine.buyWithExactMoney(Card.THEODULE_COMMITTEE)

        assertEquals(2, engine.current.money)
        assertTrue(engine.shop.visible.none { it in others })
        assertEquals(Card.entries.size, engine.shop.cardsInShop().size)
    }

    @Test
    fun hologramRally() {
        val engine = engineWith(Card.HOLOGRAM_RALLY)
        engine.current.popularity = 5
        engine.buyWithExactMoney(Card.HOLOGRAM_RALLY)

        assertEquals(2, engine.current.vote)
        assertEquals(7, engine.current.popularity)
    }

    // ------------------------------------------------------------------ permanentes

    @Test
    fun henchmanAddsOneDamageWithTwoAttacks() = runBlocking {
        val engine = engineWith(Card.HENCHMAN)
        engine.buyWithExactMoney(Card.HENCHMAN)
        val occupant = engine.players[1]
        engine.enterCity(occupant)

        engine.setDice(DAMAGE, DAMAGE, ONE, TWO, THREE, MONEY)
        engine.resolveDice()

        assertEquals(7, occupant.popularity)
    }

    @Test
    fun henchmanDoesNothingWithOneAttack() = runBlocking {
        val engine = engineWith(Card.HENCHMAN)
        engine.buyWithExactMoney(Card.HENCHMAN)
        val occupant = engine.players[1]
        engine.enterCity(occupant)

        engine.setDice(DAMAGE, ONE, ONE, TWO, THREE, MONEY)
        engine.resolveDice()

        assertEquals(9, occupant.popularity)
    }

    @Test
    fun talkingPointsRerollsOneDieAfterTheLastRoll() {
        val engine = engineWith(Card.TALKING_POINTS)
        engine.buyWithExactMoney(Card.TALKING_POINTS)
        engine.startTurn()
        assertFalse(engine.canUseTalkingPoints())

        repeat(REROLL_PER_TURN) { engine.roll() }
        assertTrue(engine.canUseTalkingPoints())
        assertTrue(engine.useTalkingPoints(2))
        assertFalse(engine.useTalkingPoints(2))
    }

    @Test
    fun localNetworkGivesMoneyAtTurnStart() {
        val engine = engineWith(Card.LOCAL_NETWORK)
        engine.buyWithExactMoney(Card.LOCAL_NETWORK)
        engine.startTurn()
        assertEquals(1, engine.current.money)
    }

    @Test
    fun bodyguardReducesTheFirstAttackOfTheTurn() = runBlocking {
        val engine = engineWith(Card.BODYGUARD)
        val defender = engine.players[1]
        defender.cards.add(Card.BODYGUARD)
        engine.enterCity(engine.current)

        engine.setDice(DAMAGE, DAMAGE, ONE, TWO, THREE, MONEY)
        engine.resolveDice()
        assertEquals(9, defender.popularity)
        assertEquals(8, engine.players[2].popularity)

        engine.hurt(defender, 2, engine.current)
        assertEquals(7, defender.popularity)

        // Nouveau tour : la garde est de nouveau disponible
        engine.nextTurn()
        engine.startTurn()
        engine.hurt(defender, 2, engine.players[3])
        assertEquals(6, defender.popularity)
    }

    @Test
    fun comebackSavesFromEliminationOnce() {
        val engine = engineWith(Card.COMEBACK)
        val player = engine.players[1]
        player.cards.add(Card.COMEBACK)
        engine.enterCity(player)

        engine.hurt(player, 15)

        assertEquals(COMEBACK_POPULARITY, player.popularity)
        assertFalse(player.has(Card.COMEBACK))
        assertSame(player, engine.inCity)
        assertTrue(Card.COMEBACK in engine.shop.cardsInShop())

        engine.hurt(player, 15)
        assertFalse(player.isAlive())
        assertNull(engine.inCity)
    }

    @Test
    fun goldenSinecureGivesMoneyWhenLeaving() {
        val engine = engineWith(Card.GOLDEN_SINECURE)
        engine.buyWithExactMoney(Card.GOLDEN_SINECURE)
        engine.enterCity(engine.current)

        engine.leaveCity(engine.current)

        assertEquals(3, engine.current.money)
        assertNull(engine.inCity)
    }

    @Test
    fun clientelismGivesMoneyForHealEvenInsideTheElysee() = runBlocking {
        val engine = engineWith(Card.CLIENTELISM)
        engine.buyWithExactMoney(Card.CLIENTELISM)
        engine.enterCity(engine.current)

        engine.setDice(HEAL, HEAL, ONE, TWO, THREE, ONE)
        engine.resolveDice()

        assertEquals(2, engine.current.money)
    }

    @Test
    fun buzzAddsOneVotePerSeries() = runBlocking {
        val engine = engineWith(Card.BUZZ)
        engine.buyWithExactMoney(Card.BUZZ)

        engine.setDice(ONE, ONE, ONE, THREE, THREE, THREE)
        engine.resolveDice()

        assertEquals(1 + 3 + 2, engine.current.vote)
    }

    @Test
    fun solidBaseRaisesMaxPopularityOnce() = runBlocking {
        val engine = engineWith(Card.SOLID_BASE)
        engine.buyWithExactMoney(Card.SOLID_BASE)
        assertEquals(SOLID_BASE_MAX_POPULARITY, engine.current.maxPopularity)

        engine.setDice(HEAL, HEAL, HEAL, ONE, TWO, THREE)
        engine.resolveDice()
        assertEquals(12, engine.current.popularity)
    }

    @Test
    fun streisandEffectHurtsTheAttacker() = runBlocking {
        val engine = engineWith(Card.STREISAND_EFFECT)
        val occupant = engine.players[1]
        occupant.cards.add(Card.STREISAND_EFFECT)
        engine.enterCity(occupant)

        engine.setDice(DAMAGE, ONE, ONE, TWO, THREE, MONEY)
        engine.resolveDice()

        assertEquals(9, occupant.popularity)
        assertEquals(9, engine.current.popularity)
    }

    @Test
    fun wellOiledMachineGivesOneMoreVoteInTheElysee() {
        val engine = engineWith(Card.WELL_OILED_MACHINE)
        engine.buyWithExactMoney(Card.WELL_OILED_MACHINE)
        engine.enterCity(engine.current)

        engine.startTurn()

        assertEquals(POINT_CITY_ENTER + POINT_CITY_TURN + 1, engine.current.vote)
    }

    @Test
    fun spinDoctorSetsADieOncePerTurn() {
        val engine = engineWith(Card.SPIN_DOCTOR)
        engine.buyWithExactMoney(Card.SPIN_DOCTOR)
        engine.startTurn()

        assertTrue(engine.useSpinDoctor(4, DAMAGE))
        assertEquals(DAMAGE, engine.dice[4])
        assertFalse(engine.useSpinDoctor(4, HEAL))

        engine.startTurn()
        assertTrue(engine.canUseSpinDoctor())
    }

    @Test
    fun everyCardHasAnEffectTest() {
        // Garde-fou : chaque carte de l'inventaire existe une seule fois avec un numéro de 1 à 24
        assertEquals((1..24).toList(), Card.entries.map { it.number })
    }
}
