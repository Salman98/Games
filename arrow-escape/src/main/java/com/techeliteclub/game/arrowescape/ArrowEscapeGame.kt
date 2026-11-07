package com.techeliteclub.game.arrowescape

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.Bezel
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.Hint
import com.techeliteclub.game.core.Pill
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.Tool
import com.techeliteclub.game.core.gem
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlin.math.roundToInt
import kotlin.random.Random

private const val LIVES = 3

/** Gem colours per direction (light top, deep base). */
fun colorsOf(d: Dir) = when (d) {
    Dir.UP -> Color(0xFF4A8DFF) to Color(0xFF1F5EE0)
    Dir.DOWN -> Color(0xFFB07BFF) to Color(0xFF7A2FD6)
    Dir.LEFT -> Color(0xFF2FD0A0) to Color(0xFF0F9A72)
    Dir.RIGHT -> Color(0xFFFF9A52) to Color(0xFFEF5A00)
}

/** Rotation of the "→" forward icon for each direction. */
private fun angleOf(d: Dir) = when (d) {
    Dir.RIGHT -> 0f
    Dir.DOWN -> 90f
    Dir.LEFT -> 180f
    Dir.UP -> 270f
}

@Composable
fun ArrowTile(d: Dir, size: Dp, modifier: Modifier = Modifier, glow: Boolean = false, alarm: Boolean = false) {
    val (top, base) = if (alarm) Color(0xFFFF8A8A) to Color(0xFFD6143A) else colorsOf(d)
    Box(
        modifier.size(size)
            .then(if (glow) Modifier.shadow(14.dp, RoundedCornerShape(12.dp), ambientColor = Color(0xFFFFE678), spotColor = Color(0xFFFFE678)) else Modifier)
            .gem(top, base, 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(GameIcons.Forward, null, tint = Color.White, modifier = Modifier.size(size * 0.55f).rotate(angleOf(d)))
    }
}

@Composable
fun ArrowEscapeGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    var level by remember { mutableIntStateOf(store.get("level", 1)) }
    val n = sizeFor(level)
    var board by remember(level) { mutableStateOf(newLevel(level, Random(level.toLong()))) }
    var history by remember(level) { mutableStateOf(listOf<List<Dir?>>()) }
    var lives by remember(level) { mutableIntStateOf(LIVES) }
    var hinted by remember(level) { mutableStateOf<Int?>(null) }
    var blocked by remember(level) { mutableStateOf<Int?>(null) }
    var blockedTick by remember(level) { mutableIntStateOf(0) }
    var leaving by remember(level) { mutableStateOf(mapOf<Int, Dir>()) }
    val shake = remember { Animatable(0f) }
    val won = board.all { it == null } && leaving.isEmpty()

    LaunchedEffect(blockedTick) {
        if (blockedTick > 0) for (x in listOf(10f, -10f, 7f, -7f, 0f)) shake.animateTo(x, tween(45))
    }

    fun restart() {
        board = newLevel(level, Random(level.toLong())); history = emptyList(); lives = LIVES; hinted = null; blocked = null; leaving = emptyMap()
    }

    fun tap(i: Int) {
        val d = board[i] ?: return
        if (lives == 0) return
        if (canExit(board, n, i)) {
            history = history + listOf(board)
            board = board.toMutableList().also { it[i] = null }
            leaving = leaving + (i to d)
            blocked = null
            if (hinted == i) hinted = null
            feedback.good()
        } else {
            blocked = i
            blockedTick++
            lives--
            feedback.bad()
        }
    }

    GameScaffold(
        ads, "ARROW ESCAPE",
        pills = listOf(Pill("LEVEL $level"), Pill("$lives / $LIVES", GameIcons.Heart, gold = true)),
        tools = listOf(
            Tool("Undo", GameIcons.Undo, enabled = history.isNotEmpty()) { board = history.last(); history = history.dropLast(1); blocked = null },
            Tool("Restart", GameIcons.Restart, onClick = ::restart),
        ),
        hint = Hint("Hint", GameIcons.Bulb, levelKey = level) { hinted = board.indices.firstOrNull { canExit(board, n, it) } },
    ) {
        Bezel(Modifier.fillMaxWidth().aspectRatio(1f), padding = 10.dp) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val size = maxWidth / n
                Column {
                    for (r in 0 until n) Row {
                        for (c in 0 until n) {
                            val i = r * n + c
                            val d = board[i]
                            Box(
                                Modifier.size(size)
                                    .offset { IntOffset(if (i == blocked) shake.value.roundToInt() else 0, 0) }
                                    .padding(3.dp)
                                    .then(if (d != null) Modifier.clickable { tap(i) } else Modifier)
                                    .semantics { if (d != null) contentDescription = "Arrow pointing ${d.name.lowercase()}" },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (d != null) ArrowTile(d, size - 6.dp, glow = i == hinted, alarm = i == blocked)
                            }
                        }
                    }
                }
                // Freed arrows slide off the board and fade.
                for ((i, d) in leaving) key(i) {
                    val anim = remember { Animatable(0f) }
                    LaunchedEffect(Unit) {
                        anim.animateTo(1f, tween(320))
                        leaving = leaving - i
                    }
                    Box(
                        Modifier
                            .offset(size * (i % n) + maxWidth * d.dc * anim.value, size * (i / n) + maxWidth * d.dr * anim.value)
                            .size(size).padding(3.dp).alpha(1f - anim.value),
                    ) { ArrowTile(d, size - 6.dp) }
                }
            }
        }
    }

    when {
        won -> {
            val stars = 1 + lives.coerceAtMost(2)
            ResultDialog("Board cleared", "Level: $level\nLives left: $lives", "Next level", {
                ads.levelDone()
                store.put("stars", store.get("stars") + stars)
                level++
                store.put("level", level)
            }, stars = stars)
        }
        lives == 0 -> ResultDialog(
            "Out of lives", "Try this level again.", "Retry", { ads.levelDone(); restart() },
            secondButton = if (ads.rewardedReady) "▶ +1 life" else null,
            onSecond = { ads.showRewarded { lives = 1 } },
            win = false,
        )
    }
}
