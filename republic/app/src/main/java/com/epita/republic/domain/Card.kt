package com.epita.republic.domain

import androidx.annotation.StringRes
import com.epita.republic.R

enum class CardType {
    IMMEDIATE,
    PERMANENT
}

// Cartes Mesure de l'inventaire (docs/inventaire.pdf, section E)
enum class Card(
    val number: Int,
    val cost: Int,
    val type: CardType,
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    val needsTarget: Boolean = false
) {
    PARTY_POLL(1, 3, CardType.IMMEDIATE, R.string.card_party_poll_title, R.string.card_party_poll_desc),
    MINISTER_POACHING(2, 6, CardType.IMMEDIATE, R.string.card_minister_poaching_title, R.string.card_minister_poaching_desc, needsTarget = true),
    FARM_FAIR_SELFIE(3, 4, CardType.IMMEDIATE, R.string.card_farm_fair_selfie_title, R.string.card_farm_fair_selfie_desc),
    FUNDRAISING_DINNER(4, 4, CardType.IMMEDIATE, R.string.card_fundraising_dinner_title, R.string.card_fundraising_dinner_desc),
    LIVE_CLASH(5, 5, CardType.IMMEDIATE, R.string.card_live_clash_title, R.string.card_live_clash_desc),
    PALACE_REVOLUTION(6, 6, CardType.IMMEDIATE, R.string.card_palace_revolution_title, R.string.card_palace_revolution_desc),
    PRESS_LEAK(7, 4, CardType.IMMEDIATE, R.string.card_press_leak_title, R.string.card_press_leak_desc, needsTarget = true),
    DISSOLUTION(8, 5, CardType.IMMEDIATE, R.string.card_dissolution_title, R.string.card_dissolution_desc),
    NIGHT_TWEET(9, 2, CardType.IMMEDIATE, R.string.card_night_tweet_title, R.string.card_night_tweet_desc, needsTarget = true),
    SUNNY_SUNDAY(10, 6, CardType.IMMEDIATE, R.string.card_sunny_sunday_title, R.string.card_sunny_sunday_desc),
    THEODULE_COMMITTEE(11, 3, CardType.IMMEDIATE, R.string.card_theodule_committee_title, R.string.card_theodule_committee_desc),
    HOLOGRAM_RALLY(12, 6, CardType.IMMEDIATE, R.string.card_hologram_rally_title, R.string.card_hologram_rally_desc),
    HENCHMAN(13, 4, CardType.PERMANENT, R.string.card_henchman_title, R.string.card_henchman_desc),
    TALKING_POINTS(14, 3, CardType.PERMANENT, R.string.card_talking_points_title, R.string.card_talking_points_desc),
    LOCAL_NETWORK(15, 4, CardType.PERMANENT, R.string.card_local_network_title, R.string.card_local_network_desc),
    BODYGUARD(16, 4, CardType.PERMANENT, R.string.card_bodyguard_title, R.string.card_bodyguard_desc),
    COMEBACK(17, 5, CardType.PERMANENT, R.string.card_comeback_title, R.string.card_comeback_desc),
    GOLDEN_SINECURE(18, 4, CardType.PERMANENT, R.string.card_golden_sinecure_title, R.string.card_golden_sinecure_desc),
    CLIENTELISM(19, 4, CardType.PERMANENT, R.string.card_clientelism_title, R.string.card_clientelism_desc),
    BUZZ(20, 4, CardType.PERMANENT, R.string.card_buzz_title, R.string.card_buzz_desc),
    SOLID_BASE(21, 4, CardType.PERMANENT, R.string.card_solid_base_title, R.string.card_solid_base_desc),
    STREISAND_EFFECT(22, 5, CardType.PERMANENT, R.string.card_streisand_effect_title, R.string.card_streisand_effect_desc),
    WELL_OILED_MACHINE(23, 7, CardType.PERMANENT, R.string.card_well_oiled_machine_title, R.string.card_well_oiled_machine_desc),
    SPIN_DOCTOR(24, 8, CardType.PERMANENT, R.string.card_spin_doctor_title, R.string.card_spin_doctor_desc)
}
