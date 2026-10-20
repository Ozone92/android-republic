package com.epita.republic.domain

import kotlin.random.Random

class GameLogic {
    fun rollDice(numberOfDice: Int): Array<DiceFace> {
        return Array(numberOfDice) { DiceFace.entries[Random.nextInt(0, 6)] }
    }
}