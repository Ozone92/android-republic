package com.epita.republic.repository

import androidx.annotation.StringRes
import com.epita.republic.R

enum class PowerCardType {
    POWER,
    ACTION
}

/**
 * What a card does. The type and the description come from the effect, and the description is
 * filled with [descriptionArgs], so the text always matches the numbers used by the rules.
 */
sealed class PowerEffect(
    val type: PowerCardType,
    @param:StringRes val descriptionRes: Int,
    val amount: Int = 0
) {
    open val descriptionArgs: List<Int> get() = listOf(amount)

    // One-shot effects (ACTION cards)
    class Heal(amount: Int) :
        PowerEffect(PowerCardType.ACTION, R.string.power_description_heal, amount)

    class GainMoney(amount: Int) :
        PowerEffect(PowerCardType.ACTION, R.string.power_description_gain_money, amount)

    class GainVote(amount: Int) :
        PowerEffect(PowerCardType.ACTION, R.string.power_description_gain_vote, amount)

    class DamageOthers(amount: Int) :
        PowerEffect(PowerCardType.ACTION, R.string.power_description_damage_others, amount)

    class VotesForPopularity(votes: Int, val popularityLoss: Int) :
        PowerEffect(PowerCardType.ACTION, R.string.power_description_votes_for_popularity, votes) {
        override val descriptionArgs get() = listOf(amount, popularityLoss)
    }

    class OthersLoseVotes(amount: Int) :
        PowerEffect(PowerCardType.ACTION, R.string.power_description_others_lose_votes, amount)

    class DamageEveryone(amount: Int) :
        PowerEffect(PowerCardType.ACTION, R.string.power_description_damage_everyone, amount)

    class VotesAndDamageOthers(votes: Int, val damage: Int) :
        PowerEffect(PowerCardType.ACTION, R.string.power_description_votes_and_damage_others, votes) {
        override val descriptionArgs get() = listOf(amount, damage)
    }

    /** Takes the Élysée (if needed) and gains [amount] votes. */
    class TakeCity(votes: Int) :
        PowerEffect(PowerCardType.ACTION, R.string.power_description_take_city, votes)

    /** The buyer plays another turn right after this one. */
    class ExtraTurn :
        PowerEffect(PowerCardType.ACTION, R.string.power_description_extra_turn) {
        override val descriptionArgs get() = emptyList<Int>()
    }

    // Permanent effects (POWER cards)
    class BonusDamage(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_bonus_damage, amount)

    class BonusHeal(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_bonus_heal, amount)

    class BonusMoney(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_bonus_money, amount)

    class ExtraReroll(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_extra_reroll, amount)

    /** A loss of exactly 1 popularity is ignored. */
    class IgnoreSmallDamage :
        PowerEffect(PowerCardType.POWER, R.string.power_description_ignore_small_damage) {
        override val descriptionArgs get() = emptyList<Int>()
    }

    class MoneyWhenBroke(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_money_when_broke, amount)

    /** End of turn: +1 vote for every [amount] money held. */
    class VotesPerMoney(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_votes_per_money, amount)

    class VotesOnBuy(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_votes_on_buy, amount)

    class CardDiscount(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_card_discount, amount)

    class CityStartVotes(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_city_start_votes, amount)

    class UnderdogVotes(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_underdog_votes, amount)

    class VotesOnElimination(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_votes_on_elimination, amount)

    /** +[amount] money when losing 2 or more popularity at once. */
    class MoneyWhenHurt(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_money_when_hurt, amount)

    class PeacefulVotes(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_peaceful_votes, amount)

    class TripleOneBonus(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_triple_one_bonus, amount)

    class AllFacesVotes(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_all_faces_votes, amount)

    /** Raises max popularity by [amount] and heals [amount] when bought. */
    class MaxPopularity(amount: Int) :
        PowerEffect(PowerCardType.POWER, R.string.power_description_max_popularity, amount)
}

data class PowerCard(
    val id: String,
    @param:StringRes val nameRes: Int,
    val cost: Int,
    val effect: PowerEffect
) {
    val type: PowerCardType get() = effect.type
    @get:StringRes val descriptionRes: Int get() = effect.descriptionRes
    val descriptionArgs: List<Int> get() = effect.descriptionArgs
}
