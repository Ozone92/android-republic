package com.epita.republic.views.game

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epita.republic.domain.DiceFace
import com.epita.republic.domain.GameLogic
import com.epita.republic.domain.SignalController
import com.epita.republic.repository.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

enum class GamePhase {
    BOT_TURN,

    DICE,
    EVILNESS,
    SHOP,
    ASK_LEAVE,

    GAME_FINISH
}

const val POINT_FOR_VICTORY = 20
const val POINT_CITY_TURN = 2
const val POINT_CITY_ENTER = 1
const val BOT_SLEEP_ACTION = 1L

class GameViewModel : ViewModel() {
    private val gameLogic = GameLogic()
    private val signalController = SignalController()
    val players: Array<Player> = Array(4) { i -> return@Array Player(isBot = i > 0) }
    val currentPlayerTurnIndex: MutableLiveData<Int> = MutableLiveData(0)
    val currentPlayer: MutableLiveData<Player> = MutableLiveData(players[0])
    val inCity: MutableLiveData<Player?> = MutableLiveData(null)
    val currentPhase: MutableLiveData<GamePhase> = MutableLiveData(GamePhase.DICE)
    val dices: MutableLiveData<Array<DiceFace>> = MutableLiveData(Array(6) { return@Array DiceFace.ONE})
    val rerollLeft: MutableLiveData<Int> = MutableLiveData(3)

    private fun gameFinish(): Boolean {
        if (currentPlayer.value!!.vote >= POINT_FOR_VICTORY) {
            return true
        }

        var playerAlive = 0
        for (player in players) {
            if (player.isAlive()) {
                playerAlive++
            }
        }

        return playerAlive <= 1
    }

    fun rollDice() {
        if (rerollLeft.value!! > 0) {
            rerollLeft.postValue(rerollLeft.value!! - 1)
            dices.postValue(gameLogic.rollDice(6))
        }
    }

    private suspend fun handleDiceResult() {
        val rolledDices = dices.value!!
        val oneCount = rolledDices.count { face -> face == DiceFace.ONE }
        val twoCount = rolledDices.count { face -> face == DiceFace.TWO }
        val threeCount = rolledDices.count { face -> face == DiceFace.THREE }
        val healthCount = rolledDices.count { face -> face == DiceFace.HEAL }
        val damageCount = rolledDices.count { face -> face == DiceFace.DAMAGE }
        val moneyCount = rolledDices.count { face -> face == DiceFace.MONEY }

        var scoreGain = 0
        var influenceGain = 0
        if (oneCount > 3) {
            scoreGain = 1 + oneCount - 3
            influenceGain = 2
        }
        if (twoCount > 3) {
            scoreGain = 2 + twoCount - 3
            influenceGain = 1
        }
        if (threeCount > 3) {
            scoreGain = 3 + threeCount - 3
        }

        if (currentPlayer != inCity.value) {
            currentPlayer.value!!.popularity =
                max(currentPlayer.value!!.popularity + healthCount, currentPlayer.value!!.maxPopularity)
        }

        // TODO Check if gain card
        currentPlayer.value!!.influence += influenceGain

        currentPlayer.value!!.vote += scoreGain
        currentPlayer.value!!.money += moneyCount
        if (damageCount > 0) {
            onDamage(damageCount)
        }
    }

    suspend fun onDamage(damageCount: Int) {
        if (currentPlayer == inCity.value) {
            damageFromInside(damageCount)
        } else {
            damageFromOutside(damageCount)
        }
    }

    fun damageFromInside(damageCount: Int) {
        // Damage to everyone outside == all player except myself
        for (player in players) {
            if (player != currentPlayer) {
                player.popularity -= damageCount
            }
        }
    }

    suspend fun damageFromOutside(damageCount: Int) {
        val playerInCity = inCity.value
        if (playerInCity != null) {
            playerInCity.popularity -= damageCount

            if (playerInCity.popularity <= 0) {
                inCity.postValue(null)
            } else {
                if (playerInCity.isBot) {
                    if (playerInCity.isBotGoOut()) {
                        inCity.postValue(null)
                    }
                } else {
                    currentPhase.postValue(GamePhase.ASK_LEAVE)
                    waitForInput()
                }
            }
        }
    }

    fun enterCity() {
        inCity.postValue(currentPlayer.value!!)
        currentPlayer.value!!.popularity += POINT_CITY_ENTER
    }

    fun endTurn() {
        if (inCity.value == null) {
            enterCity()
        }
    }

    suspend fun waitForInput() {
        if (currentPlayer.value!!.isBot) {
            delay(BOT_SLEEP_ACTION.seconds)
        } else {
            signalController.await()
        }
    }

    // GAME LOOP
    init {
        viewModelScope.launch {
            while (!gameFinish()) {
                currentPlayer.value = players[currentPlayerTurnIndex.value!!]
                if (currentPlayer.value!!.isAlive()) {
                    // Check de debut de tour (si au centre => ajouter points ; passif ; ...)
                    if (currentPlayer.value!! == inCity.value) {
                        currentPlayer.value!!.popularity += POINT_CITY_TURN
                    }

                    rerollLeft.postValue(3)
                    rollDice()
                    currentPhase.postValue(GamePhase.DICE)
                    waitForInput()
                    handleDiceResult()

                    currentPhase.postValue(GamePhase.SHOP)
                    waitForInput()

                    endTurn()
                }
            }
        }
    }
}