package com.techeliteclub.game.nonogram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.Bezel
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.Hint
import com.techeliteclub.game.core.LevelSave
import com.techeliteclub.game.core.LocalGameTheme
import com.techeliteclub.game.core.LocalPlayMode
import com.techeliteclub.game.core.Pill
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.Tool
import com.techeliteclub.game.core.displayStyle
import com.techeliteclub.game.core.glass
import com.techeliteclub.game.core.pressable
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlin.random.Random

private val TILE = Color(0xFF0D3B42)
private val CLUE = Color(0xFFFFE58A)
private val CLUE_DONE = Color.White.copy(alpha = 0.35f)
private val PINK = listOf(Color(0xFFFF8AA8), Color(0xFFE2365F))
private val GUIDE = Color(0x80FFE58A)

/**
 * Clues on the top and left, squares below. [onPaint] gets (square, isStrokeStart) for taps and drags; null makes it a picture.
 * [clueFrac] is the share of [size] kept for the clues.
 */
@Composable
fun NonoBoard(p: Puzzle, size: Dp, cells: IntArray, clueFrac: Float = 0.3f, onPaint: ((Int, Boolean) -> Unit)? = null) {
    val n = p.n
    val clue = size * clueFrac
    val cell = (size - clue) / n
    val fs = (cell.value * 0.5f).sp
    Box(Modifier.size(size).semantics { contentDescription = "Nonogram, ${cells.count { it == FILL }} squares filled" }) {
        for (c in 0 until n) {
            val done = colRuns(p, cells, c) == p.cols[c]
            Column(
                Modifier.offset(clue + cell * c, 0.dp).size(cell, clue - 3.dp)
                    .background(if (c % 2 == 1) Color.White.copy(alpha = 0.06f) else Color.Transparent, RoundedCornerShape(6.dp)),
                verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally,
            ) { for (v in p.cols[c]) Text("$v", style = displayStyle(fs), color = if (done) CLUE_DONE else CLUE) }
        }
        for (r in 0 until n) {
            val done = rowRuns(p, cells, r) == p.rows[r]
            Row(
                Modifier.offset(0.dp, clue + cell * r).size(clue - 3.dp, cell)
                    .background(if (r % 2 == 1) Color.White.copy(alpha = 0.06f) else Color.Transparent, RoundedCornerShape(6.dp))
                    .padding(end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(cell * 0.18f, Alignment.End), verticalAlignment = Alignment.CenterVertically,
            ) { for (v in p.rows[r]) Text("$v", style = displayStyle(fs), color = if (done) CLUE_DONE else CLUE) }
        }
        val gridMod = if (onPaint == null) Modifier else Modifier
            .pointerInput(onPaint) {
                fun at(o: Offset): Int? {
                    val s = this.size.width.toFloat() / n
                    val r = (o.y / s).toInt()
                    val c = (o.x / s).toInt()
                    return if (o.x >= 0 && o.y >= 0 && r < n && c < n) r * n + c else null
                }
                detectDragGestures(onDragStart = { o -> at(o)?.let { onPaint(it, true) } }) { change, _ ->
                    change.consume()
                    at(change.position)?.let { onPaint(it, false) }
                }
            }
            .pointerInput(onPaint) {
                detectTapGestures { o ->
                    val s = this.size.width.toFloat() / n
                    val r = (o.y / s).toInt()
                    val c = (o.x / s).toInt()
                    if (r in 0 until n && c in 0 until n) onPaint(r * n + c, true)
                }
            }
        Canvas(Modifier.offset(clue, clue).size(cell * n).then(gridMod)) {
            val s = this.size.width / n
            val gap = s * 0.06f
            val r = CornerRadius(s * 0.16f)
            for (i in 0 until n * n) {
                val o = Offset((i % n) * s + gap, (i / n) * s + gap)
                val sz = Size(s - 2 * gap, s - 2 * gap)
                when (cells[i]) {
                    FILL -> {
                        drawRoundRect(Brush.verticalGradient(PINK, o.y, o.y + sz.height), o, sz, r)
                        drawRoundRect(Color.White.copy(alpha = 0.28f), o, Size(sz.width, sz.height * 0.42f), r)
                    }
                    else -> drawRoundRect(TILE, o, sz, r)
                }
                if (cells[i] == CROSS) {
                    val m = s * 0.3f
                    val a = Offset((i % n) * s + m, (i / n) * s + m)
                    val b = Offset((i % n + 1) * s - m, (i / n + 1) * s - m)
                    drawLine(CLUE_DONE, a, b, s * 0.08f, StrokeCap.Round)
                    drawLine(CLUE_DONE, Offset(a.x, b.y), Offset(b.x, a.y), s * 0.08f, StrokeCap.Round)
                }
            }
            // Guide lines every 5 squares make counting easier on big boards.
            for (k in 5 until n step 5) {
                drawLine(GUIDE, Offset(k * s, 0f), Offset(k * s, this.size.height), 2.dp.toPx())
                drawLine(GUIDE, Offset(0f, k * s), Offset(this.size.width, k * s), 2.dp.toPx())
            }
        }
    }
}

/** Fill / Mark-empty switch under the board. */
@Composable
private fun ModeSwitch(cross: Boolean, onChange: (Boolean) -> Unit) {
    val t = LocalGameTheme.current
    Row(Modifier.glass(t, RoundedCornerShape(50)).padding(5.dp)) {
        for ((label, isCross) in listOf("Fill" to false, "Mark empty" to true)) {
            val on = cross == isCross
            Box(
                Modifier.pressable(label = label) { onChange(isCross) }
                    .background(if (on) Brush.verticalGradient(PINK) else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)), RoundedCornerShape(50))
                    .padding(horizontal = 18.dp, vertical = 9.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val ink = if (on) Color.White else t.glassInk
                    if (isCross) Text("✕", color = ink, fontSize = 15.sp) else Box(Modifier.size(12.dp).background(ink, RoundedCornerShape(3.dp)))
                    Text(label, style = displayStyle(16.sp), color = ink)
                }
            }
        }
    }
}

