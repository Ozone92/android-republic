# Game interaction sounds (#43)

Place the clips in `republic/app/src/main/res/raw/`:

| File | Trigger |
| --- | --- |
| `dice_roll.mp3` | An actual dice roll, including automatic and bot rolls. No sound if all dice are locked or no rerolls remain. |
| `damage.mp3` | Damage to at least one living opponent. One sound for a group attack. |
| `win.mp3` | Human victory, played at 60% together with `win_clap`. |
| `win_clap.mp3` | Victory applause, played at 40% together with `win`. |
| `lose.mp3` | The game ends without a human victory (including no winner). |
| `buy_card.mp3` | A successful power-card purchase, including free placeholder cards. |

No sound plays for rejected purchases or shop rerolls. Missing files are silently ignored and do not prevent compilation. Use short clips with no silence at the beginning; verify redistribution rights before committing them.

`GameViewModel` emits one-shot events consumed by `GameFragment`; the ending screen selects its result sound. `GameSoundPlayer` plays media using game audio attributes, releases completed clips, and stops playback when the screen leaves the foreground. Returning to the same ending screen or rotating it does not replay the result sound.

After adding files, rebuild and install the APK. Adjust the emulator/device **media** volume to hear them. Without supplied clips, only the wiring can be validated.

Victory layers are prepared before either starts. Their gains are configured in `GameSound.kt`, leaving room for future character voice lines. Character voice clips are not yet provided or connected. Final listening balance must be checked with the actual voice clips.
