package com.techeliteclub.game.core

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** What a game's home screen shows. [progress] null = no progress bar (endless games). */
data class HomeInfo(val pill: String, val pillIsTrophy: Boolean, val label: String, val progress: Float?)

/** Navigation the games can trigger without knowing about the activity. */
class GameNav(
    val home: () -> Unit,
    val settings: () -> Unit,
    val daily: (() -> Unit)? = null, // null: game has no daily puzzle / level map
    val map: (() -> Unit)? = null,
    val themes: () -> Unit = {},
    val more: () -> Unit = {},
)

val LocalNav = staticCompositionLocalOf { GameNav({}, {}) }

/**
 * Each game's MainActivity extends this. The activity shows the game's home screen, the optional
 * how-to screen (once, on first play) and the game, and owns sound, music and settings.
 */
abstract class GameActivity : ComponentActivity() {
    abstract val gameTheme: GameTheme
    abstract val logoLines: List<String>
    abstract val tagline: String
    open val hasHowTo: Boolean = false

    /** Numbered levels: turns on the daily puzzle (which drives the streak) and the level map. */
    open val hasLevels: Boolean = false

    @Composable
    abstract fun Hero()

    abstract fun homeInfo(store: GameStore): HomeInfo

    @Composable
    open fun HowTo(onDone: () -> Unit) {
    }

    @Composable
    abstract fun Game(ads: Ads)

