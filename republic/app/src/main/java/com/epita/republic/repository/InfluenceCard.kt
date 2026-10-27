package com.epita.republic.repository

import androidx.annotation.StringRes
import com.epita.republic.R
import com.epita.republic.domain.InfluenceStep

enum class InfluenceCard(
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    val step: InfluenceStep
) {
    RAISE(
        R.string.wickedness_step1_1_title,
        R.string.wickedness_step1_1_description,
        InfluenceStep.FIRST
    ),
    POPULAR(
        R.string.wickedness_step1_2_title,
        R.string.wickedness_step1_2_description,
        InfluenceStep.FIRST
    ),
    FRIEND(
        R.string.wickedness_step1_3_title,
        R.string.wickedness_step1_3_description,
        InfluenceStep.FIRST
    ),
    UNDECIDED(
        R.string.wickedness_step1_4_title,
        R.string.wickedness_step1_4_description,
        InfluenceStep.FIRST
    ),

    CIVILIAN(R.string.wickedness_step2_1_title, R.string.wickedness_step2_1_description, InfluenceStep.SECOND),
    MEDIA(R.string.wickedness_step2_2_title, R.string.wickedness_step2_2_description, InfluenceStep.SECOND),
    LOBBY(R.string.wickedness_step2_3_title, R.string.wickedness_step2_3_description, InfluenceStep.SECOND),
    EFFICIENT(R.string.wickedness_step2_4_title, R.string.wickedness_step2_4_description, InfluenceStep.SECOND),

    PRIMARY(R.string.wickedness_step3_1_title, R.string.wickedness_step3_1_description, InfluenceStep.THIRD),
    FAR(R.string.wickedness_step3_2_title, R.string.wickedness_step3_2_description, InfluenceStep.THIRD),
}
