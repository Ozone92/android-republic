package com.epita.republic.views.ending

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.epita.republic.domain.EndPlayerState
import com.epita.republic.repository.EndingPlayerInfo
import com.epita.republic.views.game.POINT_FOR_VICTORY

class EndingViewModel: ViewModel() {
    val winnerPlayer: MutableLiveData<EndingPlayerInfo?> = MutableLiveData(null)
    val playersLost: MutableLiveData<Array<EndingPlayerInfo>> = MutableLiveData(arrayOf())

    fun parseResult(result: Array<EndingFragmentParameter>) {
        val firstPlayer = result[0]
        var i = 0
        if (firstPlayer.popularity > 0) {
            i = 1
            var winningState = EndPlayerState.WIN_LAST_ALIVE
            if (firstPlayer.vote >= POINT_FOR_VICTORY) {
                winningState = EndPlayerState.WIN_VOTE
            }
            winnerPlayer.value = EndingPlayerInfo(firstPlayer, winningState)
        }

        val playersList = mutableListOf<EndingPlayerInfo>()
        while (i < result.size) {
            val playerResult = result[i]
            var playerLostState = EndPlayerState.LOST_UNPOPULAR
            if (playerResult.popularity > 0) {
                playerLostState = EndPlayerState.LOST_NO_VOTE
            }
            playersList.add(EndingPlayerInfo(playerResult, playerLostState))
            i++
        }

        playersLost.value = playersList.toTypedArray()
    }
}