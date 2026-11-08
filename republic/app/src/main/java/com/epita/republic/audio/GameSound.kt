package com.epita.republic.audio

/** File names in res/raw, without their extensions. */
enum class GameSound(val resourceName: String, val volume: Float = 1f) {
    DICE_ROLL("dice_roll"),
    DAMAGE("damage"),
    WIN("win", volume = 0.60f),
    WIN_CLAP("win_clap", volume = 0.40f),
    LOSE("lose"),
    BUY_CARD("buy_card")
}
