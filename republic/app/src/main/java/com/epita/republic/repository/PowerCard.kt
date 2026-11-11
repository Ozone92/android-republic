package com.epita.republic.repository

import androidx.annotation.StringRes
import com.epita.republic.R

enum class PowerCardType {
    POWER,
    ACTION
}

sealed class PowerEffect(
    val amount: Int,
    val type: PowerCardType,
    @param:StringRes val nameRes: Int,
    @param:StringRes val descriptionRes: Int
) {
    // One-shot effects (ACTION cards)
    class Heal(amount: Int) : PowerEffect(
        amount, PowerCardType.ACTION, R.string.power_name_heal, R.string.power_description_heal
    )

    class GainMoney(amount: Int) : PowerEffect(
        amount, PowerCardType.ACTION, R.string.power_name_gain_money, R.string.power_description_gain_money
    )

    class GainVote(amount: Int) : PowerEffect(
        amount, PowerCardType.ACTION, R.string.power_name_gain_vote, R.string.power_description_gain_vote
    )

    class DamageOthers(amount: Int) : PowerEffect(
        amount, PowerCardType.ACTION, R.string.power_name_damage_others, R.string.power_description_damage_others
    )

    // Permanent effects (POWER cards)
    class BonusDamage(amount: Int) : PowerEffect(
        amount, PowerCardType.POWER, R.string.power_name_bonus_damage, R.string.power_description_bonus_damage
    )

    class BonusHeal(amount: Int) : PowerEffect(
        amount, PowerCardType.POWER, R.string.power_name_bonus_heal, R.string.power_description_bonus_heal
    )

    class BonusMoney(amount: Int) : PowerEffect(
        amount, PowerCardType.POWER, R.string.power_name_bonus_money, R.string.power_description_bonus_money
    )

    class ExtraReroll(amount: Int) : PowerEffect(
        amount, PowerCardType.POWER, R.string.power_name_extra_reroll, R.string.power_description_extra_reroll
    )
}

data class PowerCard(
    val id: String,
    val cost: Int = 0,
    val effect: PowerEffect
) {
    val type: PowerCardType get() = effect.type
    @get:StringRes val nameRes: Int get() = effect.nameRes
    @get:StringRes val descriptionRes: Int get() = effect.descriptionRes
    val descriptionArg: Int get() = effect.amount
}
