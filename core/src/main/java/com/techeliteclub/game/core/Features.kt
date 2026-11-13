package com.techeliteclub.game.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** How the game screen was opened: a normal level or today's daily puzzle. */
class PlayMode(val daily: Boolean = false, val seed: Long = 0, val onDailyWin: () -> Unit = {}) {
    fun startLevel(store: GameStore) = if (daily) Daily.LEVEL else store.get("level", 1)
    fun seedFor(level: Int) = if (daily) seed else level.toLong()
    fun label(level: Int) = if (daily) "DAILY" else "LEVEL $level"
    fun hintKey(level: Int): Any = if (daily) "daily_$seed" else level

    /** Win button: daily goes back to the daily hub, a level saves its best stars then runs [next]. */
    fun win(store: GameStore, level: Int, stars: Int, next: () -> Unit) {
        if (daily) onDailyWin() else { Progress.recordWin(store, level, stars); next() }
    }
}

val LocalPlayMode = staticCompositionLocalOf { PlayMode() }

/**
 * The level in progress, so closing the app mid-puzzle does not lose it. One slot for the levels, one for the daily.
 * The text is tagged with its level: a save for another level, or one [load]'s decode throws on, loads as null.
 */
class LevelSave(private val store: GameStore, mode: PlayMode, level: Int) {
    private val key = if (mode.daily) "save_daily" else "save_level"
    private val tag = "${mode.hintKey(level)}\n"

    fun <T> load(decode: (String) -> T): T? =
        store.getString(key)?.takeIf { it.startsWith(tag) }?.let { runCatching { decode(it.removePrefix(tag)) }.getOrNull() }

    fun save(text: String) = store.putString(key, tag + text)
    fun clear() = store.putString(key, "")
}

/** Best stars per level ("lvl_stars_N"); the total "stars" counts each level's best once. */
object Progress {
    fun starsFor(store: GameStore, level: Int) = store.get("lvl_stars_$level")

    fun recordWin(store: GameStore, level: Int, stars: Int) {
        val old = starsFor(store, level)
        if (stars <= old) return
        store.put("lvl_stars_$level", stars)
        store.put("stars", store.get("stars") + stars - old)
    }
}

/** One shared puzzle a day: same seed for every player on the same local date. */
object Daily {
    const val LEVEL = 12 // mid difficulty
    fun seed() = Reminder.today()
    fun doneToday(store: GameStore) = store.getLong("daily_done_day", -1) == Reminder.today()
    fun markDone(store: GameStore) = store.putLong("daily_done_day", Reminder.today())
}

class StreakReward(val day: Int, val title: String, val skin: String? = null)

object Streak {
    val REWARDS = listOf(
        StreakReward(3, "+1 free hint"),
        StreakReward(7, "Neon theme", "neon"),
        StreakReward(14, "Wood theme", "wood"),
        StreakReward(30, "Gold theme", "gold"),
    )

    fun lastDay(store: GameStore) = store.getLong("streak_day", -1)
    fun current(store: GameStore) = StreakMath.shown(lastDay(store), store.get("streak"), Reminder.today())
    fun best(store: GameStore) = store.get("best_streak")

    /** Call when the player plays today. Returns the new streak the first time each day, else null. */
    fun onPlayed(store: GameStore): Int? {
        val today = Reminder.today()
        val last = lastDay(store)
        if (last == today) return null
        val streak = StreakMath.next(last, store.get("streak"), today)
        store.put("streak", streak)
        store.putLong("streak_day", today)
        if (streak > best(store)) store.put("best_streak", streak)
        REWARDS.find { it.day == streak }?.let { r ->
            if (r.skin != null) store.putBool("skin_owned_${r.skin}", true) else store.put("bonus_hints", store.get("bonus_hints") + 1)
        }
        return streak
    }
}

