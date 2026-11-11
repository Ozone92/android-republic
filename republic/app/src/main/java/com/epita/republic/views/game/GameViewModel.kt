package com.epita.republic.views.game

import android.util.Log
import com.epita.republic.audio.GameSound
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epita.republic.domain.DiceFace
import com.epita.republic.domain.GameLogic
import com.epita.republic.domain.InfluenceLogic
import com.epita.republic.domain.InfluenceStep
import com.epita.republic.domain.PowerLogic
import com.epita.republic.domain.SignalController
import com.epita.republic.repository.InfluenceCard
import com.epita.republic.repository.Player
import com.epita.republic.repository.PowerCard
import com.epita.republic.repository.PowerEffect
import com.epita.republic.R
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

enum class GamePhase {
    DICE,
    IMPROVE_DICE,
    INFLUENCE,
    SHOP,
    ASK_LEAVE,

    GAME_FINISH
}

const val POINT_FOR_VICTORY = 20
const val POINT_CITY_TURN = 2
const val POINT_CITY_ENTER = 1
const val BOT_SLEEP_ACTION = 3L
const val BOT_REROLL_DELAY_MS = 1000L
const val POWER_SHOP_REROLL_COST = 2

data class PowerShopState(
    val slots: List<PowerCard?>,
    val deckCount: Int,
    val discardCount: Int,
    val ownedCount: Int
)

class GameViewModel : ViewModel() {
    private val soundEvents = Channel<GameSound>(Channel.BUFFERED)
    val sounds = soundEvents.receiveAsFlow()

    private val gameLogic = GameLogic()
    private val influenceLogic = InfluenceLogic()
    private val powerLogic = PowerLogic()
    val signalController = SignalController()
    val players: Array<Player> =
        Array(4) { i -> return@Array Player(playerIndex = i, isBot = i > 0) }
    val currentPlayerTurnIndex: MutableLiveData<Int> = MutableLiveData(0)
    val currentPlayer: MutableLiveData<Player> = MutableLiveData(players[0])
    val inCity: MutableLiveData<Player?> = MutableLiveData(null)
    val currentPhase: MutableLiveData<GamePhase> = MutableLiveData(GamePhase.DICE)
    val dices: MutableLiveData<Array<DiceFace>> =
        MutableLiveData(Array(6) { return@Array DiceFace.ONE })
    val lockedDiceIndexes: MutableSet<Int> = mutableSetOf()
    val rerollLeft: MutableLiveData<Int> = MutableLiveData(3)
    val deadPlayers = mutableSetOf<Player>()
    val availableInfluenceCards: MutableLiveData<Array<InfluenceCard>> = MutableLiveData(arrayOf())

    private val powerDeck = mutableListOf(
        PowerCard(
            "power_1",
            cost = 3,
            effect = PowerEffect.Heal(2)
        ),
        PowerCard(
            "power_2",
            cost = 2,
            effect = PowerEffect.GainMoney(3)
        ),
        PowerCard(
            "power_3",
            cost = 4,
            effect = PowerEffect.GainVote(2)
        ),
        PowerCard(
            "power_4",
            cost = 2,
            effect = PowerEffect.DamageOthers(1)
        ),
        PowerCard(
            "power_5",
            cost = 6,
            effect = PowerEffect.BonusDamage(1)
        ),
        PowerCard(
            "power_6",
            cost = 5,
            effect = PowerEffect.ExtraReroll(1)
        ),
        PowerCard(
            "power_7",
            cost = 4,
            effect = PowerEffect.BonusMoney(1)
        ),
        PowerCard(
            "power_8",
            cost = 4,
            effect = PowerEffect.BonusHeal(1)
        )
    )
    private val powerDiscard = mutableListOf<PowerCard>()
    private val powerShopSlots = MutableList<PowerCard?>(3) { null }
    private val _powerShop = MutableLiveData<PowerShopState>()
    val powerShop: LiveData<PowerShopState> = _powerShop

    private fun drawPowerCard(): PowerCard? {
        if (powerDeck.isEmpty()) {
            powerDeck.addAll(powerDiscard)
            powerDiscard.clear()
        }
        if (powerDeck.isEmpty()) return null
        return powerDeck.removeAt(Random.nextInt(powerDeck.size))
    }

    fun canUsePowerShop(): Boolean {
        val player = currentPlayer.value ?: return false
        return !player.isBot && player.isAlive() &&
            currentPhase.value != GamePhase.GAME_FINISH
    }

    private fun canRerollPowerShop(player: Player): Boolean =
        player.money >= POWER_SHOP_REROLL_COST && (powerDeck.isNotEmpty() || powerDiscard.isNotEmpty())

