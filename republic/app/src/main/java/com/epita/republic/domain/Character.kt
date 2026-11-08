package com.epita.republic.domain

import com.epita.republic.R

enum class Character(val defaultImageRes: Int, val centerImageRes: Int) {
    MELANCHON( R.drawable.melanchon_1_cutout, R.drawable.melanchon_2_cutout),
    OBONO( R.drawable.obono_1_cutout, R.drawable.obono_2_cutout),
    MACRON(R.drawable.macron_1_cutout, R.drawable.macron_2_cutout),
    PECRESSE(R.drawable.pecresse_1_cutout, R.drawable.pecresse_2_cutout),
    HOLLANDE(R.drawable.hollande_1_cutout, R.drawable.hollande_2_cutout),
    LEPEN(R.drawable.lepen_1_cutout, R.drawable.lepen_2_cutout),
    DURIF(R.drawable.durif_1_cutout, R.drawable.durif_2_cutout),
    LASSALLE(R.drawable.lassalle_1_cutout, R.drawable.lassalle_2_cutout),
    ROYAL(R.drawable.royal_1_cutout, R.drawable.royal_2_cutout)
}