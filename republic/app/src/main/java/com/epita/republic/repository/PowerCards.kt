package com.epita.republic.repository

import com.epita.republic.R
import com.epita.republic.repository.PowerEffect.*

/** Every power card */
val POWER_CARDS: List<PowerCard> = listOf(
    // ACTION cards
    PowerCard("soins_urgence", R.string.power_name_soins_urgence, cost = 3, effect = Heal(2)),
    PowerCard("don_campagne", R.string.power_name_don_campagne, cost = 2, effect = GainMoney(3)),
    PowerCard("meeting", R.string.power_name_meeting, cost = 4, effect = GainVote(2)),
    PowerCard("congres", R.string.power_name_congres, cost = 5, effect = GainVote(3)),
    PowerCard("scandale", R.string.power_name_scandale, cost = 2, effect = DamageOthers(1)),
    PowerCard("affaire_etat", R.string.power_name_affaire_etat, cost = 3, effect = DamageOthers(2)),
    PowerCard("debat_televise", R.string.power_name_debat_televise, cost = 3, effect = VotesForPopularity(2, 2)),
    PowerCard("reforme_risquee", R.string.power_name_reforme_risquee, cost = 4, effect = VotesForPopularity(4, 3)),
    PowerCard("fuite_documents", R.string.power_name_fuite_documents, cost = 5, effect = OthersLoseVotes(3)),
    PowerCard("greve_generale", R.string.power_name_greve_generale, cost = 4, effect = DamageEveryone(3)),
    PowerCard("coups_bas", R.string.power_name_coups_bas, cost = 6, effect = VotesAndDamageOthers(2, 3)),
    PowerCard("coup_de_force", R.string.power_name_coup_de_force, cost = 5, effect = TakeCity(2)),
    PowerCard("dissolution", R.string.power_name_dissolution, cost = 7, effect = ExtraTurn()),

    // POWER cards
    PowerCard("denigrement", R.string.power_name_denigrement, cost = 6, effect = BonusDamage(1)),
    PowerCard("second_souffle", R.string.power_name_second_souffle, cost = 5, effect = ExtraReroll(1)),
    PowerCard("mecene", R.string.power_name_mecene, cost = 4, effect = BonusMoney(1)),
    PowerCard("image_publique", R.string.power_name_image_publique, cost = 4, effect = BonusHeal(1)),
    PowerCard("peau_dure", R.string.power_name_peau_dure, cost = 4, effect = IgnoreSmallDamage()),
    PowerCard("militants", R.string.power_name_militants, cost = 2, effect = MoneyWhenBroke(1)),
    PowerCard("tresorier", R.string.power_name_tresorier, cost = 3, effect = VotesPerMoney(6)),
    PowerCard("service_presse", R.string.power_name_service_presse, cost = 3, effect = VotesOnBuy(1)),
    PowerCard("austerite", R.string.power_name_austerite, cost = 3, effect = CardDiscount(1)),
    PowerCard("president_sortant", R.string.power_name_president_sortant, cost = 3, effect = CityStartVotes(1)),
    PowerCard("outsider", R.string.power_name_outsider, cost = 3, effect = UnderdogVotes(1)),
    PowerCard("charognard", R.string.power_name_charognard, cost = 4, effect = VotesOnElimination(3)),
    PowerCard("victimisation", R.string.power_name_victimisation, cost = 3, effect = MoneyWhenHurt(1)),
    PowerCard("mains_propres", R.string.power_name_mains_propres, cost = 5, effect = PeacefulVotes(1)),
    PowerCard("populisme", R.string.power_name_populisme, cost = 4, effect = TripleOneBonus(2)),
    PowerCard("rassembleur", R.string.power_name_rassembleur, cost = 3, effect = AllFacesVotes(9)),
    PowerCard("grand_public", R.string.power_name_grand_public, cost = 4, effect = MaxPopularity(2)),
)