    private val askNotifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    private lateinit var sounds: Sounds
    private lateinit var haptics: Haptics
    private var music: Music? = null
    private lateinit var settings: GameSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val store = GameStore(this)
        settings = GameSettings(store)
        // Ask for reminder notifications on the second launch (not the first), once.
        val launches = store.get("launches") + 1
        store.put("launches", launches)
        if (Build.VERSION.SDK_INT >= 33 && launches >= 2 && !store.getBool("asked_notifications", false)) {
            store.putBool("asked_notifications", true)
            askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        Reminder.schedule(this)
        sounds = Sounds(this)
        haptics = Haptics(this)
        music = Music(this)
        val ads = Ads(this).also { it.start() }
        setContent {
            var skin by remember { mutableStateOf(Skins.selected(store)) }
            val theme = gameTheme.withSkin(skin)
            val scheme = if (theme.light) lightColorScheme(primary = theme.primary[1]) else darkColorScheme(primary = theme.primary[1])
            CompositionLocalProvider(
                LocalGameTheme provides theme,
                LocalSkin provides skin,
                LocalDisplayFont provides rememberDisplayFont(),
                LocalSounds provides sounds,
                LocalHaptics provides haptics,
                LocalSettings provides settings,
            ) {
                MaterialTheme(colorScheme = scheme) {
                    var screen by rememberSaveable { mutableStateOf("home") }
                    var showSettings by remember { mutableStateOf(false) }
                    var streakPopup by remember { mutableStateOf<Int?>(null) }
                    val nav = remember {
                        GameNav(
                            home = { screen = "home" },
                            settings = { showSettings = true },
                            daily = if (hasLevels) ({ screen = "hub" }) else null,
                            map = if (hasLevels) ({ screen = "map" }) else null,
                            themes = { screen = "themes" },
                            more = { screen = "more" },
                        )
                    }
                    LaunchedEffect(screen) {
                        if (screen == "play" || screen == "daily") Reminder.markPlayed(store)
                        // Games without a daily puzzle keep the streak by playing at all.
                        if (screen == "play" && !hasLevels) streakPopup = Streak.onPlayed(store)
                    }
                    LaunchedEffect(settings.music) { if (settings.music) music?.start() else music?.pause() }
                    BackHandler(enabled = screen != "home" || showSettings) {
                        when {
                            showSettings -> showSettings = false
                            screen == "daily" -> screen = "hub"
                            else -> screen = "home"
                        }
                    }
                    val play = { screen = if (hasHowTo && !store.getBool("howto_seen", false)) "howto" else "play" }
                    CompositionLocalProvider(LocalNav provides nav) {
                        Box(Modifier.fillMaxSize()) {
                            when (screen) {
                                "home" -> HomeScreen(
                                    logoLines, tagline, homeInfo(store), hero = { Hero() },
                                    onPlay = play,
                                    onHowTo = if (hasHowTo) ({ screen = "howto" }) else null,
                                )
                                "howto" -> HowTo { store.putBool("howto_seen", true); screen = "play" }
                                "hub" -> DailyScreen { screen = "daily" }
                                "daily" -> {
                                    val mode = remember {
                                        PlayMode(daily = true, seed = Daily.seed()) {
                                            Daily.markDone(store)
                                            streakPopup = Streak.onPlayed(store)
                                            screen = "hub"
                                        }
                                    }
                                    CompositionLocalProvider(LocalPlayMode provides mode) { Game(ads) }
                                }
                                "map" -> LevelMapScreen(onPlay = play)
                                "themes" -> ThemesScreen(ads) { skin = it }
                                "more" -> MoreGamesScreen()
                                else -> Game(ads)
                            }
                            streakPopup?.let { StreakPopup(it) { streakPopup = null } }
                            if (showSettings) SettingsSheet(ads, settings) { showSettings = false }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (settings.music) music?.start()
    }

    override fun onPause() {
        super.onPause()
        music?.pause()
    }

    override fun onStop() {
        super.onStop()
        Cloud.backup(this, GameStore(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        sounds.release()
        music?.release()
    }
}

data class Tool(val label: String, val icon: ImageVector, val enabled: Boolean = true, val onClick: () -> Unit)

/**
 * The rewarded hint button. The first use per [levelKey] is free; after that it plays a rewarded ad.
 * Change [levelKey] when a new level (or new game) starts.
 */
data class Hint(val label: String, val icon: ImageVector, val levelKey: Any, val enabled: Boolean = true, val onHint: () -> Unit)

data class Pill(val text: String, val icon: ImageVector? = null, val gold: Boolean = false)

/** Play screen: top bar (home, logo, settings), status pills, the game, tools + hint, banner ad. */
@Composable
fun GameScaffold(
    ads: Ads,
    title: String,
    pills: List<Pill>,
    tools: List<Tool>,
    hint: Hint?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val t = LocalGameTheme.current
    val nav = LocalNav.current
    val feedback = rememberFeedback()
    val store = rememberStore()
    GameBackground {
        Column(Modifier.fillMaxSize().statusBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                GlassIconButton(GameIcons.Back, "Home") { feedback.tap(); nav.home() }
                GradientText(title, t.logo1, 28.sp)
                GlassIconButton(GameIcons.Gear, "Settings") { feedback.tap(); nav.settings() }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (p in pills) GlassPill {
                    if (p.icon != null) {
                        if (p.gold) GoldIcon(p.icon, 18.dp)
                        else Icon(p.icon, null, tint = t.glassInk, modifier = Modifier.size(18.dp))
                    }
                    PillText(p.text)
                }
            }
            Column(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                content = content,
            )
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (tool in tools) {
                    ToolButton(tool.label, tool.icon, Modifier.weight(1f), tool.enabled) { feedback.tap(); tool.onClick() }
                }
                if (hint != null) {
                    // Saved, so leaving and re-entering the level does not give another free hint.
                    val usedKey = "free_hint_used_" + hint.levelKey
                    var freeUsed by remember(hint.levelKey) { mutableStateOf(store.getBool(usedKey, false)) }
                    var bonus by remember { mutableIntStateOf(store.get("bonus_hints")) } // earned from streaks, any level
                    ToolButton(
                        hint.label, hint.icon, Modifier.weight(1f),
                        enabled = hint.enabled && (!freeUsed || bonus > 0 || ads.rewardedReady),
                        gold = true,
                        badge = when { !freeUsed -> "1 FREE"; bonus > 0 -> "+$bonus FREE"; else -> "▶ AD" },
                    ) {
                        when {
                            !freeUsed -> { freeUsed = true; store.putBool(usedKey, true); hint.onHint() }
                            bonus > 0 -> { bonus--; store.put("bonus_hints", bonus); hint.onHint() }
                            else -> ads.showRewarded(hint.onHint)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().navigationBarsPadding()) { ads.Banner() }
        }
    }
}

/** Sound + vibration for game events, following the player's settings. */
class Feedback(
    private val view: View,
    private val sounds: Sounds?,
    private val settings: GameSettings?,
    private val haptics: Haptics?,
) {
    private fun sfx(s: Sfx) {
        if (settings?.sound != false) {
            if (sounds != null) sounds.play(s) else view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
        }
    }

    private inline fun buzz(pattern: Haptics.() -> Unit) {
        if (settings?.vibration != false) haptics?.pattern()
    }

    fun tap() { sfx(Sfx.TAP); buzz { tick() } }
    fun good() { sfx(Sfx.PLACE); buzz { place() } }
    fun clear() { sfx(Sfx.CLEAR); buzz { clear() } }
    fun bad() { sfx(Sfx.ERROR); buzz { error() } }
    fun star() { sfx(Sfx.STAR); buzz { star() } }
    fun win() { sfx(Sfx.WIN); buzz { win() } }
}

@Composable
fun rememberFeedback(): Feedback {
    val view = LocalView.current
    val sounds = LocalSounds.current
    val settings = LocalSettings.current
    val haptics = LocalHaptics.current
    return remember(view, sounds, settings, haptics) { Feedback(view, sounds, settings, haptics) }
}

/** Distinct colours shared by the games (tiles, balls, regions). */
val PALETTE = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFDD835), Color(0xFF8E24AA),
    Color(0xFFFB8C00), Color(0xFF00ACC1), Color(0xFFD81B60), Color(0xFF6D4C41), Color(0xFF7CB342),
)

/** Gem colour pairs (light top, deep base) used for pieces across the games. */
val GEMS = listOf(
    Color(0xFFFF8A8A) to Color(0xFFD81E3A), Color(0xFF9CC4FF) to Color(0xFF2257E0), Color(0xFF8FF0A0) to Color(0xFF1FA64A),
    Color(0xFFFFF08A) to Color(0xFFF0B400), Color(0xFFD9A8FF) to Color(0xFF8A2AD8), Color(0xFFFFC27A) to Color(0xFFEF7A00),
    Color(0xFF8BF0FF) to Color(0xFF0AA3C2), Color(0xFFFFB0D8) to Color(0xFFD8247A), Color(0xFFD7B59A) to Color(0xFF8A5A3A),
    Color(0xFFCFF08A) to Color(0xFF6FA81E),
)

/** Saved numbers and switches that survive app restarts. */
class GameStore(context: Context) {
    private val prefs = context.getSharedPreferences("game", Context.MODE_PRIVATE)
    fun get(key: String, default: Int = 0) = prefs.getInt(key, default)
    fun put(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    fun getBool(key: String, default: Boolean) = prefs.getBoolean(key, default)
    fun putBool(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    fun getString(key: String): String? = prefs.getString(key, null)
    fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()
    fun getLong(key: String, default: Long = 0L) = prefs.getLong(key, default)
    fun putLong(key: String, value: Long) = prefs.edit().putLong(key, value).apply()
    fun all(): Map<String, *> = prefs.all
    fun putAll(values: Map<String, Any>) = prefs.edit().apply {
        for ((k, v) in values) when (v) {
            is Int -> putInt(k, v)
            is Long -> putLong(k, v)
            is Boolean -> putBoolean(k, v)
            is String -> putString(k, v)
        }
    }.apply()
}

@Composable
fun rememberStore(): GameStore {
    val context = LocalContext.current
    return remember { GameStore(context) }
}
