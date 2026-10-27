package com.epita.republic.domain

import kotlin.random.Random

class GameLogic {
    fun rollDice(numberOfDice: Int): Array<DiceFace> {
        return Array(numberOfDice) { DiceFace.entries[Random.nextInt(0, 6)] }
    }

    fun hasReachInfluenceStep(oldInfluence: Int, newInfluence: Int): InfluenceStep? {
        for (step in InfluenceStep.entries) {
            if (step.requiredInfluence in (oldInfluence + 1)..newInfluence) {
                return step
            }
        }

        return null
    }

}