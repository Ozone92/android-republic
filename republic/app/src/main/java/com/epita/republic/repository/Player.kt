package com.epita.republic.repository

import com.epita.republic.domain.InfluenceStep
import com.epita.republic.views.game.POINT_FOR_VICTORY
import kotlin.math.max
import kotlin.math.min

class Player(
    val playerIndex: Int,
    val isBot: Boolean,
) {
    // Health
    var popularity: Int = 10
        set(value) {
            field = min(maxPopularity, max(0, value))
        }
    var maxPopularity: Int = 10

    // Evilness
    var influence: Int = 0
        set(value) {
            field = min(10, value)
        }

    // Energy
    var money: Int = 0
        set(value) {
            field = max(0, value)
        }

    // Score/Power
    var vote: Int = 0
        set(value) {
            field = min(POINT_FOR_VICTORY, max(0, value))
        }

    val influenceCards: MutableList<InfluenceCard> = mutableListOf()

    fun isAlive(): Boolean {
        return popularity > 0
    }

    fun hasWin(): Boolean {
        return isAlive() && vote >= POINT_FOR_VICTORY
    }

    fun isBotGoOut(): Boolean {
        return popularity < maxPopularity / 2
    }

    fun getInfluenceStep(): InfluenceStep? {
        for (step in InfluenceStep.entries.reversed()) {
            if (influence >= step.requiredInfluence) {
                return step
            }
        }

        return null
    }
}
