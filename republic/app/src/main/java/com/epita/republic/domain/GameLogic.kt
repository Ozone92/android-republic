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

    fun chooseBotLockedDice(dices: Array<DiceFace>, canHeal: Boolean): Set<Int> {
        val numberFaces = listOf(DiceFace.ONE, DiceFace.TWO, DiceFace.THREE)
        val targetNumber = numberFaces
            .filter { face -> dices.count { it == face } >= 2 }
            .maxWithOrNull(compareBy({ face -> dices.count { it == face } }, { it.ordinal }))

        return dices.indices.filter { index ->
            when (dices[index]) {
                DiceFace.DAMAGE, DiceFace.MONEY -> true
                DiceFace.HEAL -> canHeal
                else -> dices[index] == targetNumber
            }
        }.toSet()
    }
}