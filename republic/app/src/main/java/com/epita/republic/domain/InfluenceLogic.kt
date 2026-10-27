package com.epita.republic.domain

import com.epita.republic.R
import com.epita.republic.repository.InfluenceCard

class InfluenceLogic {
    private val influenceCards: Map<InfluenceStep, MutableList<InfluenceCard>> = mapOf(
        Pair(
            InfluenceStep.FIRST, mutableListOf(
                InfluenceCard(
                    R.string.wickedness_step1_1_title,
                    R.string.wickedness_step1_1_description,
                    InfluenceStep.FIRST
                ),
                InfluenceCard(
                    R.string.wickedness_step1_2_title,
                    R.string.wickedness_step1_2_description,
                    InfluenceStep.FIRST
                ),
                InfluenceCard(
                    R.string.wickedness_step1_3_title,
                    R.string.wickedness_step1_3_description,
                    InfluenceStep.FIRST
                ),
                InfluenceCard(
                    R.string.wickedness_step1_4_title,
                    R.string.wickedness_step1_4_description,
                    InfluenceStep.FIRST
                ),
            )
        ),
        Pair(InfluenceStep.SECOND, mutableListOf(
            InfluenceCard(R.string.wickedness_step2_1_title, R.string.wickedness_step2_1_description, InfluenceStep.SECOND),
            InfluenceCard(R.string.wickedness_step2_2_title, R.string.wickedness_step2_2_description, InfluenceStep.SECOND),
            InfluenceCard(R.string.wickedness_step2_3_title, R.string.wickedness_step2_3_description, InfluenceStep.SECOND),
            InfluenceCard(R.string.wickedness_step2_4_title, R.string.wickedness_step2_4_description, InfluenceStep.SECOND),
        )),
        Pair(InfluenceStep.THIRD, mutableListOf(
            InfluenceCard(R.string.wickedness_step3_1_title, R.string.wickedness_step3_1_description, InfluenceStep.THIRD),
            InfluenceCard(R.string.wickedness_step3_2_title, R.string.wickedness_step3_2_description, InfluenceStep.THIRD),
        ))
    )
}