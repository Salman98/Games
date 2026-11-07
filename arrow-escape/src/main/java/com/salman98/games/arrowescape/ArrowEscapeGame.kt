package com.salman98.games.arrowescape

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salman98.games.core.Ads
import com.salman98.games.core.GameScaffold
import com.salman98.games.core.ResultDialog
import com.salman98.games.core.rememberStore
import kotlin.random.Random

private const val LIVES = 3

@Composable
fun ArrowEscapeGame(ads: Ads) {
    val store = rememberStore()
    var level by remember { mutableIntStateOf(store.get("level", 1)) }
    val n = sizeFor(level)
    var board by remember(level) { mutableStateOf(newLevel(level, Random(level.toLong()))) }
    var lives by remember(level) { mutableIntStateOf(LIVES) }
    var hinted by remember(level) { mutableStateOf<Int?>(null) }
    var blocked by remember(level) { mutableStateOf<Int?>(null) }
    val won = board.all { it == null }

    fun restart() {
        board = newLevel(level, Random(level.toLong())); lives = LIVES; hinted = null; blocked = null
    }

    fun tap(i: Int) {
        if (board[i] == null || lives == 0) return
        if (canExit(board, n, i)) {
            board = board.toMutableList().also { it[i] = null }
            blocked = null
            if (hinted == i) hinted = null
        } else {
            blocked = i
            lives--
        }
    }

    GameScaffold("Arrow Escape", "Level $level · ${"❤".repeat(lives)}", ads, ::restart, "Hint",
        onHint = { hinted = board.indices.firstOrNull { canExit(board, n, it) } }) {
        Text("Tap an arrow to send it off the board. Blocked arrows cost a life.",
            style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val size = maxWidth / n
            Column {
                for (r in 0 until n) Row {
                    for (c in 0 until n) {
                        val i = r * n + c
                        val d = board[i]
                        val bg = when {
                            d == null -> Color.Transparent
                            i == hinted -> Color(0xFF66BB6A)
                            i == blocked -> Color(0xFFE57373)
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                        Box(
                            Modifier.size(size).padding(2.dp).background(bg, RoundedCornerShape(6.dp))
                                .clickable(enabled = d != null) { tap(i) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (d != null) Text(d.symbol, fontSize = (size.value * 0.5f).sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
    }

    when {
        won -> ResultDialog("Board cleared!", "Level $level complete.", "Next level", {
            ads.levelDone()
            level++
            store.put("level", level)
        })
        lives == 0 -> ResultDialog("Out of lives", "Try this level again.", "Retry", { ads.levelDone(); restart() },
            secondButton = if (ads.rewardedReady) "▶ +1 life" else null,
            onSecond = { ads.showRewarded { lives = 1 } })
    }
}
