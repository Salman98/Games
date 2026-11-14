package com.techeliteclub.game.rectangles

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
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
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlin.random.Random

private val PASTELS = listOf(
    0xFFFF9F6B, 0xFF4FD1C0, 0xFFFFCF4A, 0xFF8FA7FF, 0xFFFF7FA6, 0xFF7BD88A, 0xFFC48BFF, 0xFF5EC3FF, 0xFFFFB35C, 0xFF9AD15A,
).map { Color(it) }
private val SQUARE = Color(0xFFFBF3E6)
private val SQUARE_EDGE = Color(0xFFF0DCC0)
private val BROWN = Color(0xFF5A2E05)
private val ORANGE = Color(0xFFE8650C)

/** Colour of a rectangle: fixed by where it is, so it keeps its colour while others change. */
private fun colorOf(r: Rect) = PASTELS[((r.r * 31 + r.c * 17 + r.h * 7 + r.w) and 0x7fffffff) % PASTELS.size]

/**
 * Squares, the player's rectangles (full colour when right, pale when wrong), a dashed preview and the clue badges.
 * [onTap] / [onDrag] are null on picture boards. [onDrag] gets (start square, end square, finished).
 */
@Composable
fun RectBoard(p: Puzzle, size: Dp, rects: List<Rect>, preview: Rect? = null, onTap: ((Int) -> Unit)? = null, onDrag: ((Int, Int, Boolean) -> Unit)? = null) {
    val n = p.n
    val cell = size / n
    val input = if (onTap == null || onDrag == null) Modifier else Modifier
        .pointerInput(onDrag) {
            fun at(o: Offset): Int {
                val s = this.size.width.toFloat() / n
                return (o.y / s).toInt().coerceIn(0, n - 1) * n + (o.x / s).toInt().coerceIn(0, n - 1)
            }
            var start = 0
            var end = 0
            detectDragGestures(
                onDragStart = { o -> start = at(o); end = start; onDrag(start, end, false) },
                onDragEnd = { onDrag(start, end, true) },
                onDragCancel = { onDrag(start, start, true) },
            ) { change, _ ->
                change.consume()
                val now = at(change.position)
                if (now != end) { end = now; onDrag(start, end, false) }
            }
        }
        .pointerInput(onTap) {
            detectTapGestures { o ->
                val s = this.size.width.toFloat() / n
                onTap((o.y / s).toInt().coerceIn(0, n - 1) * n + (o.x / s).toInt().coerceIn(0, n - 1))
            }
        }
    Box(Modifier.size(size).then(input).semantics { contentDescription = "Rectangles board, ${rects.size} rectangles drawn" }) {
        Canvas(Modifier.fillMaxSize()) {
            val s = this.size.width / n
            val gap = s * 0.05f
            for (i in 0 until n * n) {
                val o = Offset((i % n) * s + gap, (i / n) * s + gap)
                drawRoundRect(SQUARE, o, Size(s - 2 * gap, s - 2 * gap), CornerRadius(s * 0.14f))
                drawRoundRect(SQUARE_EDGE, o, Size(s - 2 * gap, s - 2 * gap), CornerRadius(s * 0.14f), style = Stroke(1.dp.toPx()))
            }
            for (r in rects) {
                val o = Offset(r.c * s + gap * 1.5f, r.r * s + gap * 1.5f)
                val sz = Size(r.w * s - gap * 3, r.h * s - gap * 3)
                val good = r.goodFor(p)
                val col = colorOf(r)
                drawRoundRect(if (good) col else col.copy(alpha = 0.35f), o, sz, CornerRadius(s * 0.2f))
                if (good) drawRoundRect(Color.White.copy(alpha = 0.3f), o, Size(sz.width, minOf(sz.height * 0.45f, s * 0.45f)), CornerRadius(s * 0.2f))
                else drawRoundRect(col, o, sz, CornerRadius(s * 0.2f), style = Stroke(2.dp.toPx()))
            }
            if (preview != null) {
                val o = Offset(preview.c * s + gap * 1.5f, preview.r * s + gap * 1.5f)
                val sz = Size(preview.w * s - gap * 3, preview.h * s - gap * 3)
                drawRoundRect(ORANGE.copy(alpha = 0.16f), o, sz, CornerRadius(s * 0.2f))
                drawRoundRect(ORANGE, o, sz, CornerRadius(s * 0.2f), style = Stroke(3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))))
            }
        }
        for ((i, v) in p.clues) {
            val done = rects.any { (i / n to i % n) in it && it.goodFor(p) }
            Box(Modifier.offset(cell * (i % n), cell * (i / n)).size(cell), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(cell * 0.62f).shadow(4.dp, CircleShape)
                        .background(if (done) Brush.verticalGradient(listOf(Color.White, Color.White)) else Brush.verticalGradient(listOf(Color(0xFF7A4612), Color(0xFF4D2A08))), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text("$v", style = displayStyle((cell.value * 0.34f).sp), color = if (done) BROWN else Color.White) }
            }
        }
    }
}