    private fun publishPowerShop() {
        _powerShop.value = PowerShopState(
            powerShopSlots.toList(), powerDeck.size,
            powerDiscard.size, players[0].powerCards.size
        )
    }

    fun rerollPowerShop() {
        if (!canUsePowerShop()) return
        val player = currentPlayer.value ?: return
        if (!canRerollPowerShop(player)) return
        player.money -= POWER_SHOP_REROLL_COST
        // Reveal the new cards before discarding the old ones, so they cannot be redrawn right away.
        val oldCards = powerShopSlots.filterNotNull()
        powerShopSlots.indices.forEach { powerShopSlots[it] = drawPowerCard() }
        powerDiscard.addAll(oldCards)
        publishPowerShop()
        currentPlayer.value = player
    }

    fun buyPowerCard(slotIndex: Int) {
        if (!canUsePowerShop()) return
        val player = currentPlayer.value ?: return
        purchasePowerCard(player, slotIndex)
    }

    private fun purchasePowerCard(player: Player, slotIndex: Int) {
        if (slotIndex !in powerShopSlots.indices) return
        val card = powerShopSlots[slotIndex] ?: return
        if (player.money < card.cost) return
        player.money -= card.cost
        soundEvents.trySend(GameSound.BUY_CARD)
        if (powerLogic.isKept(card)) {
            player.powerCards.add(card)
            // Extra rerolls count right away if the dice are still being rolled this turn.
            val effect = card.effect
            if (effect is PowerEffect.ExtraReroll && currentPhase.value == GamePhase.DICE) {
                rerollLeft.value = rerollLeft.value!! + effect.amount
            }
        } else {
            // A bought one-time card is used up: it never returns to the deck or the discard pile.
            val othersAlive = players.count { it !== player && it.isAlive() }
            powerLogic.applyOnBuy(card, player, players)
            if (card.effect is PowerEffect.DamageOthers && othersAlive > 0) {
                soundEvents.trySend(GameSound.DAMAGE)
            }
            registerDeaths()
        }
        powerShopSlots[slotIndex] = drawPowerCard()
        publishPowerShop()
        currentPlayer.value = player
        // A card can win the game (enough votes, or every opponent knocked out): end it right away.
        endGameIfOver()
    }

    /** Stops the game loop and shows the results if the game is over. Safe to call more than once. */
    private fun endGameIfOver(): Boolean {
        if (currentPhase.value == GamePhase.GAME_FINISH) return true
        if (!gameFinish()) return false
        gameLoopJob?.cancel()
        currentPhase.value = GamePhase.GAME_FINISH
        return true
    }

    /** Records players killed outside of dice damage (e.g. by a power card). */
    private fun registerDeaths() {
        for (player in players) {
            if (!player.isAlive() && deadPlayers.add(player) && inCity.value == player) {
                inCity.value = null
            }
        }
    }

    fun diceButtonClick(index: Int) {
        if (currentPlayer.value!!.isBot) return
        if (currentPhase.value!! == GamePhase.DICE) {
            if (lockedDiceIndexes.contains(index)) {
                // Dice Unlock
                lockedDiceIndexes.remove(index)
            } else {
                // Dice Lock
                lockedDiceIndexes.add(index)
            }
        } else if (currentPhase.value == GamePhase.IMPROVE_DICE) {
            lockedDiceIndexes.clear()
            lockedDiceIndexes.add(index)
        }
    }

    private fun gameFinish(): Boolean {
        if (currentPlayer.value!!.hasWin()) {
            return true
        }

        val playerAlive = players.count { p -> p.isAlive() }

        return playerAlive <= 1
    }

    fun rollDice() {
        val numberOfReroll = 6 - lockedDiceIndexes.size
        if (rerollLeft.value!! > 0 && numberOfReroll > 0) {
            val currentRoll = dices.value!!
            val newDices = gameLogic.rollDice(numberOfReroll)
            soundEvents.trySend(GameSound.DICE_ROLL)

            var i = 0;
            for (diceIndex in (0..5)) {
                if (!lockedDiceIndexes.contains(diceIndex)) {
                    currentRoll[diceIndex] = newDices[i]
                    i++;
                }
            }

            dices.value = (currentRoll)
            rerollLeft.value = (rerollLeft.value!! - 1)
        }
    }

