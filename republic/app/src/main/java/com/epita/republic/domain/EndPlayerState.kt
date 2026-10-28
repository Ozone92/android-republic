package com.epita.republic.domain

import androidx.annotation.StringRes
import com.epita.republic.R

enum class EndPlayerState(@param:StringRes val text: Int) {
    WIN_VOTE(R.string.ending_score_win),
    WIN_LAST_ALIVE(R.string.ending_last_alive_win),
    LOST_UNPOPULAR(R.string.ending_lost_dead),
    LOST_NO_VOTE(R.string.ending_lost_alive)
}