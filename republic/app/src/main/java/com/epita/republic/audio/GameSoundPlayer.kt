package com.epita.republic.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.annotation.RawRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/** Optional audio assets: missing clips stay silent until the MP3 files are supplied. */
class GameSoundPlayer(context: Context, owner: LifecycleOwner) : DefaultLifecycleObserver {
    private val context = context.applicationContext
    private val activePlayers = mutableSetOf<MediaPlayer>()
    private var enabled = true
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    init {
        owner.lifecycle.addObserver(this)
    }

    fun play(sound: GameSound, @RawRes voiceRes: Int? = null) {
        if (!enabled) return
        // Prepare both victory layers before starting either, so loading the second
        // clip does not delay its start relative to the first one.
        val layers = if (sound == GameSound.WIN) listOf(GameSound.WIN, GameSound.WIN_CLAP)
            else listOf(sound)
        val prepared = layers.mapNotNull { prepare(it) }.toMutableList()
        voiceRes?.let { resource ->
            val speechAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            prepareResource(resource, speechAttributes, 1f)?.let { prepared.add(it) }
        }
        prepared.forEach { it.start() }
    }

    private fun prepare(sound: GameSound): MediaPlayer? {
        // Resolve by name so the project also builds before the optional clips arrive.
        val resource = context.resources.getIdentifier(sound.resourceName, "raw", context.packageName)
        if (resource == 0) return null
        return prepareResource(resource, attributes, sound.volume)
    }

    private fun prepareResource(
        @RawRes resource: Int,
        audioAttributes: AudioAttributes,
        volume: Float
    ): MediaPlayer? {
        val player = MediaPlayer.create(context, resource, audioAttributes, 0) ?: return null
        player.setVolume(volume, volume)
        activePlayers.add(player)
        player.setOnCompletionListener { release(it) }
        player.setOnErrorListener { failed, _, _ ->
            release(failed)
            true
        }
        return player
    }

    private fun release(player: MediaPlayer) {
        if (activePlayers.remove(player)) player.release()
    }

    override fun onStart(owner: LifecycleOwner) {
        enabled = true
    }

    override fun onStop(owner: LifecycleOwner) {
        enabled = false
        activePlayers.toList().forEach { release(it) }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        enabled = false
        activePlayers.toList().forEach { release(it) }
    }
}