    private suspend fun handleDiceResult() {
        Log.d("GAME", "Start handleDiceResult")
        val rolledDices: MutableList<DiceFace> = dices.value!!.toMutableList()

        if (currentPlayer.value!!.influenceCards.contains(InfluenceCard.EFFICIENT)) {
            lockedDiceIndexes.clear()
            lockedDiceIndexes.add(0)
            rerollLeft.value = 0
            currentPhase.value = GamePhase.IMPROVE_DICE
            waitForInput()

            rolledDices.add(rolledDices[lockedDiceIndexes.toList()[0]])
        }

        val oneCount = rolledDices.count { face -> face == DiceFace.ONE }
        val twoCount = rolledDices.count { face -> face == DiceFace.TWO }
        val threeCount = rolledDices.count { face -> face == DiceFace.THREE }
        var healthCount = rolledDices.count { face -> face == DiceFace.HEAL }
        var damageCount = rolledDices.count { face -> face == DiceFace.DAMAGE }
        var moneyCount = rolledDices.count { face -> face == DiceFace.MONEY }

        var scoreGain = 0
        var influenceGain = 0
        Log.d(
            "GAME",
            "Dice Result ${oneCount} ONE | $twoCount TWO | ${threeCount} THREE | ${healthCount} HEAL | ${damageCount} DAMAGE | ${moneyCount} MONEY"
        )
        if (oneCount >= 3) {
            scoreGain = 1 + oneCount - 3
            influenceGain = 2

            if (currentPlayer.value!!.influenceCards.contains(InfluenceCard.FRIEND)) {
                scoreGain += 1
            }
        }
        if (twoCount >= 3) {
            scoreGain = 2 + twoCount - 3
            influenceGain = 1
        }
        if (threeCount >= 3) {
            scoreGain = 3 + threeCount - 3
        }

        if (currentPlayer.value!!.influenceCards.contains(InfluenceCard.PRIMARY)) {
            moneyCount *= 2
            healthCount *= 2
        }

        if (currentPlayer.value!!.influenceCards.contains(InfluenceCard.FAR)) {
            damageCount *= 2
        }

        // Power card bonuses only apply when the matching face was rolled.
        val rollingPlayer = currentPlayer.value!!
        if (healthCount > 0) healthCount += powerLogic.bonusHeal(rollingPlayer)
        if (moneyCount > 0) moneyCount += powerLogic.bonusMoney(rollingPlayer)
        if (damageCount > 0) damageCount += powerLogic.bonusDamage(rollingPlayer)

        Log.d("GAME", "Score Gain: ${scoreGain} | Influence Gain: ${influenceGain}")

        if (currentPlayer.value != inCity.value) {
            Log.d("GAME", "Healing player by ${healthCount}")
            currentPlayer.value!!.popularity += healthCount
        }

        currentPlayer.value!!.influence += (influenceGain)

        currentPlayer.value!!.vote += scoreGain
        currentPlayer.value!!.money += moneyCount
        currentPlayer.value = currentPlayer.value
        if (damageCount > 0) {
            onDamage(damageCount)
        }

        Log.d("GAME", "End handleDiceResult")
    }

    suspend fun onDamage(damageCount: Int) {
        if (currentPlayer.value == inCity.value) {
            damageFromInside(damageCount)
        } else {
            damageFromOutside(damageCount)
        }
    }

    fun damageFromInside(damageCount: Int) {
        if (damageCount > 0 && players.any {
                it.playerIndex != currentPlayer.value!!.playerIndex && it.isAlive()
            }) {
            soundEvents.trySend(GameSound.DAMAGE)
        }
        // Damage to everyone outside == all player except myself
        for (player in players) {
            if (player.playerIndex != currentPlayer.value!!.playerIndex) {
                Log.d(
                    "GAME",
                    "Inflict $damageCount damage to player ${player.playerIndex} (outside city) ${player}"
                )
                player.popularity -= damageCount
                Log.d("GAME", "Player ${player.playerIndex} health left: ${player}")
                if (!player.isAlive()) {
                    deadPlayers.add(player)
                }
            }
        }
    }

    suspend fun damageFromOutside(damageCount: Int) {
        val playerInCity = inCity.value
        if (playerInCity != null) {
            if (damageCount > 0 && playerInCity.isAlive()) soundEvents.trySend(GameSound.DAMAGE)
            Log.d(
                "GAME",
                "Inflict $damageCount damage to player ${playerInCity.playerIndex} (in city) ${playerInCity}"
            )
            playerInCity.popularity -= damageCount
            Log.d("GAME", "Player ${playerInCity.playerIndex} health left: ${playerInCity}")

            if (!playerInCity.isAlive()) {
                inCity.value = null
                deadPlayers.add(playerInCity)
            } else {
                if (playerInCity.isBot) {
                    if (playerInCity.isBotGoOut()) {
                        inCity.value = null
                    }
                } else {
                    currentPhase.value = (GamePhase.ASK_LEAVE)
                    signalController.await()
                }
            }
        }
    }

