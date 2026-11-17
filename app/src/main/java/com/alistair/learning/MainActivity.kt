package com.alistair.learning

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    private var toneGenerator: ToneGenerator? = null
    private var backgroundMusicPlayer: AndroidBackgroundMusicPlayer? = null
    private var musicEnabled = false
    private var activityStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 40)
        backgroundMusicPlayer = AndroidBackgroundMusicPlayer()
        val profileStore = AndroidChildProfileStore(applicationContext)
        setContent {
            AlistairApp(
                profileStore = profileStore,
                audioSettingsStore = profileStore,
                learningProgressStore = profileStore,
                onSoundEffect = ::playSoundEffect,
                onBackgroundMusicChanged = ::setBackgroundMusicEnabled,
                showAudioSettings = true
            )
        }
    }

    override fun onStart() {
        super.onStart()
        activityStarted = true
        if (musicEnabled) backgroundMusicPlayer?.setEnabled(true)
    }

    override fun onStop() {
        activityStarted = false
        backgroundMusicPlayer?.setEnabled(false)
        super.onStop()
    }

    override fun onDestroy() {
        backgroundMusicPlayer?.release()
        backgroundMusicPlayer = null
        toneGenerator?.release()
        toneGenerator = null
        super.onDestroy()
    }

    private fun playSoundEffect(effect: SoundEffect) {
        val tone = when (effect) {
            SoundEffect.TAP -> ToneGenerator.TONE_PROP_BEEP
            SoundEffect.TYPE -> ToneGenerator.TONE_PROP_BEEP2
            SoundEffect.SELECT -> ToneGenerator.TONE_PROP_BEEP2
            SoundEffect.CORRECT -> ToneGenerator.TONE_PROP_ACK
            SoundEffect.INCORRECT -> ToneGenerator.TONE_PROP_NACK
        }
        val duration = when (effect) {
            SoundEffect.TYPE -> 45
            SoundEffect.SELECT -> 130
            else -> 90
        }
        toneGenerator?.startTone(tone, duration)
    }

    private fun setBackgroundMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (activityStarted) backgroundMusicPlayer?.setEnabled(enabled)
    }
}
