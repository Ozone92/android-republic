package com.epita.republic.views.character

import androidx.annotation.DrawableRes

data class CharacterItem(
    val id: String,
    val name: String,
    @param:DrawableRes val imageRes: Int
)