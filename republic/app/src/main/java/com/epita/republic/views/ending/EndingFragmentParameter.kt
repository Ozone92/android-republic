package com.epita.republic.views.ending

import com.epita.republic.domain.Character

data class EndingFragmentParameter(
    val winningPlayerId: Int?,
    val winningPlayerImgRes: Int?,

    // Pair of PlayerId, Character
    val playerWinningOrder: Array<Pair<Int, Character>>,
): java.io.Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EndingFragmentParameter

        if (winningPlayerId != other.winningPlayerId) return false
        if (winningPlayerImgRes != other.winningPlayerImgRes) return false
        if (!playerWinningOrder.contentEquals(other.playerWinningOrder)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0;

        if (winningPlayerId != null) {
            result = winningPlayerId
        }
        if (winningPlayerImgRes != null) {
            result = 31 * result + winningPlayerImgRes
        }

        result = 31 * result + playerWinningOrder.contentHashCode()
        return result
    }
}