@Composable
fun RectanglesGame(ads: Ads) {
    val t = LocalGameTheme.current
    val store = rememberStore()
    val feedback = rememberFeedback()
    val mode = LocalPlayMode.current
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val puzzle = remember(level) { newPuzzle(level, Random(mode.seedFor(level))) }
    val n = puzzle.n
    val slot = remember(level) { LevelSave(store, mode, level) }
    val saved = remember(level) { slot.load { decodeGame(it, n) } }
    val rectsState = remember(level) { mutableStateOf(saved?.first ?: emptyList()) }
    var rects by rectsState
    var preview by remember(level) { mutableStateOf<Rect?>(null) }
    val history = remember(level) { mutableStateListOf<List<Rect>>() }
    var seconds by remember(level) { mutableIntStateOf(saved?.second ?: 0) }
    val won = isSolved(puzzle, rects)
    LaunchedEffect(level, won) { while (!won) { delay(1000); seconds++ } }
    LaunchedEffect(rects, won) { if (won) slot.clear() else slot.save(encodeGame(rects, seconds)) }

    fun commit(next: List<Rect>) {
        history.add(rectsState.value)
        rectsState.value = next
    }
    val onDrag: (Int, Int, Boolean) -> Unit = remember(level) {
        { a, b, done ->
            val rect = Rect.spanning(n, a, b)
            if (!done) preview = rect
            else {
                preview = null
                if (a != b && !isSolved(puzzle, rectsState.value)) {
                    commit(draw(rectsState.value, rect))
                    if (rect.goodFor(puzzle)) feedback.good() else feedback.tap()
                }
            }
        }
    }
    val onTap: (Int) -> Unit = remember(level) {
        { i ->
            val now = rectsState.value
            if (!isSolved(puzzle, now)) {
                val hit = now.firstOrNull { (i / n to i % n) in it }
                if (hit != null) { commit(now - hit); feedback.tap() }
                else {
                    val one = Rect(i / n, i % n, 1, 1)
                    commit(draw(now, one))
                    if (one.goodFor(puzzle)) feedback.good() else feedback.tap()
                }
            }
        }
    }

    GameScaffold(
        ads, "RECTANGLES",
        pills = listOf(Pill(mode.label(level)), Pill("${rects.count { it.goodFor(puzzle) }}/${puzzle.clues.size}", GameIcons.Grid), Pill(time(seconds), GameIcons.Clock)),
        tools = listOf(
            Tool("Undo", GameIcons.Undo, enabled = history.isNotEmpty()) { rects = history.removeAt(history.lastIndex) },
            Tool("Restart", GameIcons.Restart, enabled = rects.isNotEmpty()) { commit(emptyList()) },
        ),
        hint = Hint("Hint", GameIcons.Bulb, levelKey = mode.hintKey(level)) { commit(hinted(puzzle, rects)); feedback.good() },
    ) {
        Bezel(Modifier.fillMaxWidth().aspectRatio(1f), padding = 5.dp) {
            BoxWithConstraints(Modifier.fillMaxSize()) { RectBoard(puzzle, maxWidth, rects, preview, onTap, onDrag) }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Drag across squares. Each rectangle holds one number: its size.",
            color = t.glassInk, fontSize = 14.sp, textAlign = TextAlign.Center,
            modifier = Modifier.glass(t, RoundedCornerShape(50)).padding(horizontal = 18.dp, vertical = 9.dp),
        )
    }

    if (won) {
        val stars = when { seconds < 2 * n * n -> 3; seconds < 4 * n * n -> 2; else -> 1 }
        ResultDialog("Solved!", "Level: $level\nTime: ${time(seconds)}", if (mode.daily) "Done" else "Next level", {
            ads.levelDone()
            mode.win(store, level, stars) {
                level++
                store.put("level", level)
            }
        }, stars = stars)
    }
}

fun time(s: Int) = "%d:%02d".format(s / 60, s % 60)
