package com.techeliteclub.game.arrowescape

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlin.math.roundToInt
import kotlin.random.Random

private const val LIVES = 3

private fun colorOf(d: Dir) = when (d) {
    Dir.UP -> Color(0xFF1E88E5)
    Dir.DOWN -> Color(0xFF8E24AA)
    Dir.LEFT -> Color(0xFF00897B)
    Dir.RIGHT -> Color(0xFFF4511E)
}

@Composable
fun ArrowEscapeGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    var level by remember { mutableIntStateOf(store.get("level", 1)) }
    val n = sizeFor(level)
    var board by remember(level) { mutableStateOf(newLevel(level, Random(level.toLong()))) }
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
        board = newLevel(level, Random(level.toLong())); lives = LIVES; hinted = null; blocked = null; leaving = emptyMap()
    }

    fun tap(i: Int) {
        val d = board[i] ?: return
        if (lives == 0) return
        if (canExit(board, n, i)) {
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

    GameScaffold("Arrow Escape", "Level $level · ${"❤".repeat(lives)}${"♡".repeat(LIVES - lives)}", ads, ::restart, "Hint",
        onHint = { hinted = board.indices.firstOrNull { canExit(board, n, it) } }) {
        Text("Tap an arrow to send it off the board. Blocked arrows cost a life.",
            style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        BoxWithConstraints(
            Modifier.fillMaxWidth().aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        ) {
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
                                .clickable(enabled = d != null) { tap(i) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (d != null) {
                                ArrowTile(d, size, when (i) {
                                    hinted -> Color(0xFF43A047)
                                    blocked -> Color(0xFFE53935)
                                    else -> colorOf(d)
                                })
                            }
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
                val dx = maxWidth * d.dc * anim.value
                val dy = maxWidth * d.dr * anim.value
                Box(
                    Modifier.offset(size * (i % n) + dx, size * (i / n) + dy).size(size).padding(3.dp)
                        .alpha(1f - anim.value),
                    contentAlignment = Alignment.Center,
                ) { ArrowTile(d, size, colorOf(d)) }
            }
        }
    }

    when {
        won -> ResultDialog("Board cleared!", "Level $level complete with ${"❤".repeat(lives)} left.", "Next level", {
            ads.levelDone()
            level++
            store.put("level", level)
        }, emoji = "🏹")
        lives == 0 -> ResultDialog("Out of lives", "Try this level again.", "Retry", { ads.levelDone(); restart() },
            secondButton = if (ads.rewardedReady) "▶ +1 life" else null,
            onSecond = { ads.showRewarded { lives = 1 } },
            emoji = "💔")
    }
}

@Composable
private fun ArrowTile(d: Dir, size: Dp, color: Color) {
    Box(
        Modifier.size(size - 6.dp).background(color, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(d.symbol, fontSize = (size.value * 0.5f).sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
