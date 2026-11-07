package com.techeliteclub.game.crownlogic

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.PALETTE
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val EMPTY = 0
private const val MARK = 1
private const val CROWN = 2

@Composable
fun CrownLogicGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    var level by remember { mutableIntStateOf(store.get("level", 1)) }
    val puzzle = remember(level) { newPuzzle(level, Random(level.toLong())) }
    var cells by remember(level) { mutableStateOf(IntArray(puzzle.n * puzzle.n)) }
    val n = puzzle.n
    val crowns = cells.indices.filter { cells[it] == CROWN }
    val bad = conflicts(puzzle, crowns)
    val won = isSolved(puzzle, crowns)
    var seconds by remember(level) { mutableIntStateOf(0) }
    LaunchedEffect(level, won) { while (!won) { delay(1000); seconds++ } }
    val lineColor = MaterialTheme.colorScheme.onSurface

    fun restart() { cells = IntArray(n * n); seconds = 0 }

    fun hint() { // place one correct crown the player is missing
        val r = (0 until n).firstOrNull { cells[it * n + puzzle.solution[it]] != CROWN } ?: return
        cells = cells.copyOf().also { it[r * n + puzzle.solution[r]] = CROWN }
    }

    GameScaffold("Crown Logic", "Level $level · ${crowns.size}/$n 👑 · ${time(seconds)}", ads, ::restart, "Hint", ::hint) {
        Text(
            "One 👑 in every row, column and colour. Crowns can't touch, not even corners. Tap: ✕ → 👑 → empty.",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(12.dp))
        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val size = maxWidth / n
            Column {
                for (r in 0 until n) Row {
                    for (c in 0 until n) {
                        val i = r * n + c
                        val state = cells[i]
                        val region = puzzle.regions[i]
                        // Thick line where the neighbour belongs to another region (or at the edge).
                        val top = r == 0 || puzzle.regions[i - n] != region
                        val bottom = r == n - 1 || puzzle.regions[i + n] != region
                        val left = c == 0 || puzzle.regions[i - 1] != region
                        val right = c == n - 1 || puzzle.regions[i + 1] != region
                        Box(
                            Modifier.size(size)
                                .background(if (i in bad) Color(0xFFE57373) else PALETTE[region].copy(alpha = 0.6f))
                                .drawBehind {
                                    val thin = 0.5.dp.toPx()
                                    val thick = 3.dp.toPx()
                                    fun edge(on: Boolean, a: Offset, b: Offset) =
                                        drawLine(lineColor.copy(alpha = if (on) 0.9f else 0.2f), a, b, if (on) thick else thin)
                                    edge(top, Offset(0f, 0f), Offset(this.size.width, 0f))
                                    edge(bottom, Offset(0f, this.size.height), Offset(this.size.width, this.size.height))
                                    edge(left, Offset(0f, 0f), Offset(0f, this.size.height))
                                    edge(right, Offset(this.size.width, 0f), Offset(this.size.width, this.size.height))
                                }
                                .clickable {
                                    cells = cells.copyOf().also { it[i] = (state + 1) % 3 }
                                    feedback.tap()
                                }
                                .semantics {
                                    contentDescription = "Row ${r + 1} column ${c + 1}, " +
                                        listOf("empty", "marked", "crown")[state]
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            when (state) {
                                MARK -> Text("✕", color = Color.Black.copy(alpha = 0.55f), fontSize = (size.value * 0.35f).sp)
                                CROWN -> Text("👑", fontSize = (size.value * 0.5f).sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (won) {
        ResultDialog("Solved!", "Level $level in ${time(seconds)}.", "Next level", {
            ads.levelDone()
            level++
            store.put("level", level)
        }, emoji = "👑")
    }
}

private fun time(s: Int) = "%d:%02d".format(s / 60, s % 60)
