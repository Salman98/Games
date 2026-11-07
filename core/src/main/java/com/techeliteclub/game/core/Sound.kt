package com.techeliteclub.game.core

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/** Each sound with its loudness, so frequent taps stay quiet and the win fanfare stands out. */
enum class Sfx(val volume: Float) { TAP(0.45f), PLACE(0.7f), CLEAR(0.9f), WIN(1f), ERROR(0.6f), STAR(0.85f) }

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
        Sfx.STAR to pool.load(context, R.raw.sfx_star, 1),
    )

    fun play(s: Sfx, rate: Float = 1f) {
        pool.play(ids.getValue(s), s.volume, s.volume, 1, 0, rate)
    }

    fun release() = pool.release()
}

/** Looping background music (res/raw/music_loop.ogg). */
class Music(context: Context) {
    private val player: MediaPlayer? = MediaPlayer.create(context, R.raw.music_loop)?.apply {
        isLooping = true
        setVolume(0.3f, 0.3f)
    }

    fun start() { player?.let { if (!it.isPlaying) it.start() } }
    fun pause() { player?.let { if (it.isPlaying) it.pause() } }
    fun release() = player?.release()
}

/**
 * Vibration through the phone's vibrator. Compose's built-in haptics follow the system "touch vibration"
 * switch, which is off on many phones (e.g. ColorOS), so they often do nothing.
 */
class Haptics(context: Context) {
    private val vibrator: Vibrator? = findVibrator(context)

    @Suppress("DEPRECATION")
    private fun findVibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= 31) (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        else context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    /** [timings] in ms (off, on, off, on…); [amps] 0–255 for each slot. */
    @Suppress("DEPRECATION")
    private fun wave(timings: LongArray, amps: IntArray) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(if (v.hasAmplitudeControl()) VibrationEffect.createWaveform(timings, amps, -1) else VibrationEffect.createWaveform(timings, -1))
        } else {
            v.vibrate(timings, -1)
        }
    }

    fun tick() = wave(longArrayOf(0, 12), intArrayOf(0, 60))
    fun place() = wave(longArrayOf(0, 22), intArrayOf(0, 130))
    fun clear() = wave(longArrayOf(0, 30, 45, 50), intArrayOf(0, 170, 0, 255))
    fun error() = wave(longArrayOf(0, 45, 70, 45), intArrayOf(0, 200, 0, 200))
    fun star() = wave(longArrayOf(0, 35), intArrayOf(0, 200))
    fun win() = wave(longArrayOf(0, 60, 80, 60, 80, 160), intArrayOf(0, 150, 0, 200, 0, 255))
}

/** Sound / music / vibration switches, saved between sessions. */
class GameSettings(private val store: GameStore) {
    var sound by mutableStateOf(store.getBool("sound", true)); private set
    var music by mutableStateOf(store.getBool("music", true)); private set
    var vibration by mutableStateOf(store.getBool("vibration", true)); private set

    // Not named setSound: that would clash with the property's own setter.
    fun switchSound(on: Boolean) { sound = on; store.putBool("sound", on) }
    fun switchMusic(on: Boolean) { music = on; store.putBool("music", on) }
    fun switchVibration(on: Boolean) { vibration = on; store.putBool("vibration", on) }
}

val LocalSounds = staticCompositionLocalOf<Sounds?> { null }
val LocalSettings = staticCompositionLocalOf<GameSettings?> { null }
val LocalHaptics = staticCompositionLocalOf<Haptics?> { null }
