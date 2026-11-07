package com.techeliteclub.game.wordsearch

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.PALETTE
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordSearchGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    var level by remember { mutableIntStateOf(store.get("level", 1)) }
    val puzzle = remember(level) { newPuzzle(level, Random(level.toLong())) }
    var found by remember(level) { mutableStateOf(setOf<String>()) }
    var start by remember(level) { mutableStateOf<Int?>(null) }
    var current by remember(level) { mutableStateOf<Int?>(null) }
    var hinted by remember(level) { mutableStateOf<Int?>(null) }
    val n = puzzle.n
    val words = puzzle.placed.keys.toList()
    val colorOf = found.flatMap { w -> puzzle.placed.getValue(w).map { it to PALETTE[words.indexOf(w) % PALETTE.size] } }.toMap()
    // Letters under the finger (or between the two tapped letters) right now.
    val selecting = start?.let { a -> line(n, a, current ?: a) }.orEmpty().toSet()

    fun finish(a: Int, b: Int) {
        val w = wordAt(puzzle, a, b, found)
        if (w != null) { found = found + w; hinted = null; feedback.good() } else if (a != b) feedback.bad()
    }

    GameScaffold("Word Search", "Level $level · ${found.size}/${words.size} words", ads,
        { found = emptySet(); start = null; current = null; hinted = null }, "Hint",
        onHint = { hinted = words.firstOrNull { it !in found }?.let { puzzle.placed.getValue(it).first() } }) {
        Text("Swipe across a word, or tap its first and last letter.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        BoxWithConstraints(
            Modifier.fillMaxWidth().aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
        ) {
            val cell = maxWidth / n
            fun cellAt(o: Offset, sizePx: Float): Int {
                val c = (o.x / (sizePx / n)).toInt().coerceIn(0, n - 1)
                val r = (o.y / (sizePx / n)).toInt().coerceIn(0, n - 1)
                return r * n + c
            }
            Column(
                Modifier
                    .pointerInput(puzzle, found) {
                        detectDragGestures(
                            onDragStart = { o -> start = cellAt(o, size.width.toFloat()); current = start },
                            onDragEnd = {
                                val a = start; val b = current
                                if (a != null && b != null) finish(a, b)
                                start = null; current = null
                            },
                            onDragCancel = { start = null; current = null },
                        ) { change, _ -> change.consume(); current = cellAt(change.position, size.width.toFloat()) }
                    }
                    .pointerInput(puzzle, found) {
                        detectTapGestures { o ->
                            val i = cellAt(o, size.width.toFloat())
                            val a = start
                            if (a == null) { start = i; current = i; feedback.tap() }
                            else { finish(a, i); start = null; current = null }
                        }
                    }
            ) {
                for (r in 0 until n) Row {
                    for (c in 0 until n) {
                        val i = r * n + c
                        val bg = when {
                            i in selecting -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            i == hinted -> MaterialTheme.colorScheme.tertiary
                            i in colorOf -> colorOf.getValue(i).copy(alpha = 0.45f)
                            else -> androidx.compose.ui.graphics.Color.Transparent
                        }
                        Box(Modifier.size(cell).padding(1.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) {
                            Text("${puzzle.letters[i]}", fontSize = (cell.value * 0.5f).sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (w in words) {
                val done = w in found
                Text(
                    w,
                    modifier = Modifier
                        .background(
                            if (done) PALETTE[words.indexOf(w) % PALETTE.size].copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    if (found.size == words.size) {
        ResultDialog("All words found!", "Level $level complete.", "Next level", {
            ads.levelDone()
            level++
            store.put("level", level)
        }, emoji = "🔤")
    }
}