/** Board look. Null fields keep the game's own colours. */
class Skin(
    val id: String,
    val name: String,
    val rim: List<Color>? = null,
    val tray: List<Color>? = null,
    val gems: List<Pair<Color, Color>>? = null,
    val streakDay: Int = 0,
    val byAd: Boolean = false,
)

private fun gems(vararg hex: Long) = hex.toList().chunked(2) { Color(it[0]) to Color(it[1]) }

val SKINS = listOf(
    Skin("classic", "Classic"),
    Skin(
        "ocean", "Ocean",
        rim = listOf(Color(0xFF9FE7FF), Color(0xFF2A8FD0), Color(0xFF0D4C80)),
        tray = listOf(Color(0xFF0C3A63), Color(0xFF07243F)),
        gems = gems(
            0xFF9CF6FF, 0xFF0AA3C2, 0xFF9CC4FF, 0xFF2257E0, 0xFF8FF0D0, 0xFF13A88A, 0xFFB8F0FF, 0xFF3B8FD8, 0xFFA8B8FF, 0xFF4A4AD8,
            0xFF7FE8E0, 0xFF0F8F8A, 0xFFCFEFFF, 0xFF5AA8E8, 0xFF9AD8FF, 0xFF1F6FB8, 0xFFB0FFEA, 0xFF22B890, 0xFF8AB0FF, 0xFF2A5AC8,
        ),
    ),
    Skin(
        "neon", "Neon", streakDay = 7,
        rim = listOf(Color(0xFFFF6BF0), Color(0xFF8A2BE2), Color(0xFF2B0A5A)),
        tray = listOf(Color(0xFF14052E), Color(0xFF07021A)),
        gems = gems(
            0xFFFF9BF5, 0xFFE010C8, 0xFF8BFFFF, 0xFF00C8E0, 0xFFB5FF8A, 0xFF40E010, 0xFFFFF58A, 0xFFF0D000, 0xFFC89BFF, 0xFF7A20F0,
            0xFFFFB08A, 0xFFFF5A1E, 0xFF8AFFD0, 0xFF00E08A, 0xFFFF8AB5, 0xFFF0105A, 0xFF8AC8FF, 0xFF1E7AFF, 0xFFE0FF8A, 0xFFA8E000,
        ),
    ),
    Skin(
        "candy", "Candy", byAd = true,
        rim = listOf(Color(0xFFFFD6EC), Color(0xFFFF7EB8), Color(0xFFC23A7A)),
        tray = listOf(Color(0xFF5A1A40), Color(0xFF3A0E2A)),
        gems = gems(
            0xFFFFC2DC, 0xFFFF5A9A, 0xFFFFE0B0, 0xFFFFA040, 0xFFD8C2FF, 0xFF9A6AF0, 0xFFC2FFE0, 0xFF40D08A, 0xFFFFF5B0, 0xFFF0C820,
            0xFFB0E8FF, 0xFF40A8F0, 0xFFFFC8C8, 0xFFF05050, 0xFFF0C8FF, 0xFFC050E0, 0xFFC8FFC8, 0xFF5AC85A, 0xFFFFD8A8, 0xFFE89040,
        ),
    ),
    Skin(
        "wood", "Wood", streakDay = 14,
        rim = listOf(Color(0xFFE8C38A), Color(0xFFA0642A), Color(0xFF5A3410)),
        tray = listOf(Color(0xFF6A4220), Color(0xFF4A2C14)),
        gems = gems(
            0xFFF0C890, 0xFFB0702A, 0xFFE8D8B0, 0xFFA89060, 0xFFC8E0A0, 0xFF6A8A30, 0xFFF0A890, 0xFFB04A2A, 0xFFD8C0A8, 0xFF7A5A40,
            0xFFF8E0A0, 0xFFC8A040, 0xFFB8D8C8, 0xFF4A8A6A, 0xFFE0B0A0, 0xFF9A5A4A, 0xFFD0D0A0, 0xFF8A8A40, 0xFFE8C8B0, 0xFFA87A5A,
        ),
    ),
    Skin(
        "gold", "Gold", streakDay = 30,
        rim = listOf(Color(0xFFFFF1B0), Color(0xFFD9A020), Color(0xFF8A5A00)),
        tray = listOf(Color(0xFF2A1E08), Color(0xFF140E02)),
        gems = gems(
            0xFFFFF3B0, 0xFFE0A800, 0xFFFFE0A0, 0xFFD08A00, 0xFFFFF8D0, 0xFFC8A040, 0xFFFFD890, 0xFFB87A10, 0xFFF8F0C0, 0xFFA89050,
            0xFFFFE8B8, 0xFFE09030, 0xFFFFF0A0, 0xFFF0C000, 0xFFF0E0C0, 0xFFB09060, 0xFFFFE090, 0xFFD8A020, 0xFFFFF8E0, 0xFFD0B070,
        ),
    ),
)

