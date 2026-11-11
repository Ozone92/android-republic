package com.epita.republic.domain

import com.epita.republic.R

enum class Character(val defaultImageRes: Int, val centerImageRes: Int, val pickVoiceRes: Int? = null, val winVoiceRes: Int? = null, val loseVoiceRes: Int? = null) {
    MELANCHON( R.drawable.melanchon_1_cutout, R.drawable.melanchon_2_cutout, R.raw.melanchon_pick, R.raw.melanchon_win, R.raw.melanchon_lose),
    OBONO( R.drawable.obono_1_cutout, R.drawable.obono_2_cutout, R.raw.obono_pick, R.raw.obono_win, R.raw.obono_lose),
    MACRON(R.drawable.macron_1_cutout, R.drawable.macron_2_cutout, R.raw.macron_pick, R.raw.macron_win, R.raw.macron_lose),
    PECRESSE(R.drawable.pecresse_1_cutout, R.drawable.pecresse_2_cutout, R.raw.pecresse_pick, R.raw.pecresse_win, R.raw.pecresse_lose),
    HOLLANDE(R.drawable.hollande_1_cutout, R.drawable.hollande_2_cutout, R.raw.hollande_pick, R.raw.hollande_win, R.raw.hollande_lose),
    LEPEN(R.drawable.lepen_1_cutout, R.drawable.lepen_2_cutout),
    DURIF(R.drawable.durif_1_cutout, R.drawable.durif_2_cutout, R.raw.durif_pick, R.raw.durif_win, R.raw.durif_lose),
    LASSALLE(R.drawable.lassalle_1_cutout, R.drawable.lassalle_2_cutout, R.raw.lassalle_pick, R.raw.lassalle_win, R.raw.lassalle_lose),
    ROYAL(R.drawable.royal_1_cutout, R.drawable.royal_2_cutout, R.raw.royal_pick, R.raw.royal_win, R.raw.royal_lose)
}