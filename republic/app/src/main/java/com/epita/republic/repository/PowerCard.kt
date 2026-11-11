package com.epita.republic.repository

import androidx.annotation.StringRes

/** Same split as King of Tokyo: KEEP stays with the player, DISCARD is used once when bought. */
enum class PowerCardType {
    KEEP,
    DISCARD
}

sealed interface PowerEffect {
    // (DISCARD cards)
    data class Heal(val amount: Int) : PowerEffect
    data class GainMoney(val amount: Int) : PowerEffect
    data class GainVote(val amount: Int) : PowerEffect
    data class DamageOthers(val amount: Int) : PowerEffect

    // (KEEP cards)
    data class BonusDamage(val amount: Int) : PowerEffect
    data class BonusHeal(val amount: Int) : PowerEffect
    data class BonusMoney(val amount: Int) : PowerEffect
    data class ExtraReroll(val amount: Int) : PowerEffect
}

data class PowerCard(
    val id: String,
    @param:StringRes val nameRes: Int,
    val cost: Int = 0,
    val type: PowerCardType,
    val effect: PowerEffect
)
