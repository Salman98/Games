package com.salman98.games.wordsearch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salman98.games.core.Ads
import com.salman98.games.core.GameScaffold
import com.salman98.games.core.PALETTE
import com.salman98.games.core.ResultDialog
import com.salman98.games.core.rememberStore
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordSearchGame(ads: Ads) {
    val store = rememberStore()
    var level by remember { mutableIntStateOf(store.get("level", 1)) }
    val puzzle = remember(level) { newPuzzle(level, Random(level.toLong())) }
    var found by remember(level) { mutableStateOf(setOf<String>()) }
    var start by remember(level) { mutableStateOf<Int?>(null) }
    var hinted by remember(level) { mutableStateOf<Int?>(null) }
    val n = puzzle.n
    val words = puzzle.placed.keys.toList()
    val colorOf = found.flatMap { w -> puzzle.placed.getValue(w).map { it to PALETTE[words.indexOf(w) % PALETTE.size] } }.toMap()

    fun tap(i: Int) {
        val a = start
        if (a == null) { start = i; return }
        wordAt(puzzle, a, i, found)?.let { found = found + it; hinted = null }
        start = null
    }

    GameScaffold("Word Search", "Level $level · ${found.size}/${words.size}", ads,
        { found = emptySet(); start = null; hinted = null }, "Hint",
        onHint = { hinted = words.firstOrNull { it !in found }?.let { puzzle.placed.getValue(it).first() } }) {
        Text("Tap the first letter, then the last letter of a word.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val cell = maxWidth / n
            Column {
                for (r in 0 until n) Row {
                    for (c in 0 until n) {
                        val i = r * n + c
                        val bg = when {
                            i == start -> MaterialTheme.colorScheme.primary
                            i == hinted -> MaterialTheme.colorScheme.tertiary
                            i in colorOf -> colorOf.getValue(i).copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                        Box(
                            Modifier.size(cell).background(bg, CircleShape).clickable { tap(i) },
                            contentAlignment = Alignment.Center,
                        ) { Text("${puzzle.letters[i]}", fontSize = (cell.value * 0.5f).sp) }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for (w in words) Text(
                w,
                textDecoration = if (w in found) TextDecoration.LineThrough else null,
                color = if (w in found) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    if (found.size == words.size) {
        ResultDialog("All words found!", "Level $level complete.", "Next level", {
            ads.levelDone()
            level++
            store.put("level", level)
        })
    }
}
