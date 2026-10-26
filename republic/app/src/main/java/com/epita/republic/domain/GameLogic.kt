package com.epita.republic.domain

import kotlin.random.Random

class GameLogic(private val random: Random = Random.Default) {
    fun rollDice(numberOfDice: Int): Array<DiceFace> {
        return Array(numberOfDice) { DiceFace.entries[random.nextInt(0, 6)] }
    }
}
