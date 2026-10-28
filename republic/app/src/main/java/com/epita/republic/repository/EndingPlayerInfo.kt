package com.epita.republic.repository

import com.epita.republic.domain.Character
import com.epita.republic.domain.EndPlayerState
import com.epita.republic.views.ending.EndingFragmentParameter

data class EndingPlayerInfo(
    val playerIndex: Int,
    val isBot: Boolean,
    val character: Character,
    val endPlayerState: EndPlayerState
) {
    constructor(
        parameter: EndingFragmentParameter,
        endPlayerState: EndPlayerState
    ) : this(parameter.playerIndex, parameter.isBot, parameter.character, endPlayerState)
}
