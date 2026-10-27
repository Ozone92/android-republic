package com.epita.republic.repository

import androidx.annotation.StringRes
import com.epita.republic.domain.InfluenceStep

data class InfluenceCard(
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    val step: InfluenceStep
)