@Composable
fun NonogramGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val mode = LocalPlayMode.current
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val puzzle = remember(level) { newPuzzle(level, Random(mode.seedFor(level))) }
    val n = puzzle.n
    val slot = remember(level) { LevelSave(store, mode, level) }
    val saved = remember(level) { slot.load { decodeGame(it, n) } }
    val cellsState = remember(level) { mutableStateOf(saved?.first ?: IntArray(n * n)) }
    var cells by cellsState
    val crossState = remember(level) { mutableStateOf(false) }
    val history = remember(level) { mutableStateListOf<IntArray>() }
    var seconds by remember(level) { mutableIntStateOf(saved?.second ?: 0) }
    val won = isSolved(puzzle, cells)
    LaunchedEffect(level, won) { while (!won) { delay(1000); seconds++ } }
    LaunchedEffect(cells, won) { if (won) slot.clear() else slot.save(encodeGame(cells, seconds)) }

    // One stroke paints every square it crosses the same way: the first square decides fill, mark or clear.
    val target = remember(level) { intArrayOf(FILL) }
    val onPaint: (Int, Boolean) -> Unit = remember(level) {
        { i, start ->
            val now = cellsState.value
            if (!isSolved(puzzle, now)) {
                if (start) {
                    val want = if (crossState.value) CROSS else FILL
                    target[0] = if (now[i] == want) EMPTY else want
                    history.add(now)
                    if (history.size > 60) history.removeAt(0)
                }
                if (now[i] != target[0]) {
                    val next = now.copyOf()
                    next[i] = target[0]
                    cellsState.value = next
                    val r = i / n
                    val c = i % n
                    val lineDone = rowRuns(puzzle, next, r) == puzzle.rows[r] || colRuns(puzzle, next, c) == puzzle.cols[c]
                    if (target[0] == FILL && lineDone) feedback.good() else feedback.tap()
                }
            }
        }
    }

    GameScaffold(
        ads, "NONOGRAM",
        pills = listOf(Pill(mode.label(level)), Pill("${n}×$n", GameIcons.Grid), Pill(time(seconds), GameIcons.Clock)),
        tools = listOf(
            Tool("Undo", GameIcons.Undo, enabled = history.isNotEmpty()) { cells = history.removeAt(history.lastIndex) },
            Tool("Restart", GameIcons.Restart, enabled = cells.any { it != EMPTY }) { history.add(cells); cells = IntArray(n * n) },
        ),
        hint = Hint("Hint", GameIcons.Bulb, levelKey = mode.hintKey(level)) { history.add(cells); cells = hinted(puzzle, cells); feedback.good() },
    ) {
        Bezel(Modifier.fillMaxWidth().aspectRatio(1f), padding = 6.dp) {
            BoxWithConstraints(Modifier.fillMaxSize()) { NonoBoard(puzzle, maxWidth, cells, onPaint = onPaint) }
        }
        Spacer(Modifier.height(16.dp))
        ModeSwitch(crossState.value) { crossState.value = it; feedback.tap() }
    }

    if (won) {
        val stars = when { seconds < 3 * n * n -> 3; seconds < 6 * n * n -> 2; else -> 1 }
        ResultDialog("Picture revealed!", "Level: $level\nTime: ${time(seconds)}", if (mode.daily) "Done" else "Next level", {
            ads.levelDone()
            mode.win(store, level, stars) {
                level++
                store.put("level", level)
            }
        }, stars = stars)
    }
}

fun time(s: Int) = "%d:%02d".format(s / 60, s % 60)
