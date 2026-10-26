package com.epita.republic.views.game

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epita.republic.domain.Card
import com.epita.republic.domain.DiceFace
import com.epita.republic.domain.GameEngine
import com.epita.republic.domain.GameListener
import com.epita.republic.domain.GamePhase
import com.epita.republic.domain.REROLL_PER_TURN
import com.epita.republic.domain.SignalController
import com.epita.republic.repository.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

const val BOT_SLEEP_ACTION = 3L

class GameViewModel : ViewModel(), GameListener {
    val signalController = SignalController()
    val players: Array<Player> = Array(4) { i -> return@Array Player(isBot = i > 0) }
    val engine = GameEngine(players.toList(), listener = this)

    val currentPlayerTurnIndex: MutableLiveData<Int> = MutableLiveData(0)
    val currentPlayer: MutableLiveData<Player> = MutableLiveData(players[0])
    val inCity: MutableLiveData<Player?> = MutableLiveData(null)
    val currentPhase: MutableLiveData<GamePhase> = MutableLiveData(GamePhase.DICE)
    val dices: MutableLiveData<Array<DiceFace>> = MutableLiveData(engine.dice)
    val rerollLeft: MutableLiveData<Int> = MutableLiveData(REROLL_PER_TURN)
    val shopCards: MutableLiveData<List<Card?>> = MutableLiveData(engine.shop.visible)

    private var stayInCityChoice = true

    fun diceButtonClick(index: Int): Boolean {
        return engine.toggleLock(index)
    }

    fun rollDice() {
        engine.roll()
        onStateChanged()
    }

    fun useTalkingPoints(index: Int) {
        engine.useTalkingPoints(index)
        onStateChanged()
    }

    fun useSpinDoctor(index: Int, face: DiceFace) {
        engine.useSpinDoctor(index, face)
        onStateChanged()
    }

    fun buyCard(slot: Int, targetIndex: Int? = null): Boolean {
        val bought = engine.buy(slot, targetIndex?.let { players[it] })
        onStateChanged()
        return bought
    }

    fun refreshShop(): Boolean {
        val refreshed = engine.refreshShop()
        onStateChanged()
        return refreshed
    }

    fun playerStayClick() {
        stayInCityChoice = true
        signalController.sendNotify()
    }

    fun playerExitCityClick() {
        stayInCityChoice = false
        signalController.sendNotify()
    }

    private suspend fun waitForInput(player: Player) {
        Log.d("GAME", "Wait for BOT? ${player.isBot}")
        if (player.isBot) {
            Log.d("GAME", "BOT Waiting...")
            delay(BOT_SLEEP_ACTION.seconds)
        } else {
            signalController.await()
        }
    }

    override suspend fun onDicePhase(player: Player) {
        waitForInput(player)
    }

    override suspend fun onShopPhase(player: Player) {
        waitForInput(player)
    }

    override suspend fun stayInCity(player: Player): Boolean {
        currentPhase.value = GamePhase.ASK_LEAVE
        signalController.await()
        return stayInCityChoice
    }

    override fun onPhaseChanged(phase: GamePhase) {
        Log.d("GAME", "PLAYER ${engine.currentIndex} -> $phase")
        onStateChanged()
        currentPhase.value = phase
    }

    override fun onStateChanged() {
        currentPlayerTurnIndex.value = engine.currentIndex
        currentPlayer.value = engine.current
        inCity.value = engine.inCity
        dices.value = engine.dice
        rerollLeft.value = engine.rerollLeft
        shopCards.value = engine.shop.visible
    }

    // GAME LOOP
    init {
        viewModelScope.launch {
            engine.run()
        }
    }
}
