package com.epita.republic.domain

import com.epita.republic.R

enum class Character(val defaultImageRes: Int, val centerImageRes: Int) {
    MELANCHON( R.drawable.melanchon_1, R.drawable.melanchon_2),
    OBONO( R.drawable.obono_1, R.drawable.obono_2),
    MACRON(R.drawable.macron_1, R.drawable.macron_2),
    PECRESSE(R.drawable.pecresse_1, R.drawable.pecresse_2),
    HOLLANDE(R.drawable.hollande_1, R.drawable.hollande_2),
    LEPEN(R.drawable.lepen_1, R.drawable.lepen_2),
    DURIF(R.drawable.durif_1, R.drawable.durif_2),
    LASSALLE(R.drawable.lassalle_1, R.drawable.lassalle_2),
    ROYAL(R.drawable.royal_1, R.drawable.royal_2)
}