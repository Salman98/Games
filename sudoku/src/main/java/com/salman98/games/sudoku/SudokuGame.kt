package com.salman98.games.sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salman98.games.core.Ads
import com.salman98.games.core.GameScaffold
import com.salman98.games.core.ResultDialog
import com.salman98.games.core.rememberStore
import kotlin.random.Random

@Composable
fun SudokuGame(ads: Ads) {
    val store = rememberStore()
    var level by remember { mutableIntStateOf(store.get("level", 1)) }
    val puzzle = remember(level) { newPuzzle(level, Random(level.toLong())) }
    var grid by remember(level) { mutableStateOf(puzzle.givens.copyOf()) }
    var selected by remember(level) { mutableStateOf<Int?>(null) }
    val won = isSolved(grid, puzzle.solution)

    fun set(i: Int, d: Int) {
        if (puzzle.givens[i] != 0) return
        grid = grid.copyOf().also { it[i] = d }
    }

    fun hint() {
        val i = selected?.takeIf { grid[it] != puzzle.solution[it] }
            ?: (0 until 81).firstOrNull { grid[it] != puzzle.solution[it] } ?: return
        set(i, puzzle.solution[i])
    }

    GameScaffold("Sudoku", "Level $level", ads, { grid = puzzle.givens.copyOf() }, "Hint", ::hint) {
        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val cell = maxWidth / 9
            Column(Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface)) {
                for (r in 0 until 9) Row {
                    for (c in 0 until 9) {
                        val i = r * 9 + c
                        val d = grid[i]
                        val given = puzzle.givens[i] != 0
                        val wrong = d != 0 && d != puzzle.solution[i]
                        val bg = when {
                            i == selected -> MaterialTheme.colorScheme.primaryContainer
                            (r / 3 + c / 3) % 2 == 0 -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.surface
                        }
                        Box(
                            Modifier.size(cell).background(bg)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                .clickable { selected = i },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (d != 0) Text(
                                "$d",
                                fontSize = (cell.value * 0.55f).sp,
                                fontWeight = if (given) FontWeight.Bold else FontWeight.Normal,
                                color = if (wrong) Color.Red else if (given) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        for (row in listOf(1..5, 6..9)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (d in row) FilledTonalButton(onClick = { selected?.let { set(it, d) } }) { Text("$d") }
                if (row.last == 9) FilledTonalButton(onClick = { selected?.let { set(it, 0) } }) { Text("⌫") }
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
