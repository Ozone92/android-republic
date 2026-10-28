package com.epita.republic.views.ending

import com.epita.republic.domain.Character

data class EndingFragmentParameter(
    // Pair of PlayerId, Character
    val playerWinningOrder: Array<Pair<Int, Character>>,
): java.io.Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EndingFragmentParameter
        return playerWinningOrder.contentEquals(other.playerWinningOrder)
    }

    override fun hashCode(): Int {
        return playerWinningOrder.contentHashCode()
    }
}
