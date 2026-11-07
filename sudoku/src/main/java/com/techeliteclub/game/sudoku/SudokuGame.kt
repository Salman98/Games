package com.techeliteclub.game.sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SudokuGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    var level by remember { mutableIntStateOf(store.get("level", 1)) }
    val puzzle = remember(level) { newPuzzle(level, Random(level.toLong())) }
    var grid by remember(level) { mutableStateOf(puzzle.givens.copyOf()) }
    var selected by remember(level) { mutableStateOf<Int?>(null) }
    var mistakes by remember(level) { mutableIntStateOf(0) }
    var seconds by remember(level) { mutableIntStateOf(0) }
    val won = isSolved(grid, puzzle.solution)
    LaunchedEffect(level, won) { while (!won) { delay(1000); seconds++ } }

    val line = MaterialTheme.colorScheme.onSurface
    val peerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    val sameColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    val selColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    // How many of each digit are correctly placed (to grey out finished digits).
    val placed = IntArray(10).also { counts -> for (i in 0 until 81) if (grid[i] == puzzle.solution[i]) counts[grid[i]]++ }

    fun set(i: Int, d: Int) {
        if (puzzle.givens[i] != 0) return
        grid = grid.copyOf().also { it[i] = d }
        when {
            d == 0 -> feedback.tap()
            d == puzzle.solution[i] -> feedback.good()
            else -> { mistakes++; feedback.bad() }
        }
    }

    fun hint() {
        val i = selected?.takeIf { grid[it] != puzzle.solution[it] }
            ?: (0 until 81).firstOrNull { grid[it] != puzzle.solution[it] } ?: return
        selected = i
        set(i, puzzle.solution[i])
    }

    fun restart() { grid = puzzle.givens.copyOf(); mistakes = 0; seconds = 0; selected = null }

    GameScaffold("Sudoku", "Level $level · ✕ $mistakes · ${time(seconds)}", ads, ::restart, "Hint", ::hint) {
        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val cell = maxWidth / 9
            val sel = selected
            Column {
                for (r in 0 until 9) Row {
                    for (c in 0 until 9) {
                        val i = r * 9 + c
                        val d = grid[i]
                        val given = puzzle.givens[i] != 0
                        val wrong = d != 0 && d != puzzle.solution[i]
                        val peer = sel != null && (sel / 9 == r || sel % 9 == c || (sel / 27 == r / 3 && sel % 9 / 3 == c / 3))
                        val bg = when {
                            i == sel -> selColor
                            sel != null && d != 0 && d == grid[sel] -> sameColor
                            peer -> peerColor
                            else -> Color.Transparent
                        }
                        Box(
                            Modifier.size(cell).background(bg)
                                .drawBehind {
                                    // Thin lines everywhere, thick lines around each 3x3 box.
                                    val w = this.size.width
                                    val h = this.size.height
                                    fun edge(thick: Boolean, a: Offset, b: Offset) = drawLine(
                                        line.copy(alpha = if (thick) 0.9f else 0.2f), a, b,
                                        (if (thick) 2.5.dp else 0.5.dp).toPx(),
                                    )
                                    edge(r % 3 == 0, Offset(0f, 0f), Offset(w, 0f))
                                    edge(c % 3 == 0, Offset(0f, 0f), Offset(0f, h))
                                    if (r == 8) edge(true, Offset(0f, h), Offset(w, h))
                                    if (c == 8) edge(true, Offset(w, 0f), Offset(w, h))
                                }
                                .clickable { selected = i; feedback.tap() },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (d != 0) Text(
                                "$d",
                                fontSize = (cell.value * 0.55f).sp,
                                fontWeight = if (given) FontWeight.Bold else FontWeight.Medium,
                                color = when {
                                    wrong -> Color(0xFFE53935)
                                    given -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.primary
                                },
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        // Number pad: digit plus how many are still to place. Finished digits fade out.
        for (row in listOf(1..5, 6..10)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (d in row) {
                    val erase = d == 10
                    val left = if (erase) 0 else 9 - placed[d]
                    Column(
                        Modifier.weight(1f).height(58.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(10.dp))
                            .clickable(enabled = erase || left > 0) { selected?.let { set(it, if (erase) 0 else d) } }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        val faded = !erase && left == 0
                        Text(if (erase) "⌫" else "$d", fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = if (faded) 0.25f else 1f))
                        if (!erase) Text("$left", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = if (faded) 0.25f else 0.7f))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }

    if (won) {
        ResultDialog("Solved!", "Level $level in ${time(seconds)} with $mistakes mistakes.", "Next level", {
            ads.levelDone()
            level++
            store.put("level", level)
        }, emoji = "🧩")
    }
}

private fun time(s: Int) = "%d:%02d".format(s / 60, s % 60)
