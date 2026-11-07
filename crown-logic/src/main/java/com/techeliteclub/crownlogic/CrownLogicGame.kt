package com.techeliteclub.crownlogic

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.core.Ads
import com.techeliteclub.core.GameScaffold
import com.techeliteclub.core.PALETTE
import com.techeliteclub.core.ResultDialog
import com.techeliteclub.core.rememberStore
import kotlin.random.Random

private const val EMPTY = 0
private const val MARK = 1
private const val CROWN = 2

@Composable
fun CrownLogicGame(ads: Ads) {
    val store = rememberStore()
    var level by remember { mutableIntStateOf(store.get("level", 1)) }
    val puzzle = remember(level) { newPuzzle(level, Random(level.toLong())) }
    var cells by remember(level) { mutableStateOf(IntArray(puzzle.n * puzzle.n)) }
    val n = puzzle.n
    val crowns = cells.indices.filter { cells[it] == CROWN }
    val bad = conflicts(puzzle, crowns)
    val won = isSolved(puzzle, crowns)

    fun restart() { cells = IntArray(n * n) }

    fun hint() { // place one correct crown the player is missing
        val r = (0 until n).firstOrNull { cells[it * n + puzzle.solution[it]] != CROWN } ?: return
        cells = cells.copyOf().also { it[r * n + puzzle.solution[r]] = CROWN }
    }

    GameScaffold("Crown Logic", "Level $level · ${n}×$n", ads, ::restart, "Hint", ::hint) {
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
                        Box(
                            Modifier.size(size).padding(1.dp)
                                .background(PALETTE[puzzle.regions[i]].copy(alpha = 0.55f))
                                .clickable { cells = cells.copyOf().also { it[i] = (state + 1) % 3 } }
                                .semantics { contentDescription = "Row ${r + 1} column ${c + 1}" },
                            contentAlignment = Alignment.Center,
                        ) {
                            when (state) {
                                MARK -> Text("✕", color = Color.DarkGray)
                                CROWN -> Text(
                                    "👑", fontSize = (size.value * 0.5f).sp,
                                    modifier = if (i in bad) Modifier.background(Color.Red.copy(alpha = 0.5f)) else Modifier,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (won) {
        ResultDialog("Solved!", "Level $level complete.", "Next level", {
            ads.levelDone()
            level++
            store.put("level", level)
        })
    }
}