object Skins {
    fun owned(store: GameStore, skin: Skin) = (skin.streakDay == 0 && !skin.byAd) || store.getBool("skin_owned_${skin.id}", false)
    fun selected(store: GameStore) = SKINS.find { it.id == store.getString("skin") }?.takeIf { owned(store, it) } ?: SKINS[0]
    fun select(store: GameStore, skin: Skin) = store.putString("skin", skin.id)
    fun unlock(store: GameStore, skin: Skin) = store.putBool("skin_owned_${skin.id}", true)
}

val LocalSkin = staticCompositionLocalOf { SKINS[0] }

/** Gem colours of the selected skin (falls back to the default [GEMS]). */
val activeGems: List<Pair<Color, Color>>
    @Composable get() = LocalSkin.current.gems ?: GEMS

/** The skin's board colours applied over the game's theme. */
fun GameTheme.withSkin(skin: Skin) = copy(rim = skin.rim ?: rim, tray = skin.tray ?: tray)

class GameInfo(val name: String, val pkg: String, val blurb: String, val theme: GameTheme)

val ALL_GAMES = listOf(
    GameInfo("Block Grid", "com.techeliteclub.game.blockpuzzle", "Drag, drop, blast lines", Themes.Block),
    GameInfo("Color Sort", "com.techeliteclub.game.colorsort", "Pour and match colours", Themes.Sort),
    GameInfo("Crown Logic", "com.techeliteclub.game.crownlogic", "One crown per row", Themes.Crown),
    GameInfo("Arrow Escape", "com.techeliteclub.game.arrowescape", "Clear the arrows", Themes.Arrow),
    GameInfo("Tile Mahjong", "com.techeliteclub.game.tilemahjong", "Match free tiles", Themes.Mahjong),
    GameInfo("Sudoku", "com.techeliteclub.game.sudoku", "Calm number logic", Themes.Sudoku),
    GameInfo("Word Search", "com.techeliteclub.game.wordsearch", "Find hidden words", Themes.Word),
    GameInfo("Solitaire", "com.techeliteclub.game.solitaire", "The classic card game", Themes.Solitaire),
    GameInfo("Car Escape", "com.techeliteclub.game.carescape", "Free the red car", Themes.Car),
    GameInfo("2048", "com.techeliteclub.game.merge2048", "Swipe and merge", Themes.M2048),
    GameInfo("Mini Sudoku", "com.techeliteclub.game.minisudoku", "Six by six, in minutes", Themes.MiniSudoku),
    GameInfo("Sun & Moon", "com.techeliteclub.game.sunmoon", "Balance suns and moons", Themes.SunMoon),
    GameInfo("Number Path", "com.techeliteclub.game.numberpath", "One line, every square", Themes.Path),
)

fun isInstalled(context: Context, pkg: String) = context.packageManager.getLaunchIntentForPackage(pkg) != null

/** Opens the game if installed, else its Play Store page (app, then web). */
fun openGame(context: Context, pkg: String) {
    context.packageManager.getLaunchIntentForPackage(pkg)?.let { return context.startActivity(it) }
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(market)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
    }
}
