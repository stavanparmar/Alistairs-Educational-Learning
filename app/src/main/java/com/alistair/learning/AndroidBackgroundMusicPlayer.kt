package com.alistair.learning

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

class AndroidBackgroundMusicPlayer {
    private var audioTrack: AudioTrack? = null

    fun setEnabled(enabled: Boolean) {
        if (enabled) start() else stop()
    }

    fun release() = stop()

    private fun start() {
        if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) return
        stop()

        val samples = createMusicLoop()
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(samples.size * 2)
            .build()

        val written = track.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
        if (written != samples.size) {
            Log.e(TAG, "Background music buffer was not fully written: $written/${samples.size}")
            track.release()
            return
        }
        val loopResult = track.setLoopPoints(0, samples.size, -1)
        if (loopResult != AudioTrack.SUCCESS) {
            Log.e(TAG, "Unable to loop background music: $loopResult")
            track.release()
            return
        }

        track.setVolume(MUSIC_VOLUME)
        track.play()
        audioTrack = track
    }

    private fun stop() {
        audioTrack?.let { track ->
            track.pause()
            track.flush()
            track.release()
        }
        audioTrack = null
    }

    private fun createMusicLoop(): ShortArray {
        val noteFrames = SAMPLE_RATE * NOTE_DURATION_MILLIS / 1000
        val notes = intArrayOf(
            523, 659, 784, 659,
            587, 698, 880, 698,
            523, 659, 784, 988,
            880, 784, 659, 587
        )
        val bassNotes = intArrayOf(131, 147, 131, 165)
        return ShortArray(noteFrames * notes.size).also { samples ->
            notes.forEachIndexed { noteIndex, frequency ->
                val start = noteIndex * noteFrames
                val bassFrequency = bassNotes[noteIndex / 4]
                for (frame in 0 until noteFrames) {
                    val attack = min(1.0, frame / 500.0)
                    val release = min(1.0, (noteFrames - frame) / 900.0)
                    val envelope = min(attack, release)
                    val time = frame.toDouble() / SAMPLE_RATE
                    val lead = sin(2.0 * PI * frequency * time)
                    val bass = sin(2.0 * PI * bassFrequency * time)
                    val overtone = sin(4.0 * PI * frequency * time)
                    val value = (lead * 0.72 + bass * 0.22 + overtone * 0.06) *
                        MAX_AMPLITUDE * envelope
                    samples[start + frame] = value.toInt().toShort()
                }
            }
        }
    }

    private companion object {
        const val TAG = "BackgroundMusic"
        const val SAMPLE_RATE = 22050
        const val NOTE_DURATION_MILLIS = 360
        const val MAX_AMPLITUDE = 9000.0
        const val MUSIC_VOLUME = 0.45f
    }
}
