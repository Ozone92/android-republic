package com.epita.republic.views.common

import android.content.Context
import com.epita.republic.R
import com.epita.republic.domain.Character

fun playerName(context: Context, character: Character, isBot: Boolean): String {
    val name = context.getString(character.displayName)
    return if (isBot) name else context.getString(R.string.game_player_you, name)
}
