package com.epita.republic.views.home

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import androidx.annotation.RawRes
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.epita.republic.R

class MainActivity : AppCompatActivity() {
    private var voicePlayer: MediaPlayer? = null

    fun playVoiceLine(@RawRes audioRes: Int) {
        releaseVoiceLine()
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        val player = MediaPlayer.create(this, audioRes, attributes, 0) ?: return
        voicePlayer = player
        player.setOnCompletionListener { releaseVoiceLine() }
        player.setOnErrorListener { _, _, _ ->
            releaseVoiceLine()
            true
        }
        player.start()
    }

    private fun releaseVoiceLine() {
        voicePlayer?.release()
        voicePlayer = null
    }

    override fun onStop() {
        releaseVoiceLine()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}