    private suspend fun botRollDice(bot: Player) {
        while (rerollLeft.value!! > 0) {
            delay(BOT_REROLL_DELAY_MS)
            val canHeal = bot != inCity.value && bot.popularity < bot.maxPopularity
            val toLock = gameLogic.chooseBotLockedDice(dices.value!!, canHeal)
            lockedDiceIndexes.clear()
            lockedDiceIndexes.addAll(toLock)
            if (toLock.size == dices.value!!.size) break
            rollDice()
        }
        dices.value = dices.value
    }

    private fun botBuyPowerCard(bot: Player) {
        val slotIndex = powerShopSlots.indices
            .filter { index -> powerShopSlots[index]?.let { it.cost <= bot.money } == true }
            .maxByOrNull { index -> powerShopSlots[index]!!.cost }
            ?: return
        purchasePowerCard(bot, slotIndex)
    }

    suspend fun influencePhase(step: InfluenceStep) {
        availableInfluenceCards.value = influenceLogic.getCards(step)
        waitForInput()
        if (currentPlayer.value!!.isBot) {
            influenceCardChoiceClick(0)
        }
    }

    fun influenceCardChoiceClick(index: Int) {
        val influenceStep = currentPlayer.value!!.getInfluenceStep()!!
        val card = availableInfluenceCards.value!![index]
        influenceLogic.removeCard(influenceStep, card)
        currentPlayer.value!!.influenceCards.add(card)
        influenceLogic.handleImmediateCards(currentPlayerTurnIndex.value!!, players, card)

        signalController.sendNotify()
    }

    fun playerExitCityClick() {
        inCity.value = null
        signalController.sendNotify()
    }

    fun enterCity() {
        inCity.value = currentPlayer.value
        currentPlayer.value!!.vote += POINT_CITY_ENTER
    }

    fun endTurn() {
        if (inCity.value == null) {
            enterCity()
        }
    }

    suspend fun waitForInput() {
        Log.d("GAME", "Wait for BOT? ${currentPlayer.value!!.isBot}")
        if (currentPlayer.value!!.isBot) {
            Log.d("GAME", "BOT Waiting...")
            delay(BOT_SLEEP_ACTION.seconds)
        } else {
            signalController.await()
        }
    }

    fun getWinningPlayerOrder(): Array<Player> {
        val playersOrder =
            mutableListOf(
                *players
                    .filter { p -> p.isAlive() }
                    .sortedByDescending { p -> p.vote }
                    .toTypedArray()
            )

        for (player in deadPlayers.reversed()) {
            playersOrder.add(player)
        }

        return playersOrder.toTypedArray()
    }

    private var gameLoopJob: Job? = null

    // GAME LOOP
    init {
        powerShopSlots.indices.forEach { powerShopSlots[it] = drawPowerCard() }
        publishPowerShop()
        gameLoopJob = viewModelScope.launch {
            while (!gameFinish()) {
                Log.d("GAME", "START PLAYER ${currentPlayerTurnIndex.value} TURN")
                val player = players[currentPlayerTurnIndex.value!!]
                currentPlayer.value = player
                if (player.isAlive()) {
                    // Check de debut de tour (si au centre => ajouter points ; passif ; ...)
                    if (player == inCity.value) {
                        player.vote += POINT_CITY_TURN
                    }

                    if (player.influenceCards.contains(InfluenceCard.RAISE)) {
                        player.money += 1
                    }
                    if (player.influenceCards.contains(InfluenceCard.POPULAR)) {
                        player.popularity += 1
                    }

                    val baseRerolls =
                        if (player.influenceCards.contains(InfluenceCard.UNDECIDED)) 4 else 3
                    rerollLeft.value = baseRerolls + powerLogic.extraRerolls(player)

                    lockedDiceIndexes.clear()
                    rollDice()
                    currentPhase.value = GamePhase.DICE
                    if (player.isBot) {
                        botRollDice(player)
                    }
                    waitForInput()
                    val oldInfluence = player.influence
                    handleDiceResult()

                    val newInfluenceStep =
                        gameLogic.hasReachInfluenceStep(oldInfluence, player.influence)
                    if (newInfluenceStep != null && influenceLogic.remainingCardsCount(
                            newInfluenceStep
                        ) > 0
                    ) {
                        currentPhase.value = GamePhase.INFLUENCE
                        influencePhase(newInfluenceStep)
                    }

                    currentPhase.value = GamePhase.SHOP
                    if (player.isBot) {
                        botBuyPowerCard(player)
                        // The bot's card may have ended the game; stop before ending its turn.
                        if (currentPhase.value == GamePhase.GAME_FINISH) return@launch
                    }
                    // waitForInput() TODO Add shop view

                    endTurn()
                }

                currentPlayerTurnIndex.value = (currentPlayerTurnIndex.value!! + 1) % players.size
            }

            endGameIfOver()
        }
    }
}