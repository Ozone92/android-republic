package com.epita.republic.domain

import com.epita.republic.repository.InfluenceCard
import com.epita.republic.repository.Player

class InfluenceLogic {
    companion object {
        private val influenceCardsTaken: Map<InfluenceStep, MutableSet<InfluenceCard>> = mapOf(
            Pair(InfluenceStep.FIRST, mutableSetOf()),
            Pair(InfluenceStep.SECOND, mutableSetOf()),
            Pair(InfluenceStep.THIRD, mutableSetOf())
        )
    }

    fun remainingCardsCount(step: InfluenceStep): Int {
        return getForStep(step).size - influenceCardsTaken[step]!!.size
    }

    fun getCards(step: InfluenceStep): Array<InfluenceCard> {
        val cardsTaken = influenceCardsTaken[step]!!
        return getForStep(step).filter { card -> !cardsTaken.contains(card) }.toTypedArray()
    }

    fun removeCard(step: InfluenceStep, card: InfluenceCard) {
        influenceCardsTaken[step]!!.add(card)
    }

    private fun getForStep(step: InfluenceStep): Array<InfluenceCard> {
        return InfluenceCard.entries.filter { card -> card.step == step }.toTypedArray()
    }

    fun handleImmediateCards(ownerPlayerIndex: Int, players:  Array<Player>, card: InfluenceCard) {
        when(card) {
            InfluenceCard.CIVILIAN -> {
                val player = players[ownerPlayerIndex]
                player.maxPopularity += 2
                player.popularity += player.maxPopularity
            }
            InfluenceCard.MEDIA -> {
                for ((index, player) in players.withIndex()) {
                    if (index != ownerPlayerIndex) {
                        player.vote -= 4
                    }
                }
            }
            else -> {}
        }
    }
}