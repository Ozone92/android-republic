package com.epita.republic.views.game

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epita.republic.domain.DiceFace
import com.epita.republic.domain.GameLogic
import com.epita.republic.domain.InfluenceLogic
import com.epita.republic.domain.InfluenceStep
import com.epita.republic.domain.SignalController
import com.epita.republic.repository.InfluenceCard
import com.epita.republic.repository.Player
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

class GameViewModel : ViewModel() {
    private val gameLogic = GameLogic()
    private val influenceLogic = InfluenceLogic()
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

    fun diceButtonClick(index: Int) {
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

        Log.d("GAME", "Score Gain: ${scoreGain} | Influence Gain: ${influenceGain}")

        if (currentPlayer.value != inCity.value) {
            Log.d("GAME", "Healing player by ${healthCount}")
            currentPlayer.value!!.popularity += healthCount
        }

        currentPlayer.value!!.influence += (influenceGain)

        currentPlayer.value!!.vote += scoreGain
        currentPlayer.value!!.money += moneyCount
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
            Log.d(
                "GAME",
                "Inflict $damageCount damage to player ${playerInCity.playerIndex} (in city) ${playerInCity}"
            )
            playerInCity.popularity -= damageCount
            Log.d("GAME", "Player ${playerInCity.playerIndex} health left: ${playerInCity}")

            if (!playerInCity.isAlive()) {
                inCity.value = null
                deadPlayers.add(currentPlayer.value!!)
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

    // GAME LOOP
    init {
        viewModelScope.launch {
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
                        player.influence += 1
                    }

                    if (player.influenceCards.contains(InfluenceCard.UNDECIDED)) {
                        rerollLeft.value = 4
                    } else {
                        rerollLeft.value = 3
                    }

                    lockedDiceIndexes.clear()
                    rollDice()
                    currentPhase.value = GamePhase.DICE
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
                    // waitForInput() TODO Add shop view

                    endTurn()
                }

                currentPlayerTurnIndex.value = (currentPlayerTurnIndex.value!! + 1) % players.size
            }

            currentPhase.value = (GamePhase.GAME_FINISH)
        }
    }
}