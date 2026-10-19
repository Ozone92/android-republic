package com.epita.republic.repository

data class Player(
    // Health
    var popularity: Int = 10,
    var maxPopularity: Int = 10,

    // Evilness
    var influence: Int = 0,

    // Energy
    var money: Int = 0,

    // Score/Power
    var vote: Int = 0,

    var isBot: Boolean
) {
    fun isAlive(): Boolean {
        return popularity > 0
    }

    fun hasWin(): Boolean {
        return vote >= 20
    }

    fun isBotGoOut(): Boolean {
        return popularity < maxPopularity / 2
    }
}
