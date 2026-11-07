package com.techeliteclub.game.core

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

enum class Sfx { TAP, PLACE, CLEAR, WIN, ERROR }

/** Short sound effects (res/raw/sfx_*.wav). */
class Sounds(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val ids = mapOf(
        Sfx.TAP to pool.load(context, R.raw.sfx_tap, 1),
        Sfx.PLACE to pool.load(context, R.raw.sfx_place, 1),
        Sfx.CLEAR to pool.load(context, R.raw.sfx_clear, 1),
        Sfx.WIN to pool.load(context, R.raw.sfx_win, 1),
        Sfx.ERROR to pool.load(context, R.raw.sfx_error, 1),
    )

    fun play(s: Sfx, rate: Float = 1f) {
        pool.play(ids.getValue(s), 0.8f, 0.8f, 1, 0, rate)
    }

    fun release() = pool.release()
}

/** Looping background music (res/raw/music_loop.ogg). */
class Music(context: Context) {
    private val player: MediaPlayer? = MediaPlayer.create(context, R.raw.music_loop)?.apply {
        isLooping = true
        setVolume(0.35f, 0.35f)
    }

    fun start() { player?.let { if (!it.isPlaying) it.start() } }
    fun pause() { player?.let { if (it.isPlaying) it.pause() } }
    fun release() = player?.release()
}

/** Sound / music / vibration switches, saved between sessions. */
class GameSettings(private val store: GameStore) {
    var sound by mutableStateOf(store.getBool("sound", true)); private set
    var music by mutableStateOf(store.getBool("music", true)); private set
    var vibration by mutableStateOf(store.getBool("vibration", true)); private set

    fun setSound(on: Boolean) { sound = on; store.putBool("sound", on) }
    fun setMusic(on: Boolean) { music = on; store.putBool("music", on) }
    fun setVibration(on: Boolean) { vibration = on; store.putBool("vibration", on) }
}

val LocalSounds = staticCompositionLocalOf<Sounds?> { null }
val LocalSettings = staticCompositionLocalOf<GameSettings?> { null }
