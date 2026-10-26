package com.epita.republic.domain

import com.epita.republic.repository.Player
import kotlin.random.Random

fun bots(count: Int = 4): List<Player> {
    return List(count) { Player(isBot = true) }
}

// Shop qui ne contient que les cartes demandées
fun engineWith(vararg cards: Card, players: List<Player> = bots(), seed: Int = 1): GameEngine {
    return GameEngine(players, Random(seed), Shop(Random(seed), cards.toList()))
}

fun GameEngine.setDice(vararg faces: DiceFace) {
    require(faces.size == DICE_COUNT)
    faces.copyInto(dice)
}

// Le joueur courant achète `card` avec juste ce qu'il faut d'argent
fun GameEngine.buyWithExactMoney(card: Card, target: Player? = null): Boolean {
    current.money = card.cost
    return buy(shop.visible.indexOf(card), target)
}
