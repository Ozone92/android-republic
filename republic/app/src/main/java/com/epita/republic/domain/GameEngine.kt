package com.epita.republic.domain

import com.epita.republic.repository.Player
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

const val POINT_FOR_VICTORY = 20
const val POINT_CITY_TURN = 2
const val POINT_CITY_ENTER = 1
const val DICE_COUNT = 6
const val REROLL_PER_TURN = 3
const val COMEBACK_POPULARITY = 4
const val SOLID_BASE_MAX_POPULARITY = 12

// Ce que le moteur demande à l'extérieur (UI, attente du joueur humain, délai des bots)
interface GameListener {
    // Joueur humain attaqué à l'Élysée : true = il reste
    suspend fun stayInCity(player: Player): Boolean = true

    suspend fun onDicePhase(player: Player) {}

    suspend fun onShopPhase(player: Player) {}

    fun onPhaseChanged(phase: GamePhase) {}

    fun onCardBought(player: Player, card: Card) {}

    fun onStateChanged() {}
}

class GameEngine(
    val players: List<Player>,
    random: Random = Random.Default,
    val shop: Shop = Shop(random),
    var listener: GameListener = object : GameListener {}
) {
    private val gameLogic = GameLogic(random)

    var currentIndex: Int = 0
        private set
    val current: Player get() = players[currentIndex]
    var inCity: Player? = null
        internal set

    val dice: Array<DiceFace> = Array(DICE_COUNT) { DiceFace.ONE }
    val lockedDice: MutableSet<Int> = mutableSetOf()
    var rerollLeft: Int = REROLL_PER_TURN
        private set
    private var talkingPointsUsed = false
    private var spinDoctorUsed = false

    // ------------------------------------------------------------------ tour de jeu

    suspend fun run() {
        while (!isFinished()) {
            playNextTurn()
        }
        listener.onPhaseChanged(GamePhase.GAME_FINISH)
    }

    suspend fun playNextTurn() {
        val player = current
        if (player.isAlive()) {
            startTurn()
            listener.onPhaseChanged(GamePhase.DICE)
            listener.onDicePhase(player)
            resolveDice()
            listener.onStateChanged()

            if (!isFinished() && player.isAlive()) {
                listener.onPhaseChanged(GamePhase.SHOP)
                listener.onShopPhase(player)
                if (player.isBot) {
                    playBotShop()
                }
                listener.onStateChanged()
            }
            endTurn()
        }
        nextTurn()
        listener.onStateChanged()
    }

    fun startTurn() {
        players.forEach { it.guardUsed = false }
        talkingPointsUsed = false
        spinDoctorUsed = false

        if (inCity === current) {
            current.vote += POINT_CITY_TURN
            if (current.has(Card.WELL_OILED_MACHINE)) {
                current.vote += 1
            }
        }
        if (current.has(Card.LOCAL_NETWORK)) {
            current.money += 1
        }

        rerollLeft = REROLL_PER_TURN
        lockedDice.clear()
        roll()
        listener.onStateChanged()
    }

    fun endTurn() {
        if (inCity == null && current.isAlive()) {
            enterCity(current)
        }
    }

    fun nextTurn() {
        currentIndex = (currentIndex + 1) % players.size
    }

    fun isFinished(): Boolean {
        return players.any { it.isAlive() && it.hasWin() } || players.count { it.isAlive() } <= 1
    }

    fun winner(): Player? {
        return players.filter { it.isAlive() && it.hasWin() }.maxByOrNull { it.vote }
            ?: players.singleOrNull { it.isAlive() }
    }

    // ------------------------------------------------------------------ dés

    // Retourne true si le dé est maintenant déverrouillé
    fun toggleLock(index: Int): Boolean {
        return if (lockedDice.remove(index)) {
            true
        } else {
            lockedDice.add(index)
            false
        }
    }

    fun roll(): Boolean {
        val freeDice = (0 until DICE_COUNT).filter { it !in lockedDice }
        if (rerollLeft <= 0 || freeDice.isEmpty()) {
            return false
        }
        val faces = gameLogic.rollDice(freeDice.size)
        freeDice.forEachIndexed { i, diceIndex -> dice[diceIndex] = faces[i] }
        rerollLeft--
        return true
    }

    // Éléments de langage : après le dernier lancer, relancer un dé
    fun canUseTalkingPoints(): Boolean {
        return current.has(Card.TALKING_POINTS) && !talkingPointsUsed && rerollLeft == 0
    }

    fun useTalkingPoints(index: Int): Boolean {
        if (!canUseTalkingPoints()) {
            return false
        }
        dice[index] = gameLogic.rollDice(1)[0]
        talkingPointsUsed = true
        return true
    }

    // Spin doctor : changer un dé sur la face de son choix
    fun canUseSpinDoctor(): Boolean {
        return current.has(Card.SPIN_DOCTOR) && !spinDoctorUsed
    }

    fun useSpinDoctor(index: Int, face: DiceFace): Boolean {
        if (!canUseSpinDoctor()) {
            return false
        }
        dice[index] = face
        spinDoctorUsed = true
        return true
    }

    suspend fun resolveDice() {
        val player = current
        val count = DiceFace.entries.associateWith { face -> dice.count { it == face } }

        // Série de 1 / 2 / 3 : votes, et Influence pour les séries de 1 et de 2
        for ((face, points, influence) in listOf(
            Triple(DiceFace.ONE, 1, 2),
            Triple(DiceFace.TWO, 2, 1),
            Triple(DiceFace.THREE, 3, 0)
        )) {
            val n = count.getValue(face)
            if (n >= 3) {
                player.vote += points + n - 3
                player.influence += influence
                if (player.has(Card.BUZZ)) {
                    player.vote += 1
                }
            }
        }

        val healCount = count.getValue(DiceFace.HEAL)
        if (inCity !== player) {
            heal(player, healCount)
        }
        if (player.has(Card.CLIENTELISM)) {
            player.money += healCount
        }
        player.money += count.getValue(DiceFace.MONEY)

        var damage = count.getValue(DiceFace.DAMAGE)
        if (damage >= 2 && player.has(Card.HENCHMAN)) {
            damage += 1
        }
        if (damage > 0) {
            attack(player, damage)
        }
    }

    private suspend fun attack(attacker: Player, damage: Int) {
        if (inCity === attacker) {
            for (player in players) {
                if (player !== attacker && player.isAlive()) {
                    hurt(player, damage, attacker)
                }
            }
        } else {
            val occupant = inCity ?: return
            hurt(occupant, damage, attacker)
            if (inCity === occupant && !stayInCity(occupant)) {
                leaveCity(occupant)
            }
        }
    }

    private suspend fun stayInCity(player: Player): Boolean {
        return if (player.isBot) {
            !player.isBotGoOut()
        } else {
            listener.stayInCity(player)
        }
    }

    // ------------------------------------------------------------------ stats

    // attacker != null : dégâts d'une attaque (Garde rapprochée, Effet Streisand)
    fun hurt(victim: Player, amount: Int, attacker: Player? = null) {
        if (!victim.isAlive()) {
            return
        }
        var damage = amount
        if (attacker != null && victim.has(Card.BODYGUARD) && !victim.guardUsed) {
            damage -= 1
            victim.guardUsed = true
        }
        if (damage > 0) {
            victim.popularity = max(0, victim.popularity - damage)
            if (victim.popularity == 0) {
                knockOut(victim)
            }
        }
        if (attacker != null && attacker !== victim && victim.has(Card.STREISAND_EFFECT)) {
            hurt(attacker, 1)
        }
    }

    private fun knockOut(player: Player) {
        if (player.has(Card.COMEBACK)) {
            player.cards.remove(Card.COMEBACK)
            shop.discard(Card.COMEBACK)
            player.popularity = min(COMEBACK_POPULARITY, player.maxPopularity)
        } else if (inCity === player) {
            inCity = null
        }
    }

    fun heal(player: Player, amount: Int) {
        if (player.isAlive()) {
            player.popularity = min(player.maxPopularity, player.popularity + amount)
        }
    }

    private fun loseVotes(player: Player, amount: Int): Int {
        val lost = min(amount, player.vote)
        player.vote -= lost
        return lost
    }

    fun enterCity(player: Player) {
        inCity = player
        player.vote += POINT_CITY_ENTER
    }

    fun leaveCity(player: Player) {
        if (inCity === player) {
            inCity = null
            if (player.has(Card.GOLDEN_SINECURE)) {
                player.money += 3
            }
        }
    }

    // ------------------------------------------------------------------ shop

    fun opponents(player: Player = current): List<Player> {
        return players.filter { it !== player && it.isAlive() }
    }

    fun isPlayable(card: Card, buyer: Player = current): Boolean {
        return when (card) {
            Card.PALACE_REVOLUTION -> inCity !== buyer
            Card.SOLID_BASE -> buyer.maxPopularity < SOLID_BASE_MAX_POPULARITY
            else -> !card.needsTarget || opponents(buyer).isNotEmpty()
        }
    }

    fun canBuy(slot: Int): Boolean {
        val card = shop.visible.getOrNull(slot) ?: return false
        return current.money >= card.cost && isPlayable(card)
    }

    fun buy(slot: Int, target: Player? = null): Boolean {
        if (!canBuy(slot)) {
            return false
        }
        val card = shop.visible[slot]!!
        val buyer = current
        if (card.needsTarget && (target == null || target !in opponents(buyer))) {
            return false
        }

        buyer.money -= card.cost
        shop.take(slot)
        if (card.type == CardType.PERMANENT) {
            buyer.cards.add(card)
            if (card == Card.SOLID_BASE) {
                buyer.maxPopularity = SOLID_BASE_MAX_POPULARITY
            }
        } else {
            applyImmediate(card, buyer, target)
            shop.discard(card)
        }
        listener.onCardBought(buyer, card)
        return true
    }

    fun refreshShop(): Boolean {
        if (current.money < SHOP_REFRESH_COST) {
            return false
        }
        current.money -= SHOP_REFRESH_COST
        shop.refresh()
        return true
    }

    private fun applyImmediate(card: Card, buyer: Player, target: Player?) {
        when (card) {
            Card.PARTY_POLL -> buyer.vote += 2
            Card.MINISTER_POACHING -> buyer.vote += loseVotes(target!!, 2)
            Card.FARM_FAIR_SELFIE -> heal(buyer, 3)
            Card.FUNDRAISING_DINNER -> buyer.money += 6
            Card.LIVE_CLASH -> {
                buyer.vote += 2
                opponents(buyer).forEach { hurt(it, 1) }
            }
            Card.PALACE_REVOLUTION -> {
                val occupant = inCity
                if (occupant != null) {
                    hurt(occupant, 3)
                    leaveCity(occupant)
                }
                enterCity(buyer)
            }
            Card.PRESS_LEAK -> loseVotes(target!!, 2)
            Card.DISSOLUTION -> {
                players.filter { it.isAlive() }.forEach { hurt(it, 2) }
                buyer.vote += 2
            }
            Card.NIGHT_TWEET -> {
                loseVotes(target!!, 1)
                hurt(buyer, 1)
            }
            Card.SUNNY_SUNDAY -> opponents(buyer).forEach { loseVotes(it, 2) }
            Card.THEODULE_COMMITTEE -> {
                shop.refresh()
                buyer.money += 2
            }
            Card.HOLOGRAM_RALLY -> {
                buyer.vote += 2
                heal(buyer, 2)
            }
            else -> error("$card is not an immediate card")
        }
    }

    // ------------------------------------------------------------------ bots

    // Achète la carte la plus chère possible tant qu'il a l'argent
    fun playBotShop() {
        while (true) {
            val slot = shop.visible.indices
                .filter { canBuy(it) && botWants(shop.visible[it]!!) }
                .maxByOrNull { shop.visible[it]!!.cost } ?: return
            buy(slot, botTarget())
        }
    }

    // Vise l'adversaire qui a le plus de votes
    fun botTarget(): Player? {
        return opponents().maxByOrNull { it.vote }
    }

    private fun botWants(card: Card): Boolean {
        return when (card) {
            Card.DISSOLUTION -> current.popularity > 2
            Card.NIGHT_TWEET -> current.popularity > 1
            else -> true
        }
    }
}
