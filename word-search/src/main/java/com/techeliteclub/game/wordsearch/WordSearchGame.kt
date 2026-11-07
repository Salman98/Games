package com.techeliteclub.game.wordsearch

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.Bezel
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.Hint
import com.techeliteclub.game.core.LocalPlayMode
import com.techeliteclub.game.core.Pill
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.Tool
import com.techeliteclub.game.core.displayStyle
import com.techeliteclub.game.core.glass
import com.techeliteclub.game.core.LocalGameTheme
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlin.random.Random

private val CAPSULES = listOf(
    Color(0xFFFF8A65), Color(0xFF3FBF72), Color(0xFF4F8DFF), Color(0xFFB07BFF), Color(0xFFFFB300),
    Color(0xFF00ACC1), Color(0xFFE91E63), Color(0xFF8D6E63), Color(0xFF7CB342), Color(0xFF5C6BC0),
)
private val LETTER = Color(0xFF5A1F45)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordSearchGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val t = LocalGameTheme.current
    val mode = LocalPlayMode.current
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val puzzle = remember(level) { newPuzzle(level, Random(mode.seedFor(level))) }
    var found by remember(level) { mutableStateOf(listOf<String>()) }
    var start by remember(level) { mutableStateOf<Int?>(null) }
    var current by remember(level) { mutableStateOf<Int?>(null) }
    var hinted by remember(level) { mutableStateOf<Int?>(null) }
    val n = puzzle.n
    val words = puzzle.placed.keys.toList()

    fun finish(a: Int, b: Int) {
        val w = wordAt(puzzle, a, b, found.toSet())
        if (w != null) {
            found = found + w; hinted = null
            feedback.clear() // the win screen plays the win sound
        } else if (a != b) feedback.bad()
    }

    GameScaffold(
        ads, "WORD SEARCH",
        pills = listOf(Pill(mode.label(level)), Pill("${found.size} / ${words.size} WORDS")),
        tools = listOf(Tool("Restart", GameIcons.Restart) { found = emptyList(); start = null; current = null; hinted = null }),
        hint = Hint("Hint", GameIcons.Bulb, levelKey = mode.hintKey(level)) {
            hinted = words.firstOrNull { it !in found }?.let { puzzle.placed.getValue(it).first() }
        },
    ) {
        Bezel(Modifier.fillMaxWidth().aspectRatio(1f), padding = 6.dp) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val cell = maxWidth / n
                fun cellAt(o: Offset, widthPx: Float): Int {
                    val c = (o.x / (widthPx / n)).toInt().coerceIn(0, n - 1)
                    val r = (o.y / (widthPx / n)).toInt().coerceIn(0, n - 1)
                    return r * n + c
                }
                // Capsules behind the letters: found words, the live selection, the hint.
                Canvas(Modifier.fillMaxSize()) {
                    val step = size.width / n
                    fun centre(i: Int) = Offset((i % n + 0.5f) * step, (i / n + 0.5f) * step)
                    found.forEach { w ->
                        val cells = puzzle.placed.getValue(w)
                        capsule(centre(cells.first()), centre(cells.last()), step, CAPSULES[words.indexOf(w) % CAPSULES.size].copy(alpha = 0.5f))
                    }
                    hinted?.let { capsule(centre(it), centre(it), step, Color(0x99FFC533)) }
                    val a = start
                    val b = current
                    if (a != null && b != null && line(n, a, b) != null) capsule(centre(a), centre(b), step, Color(0x73FF2D7A))
                }
                Column(
                    Modifier
                        .pointerInput(puzzle, found) {
                            detectDragGestures(
                                onDragStart = { o -> start = cellAt(o, size.width.toFloat()); current = start; feedback.tap() },
                                onDragEnd = {
                                    val a = start
                                    val b = current
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
                                if (a == null) { start = i; current = i; feedback.tap() } else { finish(a, i); start = null; current = null }
                            }
                        }
                ) {
                    for (r in 0 until n) Row {
                        for (c in 0 until n) {
                            Box(Modifier.size(cell), contentAlignment = Alignment.Center) {
                                Text("${puzzle.letters[r * n + c]}", style = displayStyle((cell.value * 0.52f).sp), color = LETTER)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (w in words) {
                val done = w in found
                val shape = RoundedCornerShape(50)
                Text(
                    w,
                    modifier = (if (done) Modifier.background(CAPSULES[words.indexOf(w) % CAPSULES.size], shape) else Modifier.glass(t, shape))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                )
            }
        }
    }

    if (found.size == words.size) {
        ResultDialog("All words found", "Level: $level\nWords: ${words.size}", if (mode.daily) "Done" else "Next level", {
            ads.levelDone()
            mode.win(store, level, 3) {
                level++
                store.put("level", level)
            }
        })
    }
}

/** A rounded highlight from cell centre [a] to [b]. */
private fun DrawScope.capsule(a: Offset, b: Offset, step: Float, fill: Color) =
    drawLine(fill, a, b, step * 0.8f, StrokeCap.Round)
