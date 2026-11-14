package com.techeliteclub.game.numberpath

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import com.techeliteclub.game.core.LocalPlayMode
import com.techeliteclub.game.core.Pill
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.Tool
import com.techeliteclub.game.core.displayStyle
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlin.random.Random

private val BOARD = Color(0xFF2A0920)
private val TILE = Color(0xFF55193F)
private val VISITED = Color(0xFF8A3268)
private val ROUTE = listOf(Color(0xFFFFE98A), Color(0xFFFFB02E), Color(0xFFFF7A3A))
private val GOLD = Color(0xFFFFC233)
private val NODE = Color(0xFF1C0714)

/**
 * The board: squares, the gold route through [path] and the numbered stops.
 * [onTouch] gets the square under the finger for taps and drags; null makes the board a picture.
 */
@Composable
fun PathBoard(n: Int, size: Dp, numbers: IntArray, path: List<Int>, onTouch: ((Int) -> Unit)? = null) {
    val cell = size / n
    val pulse by rememberInfiniteTransition(label = "head").animateFloat(0.8f, 1.2f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "pulse")
    fun cellAt(o: Offset, width: Int): Int? {
        val s = width.toFloat() / n
        val r = (o.y / s).toInt()
        val c = (o.x / s).toInt()
        return if (o.x >= 0 && o.y >= 0 && r < n && c < n) r * n + c else null
    }
    Box(
        Modifier.size(size).clip(RoundedCornerShape(14.dp)).background(BOARD)
            .then(
                if (onTouch == null) Modifier else Modifier
                    .pointerInput(onTouch) {
                        detectDragGestures(onDragStart = { o -> cellAt(o, this.size.width)?.let(onTouch) }) { change, _ ->
                            change.consume()
                            cellAt(change.position, this.size.width)?.let(onTouch)
                        }
                    }
                    .pointerInput(onTouch) { detectTapGestures { o -> cellAt(o, this.size.width)?.let(onTouch) } },
            )
            .semantics { contentDescription = "Board, ${path.size} of ${n * n} squares on the route" },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val s = this.size.width / n
            fun centre(i: Int) = Offset((i % n + 0.5f) * s, (i / n + 0.5f) * s)
            val gap = 2.dp.toPx()
            val onRoute = path.toSet()
            for (i in 0 until n * n) {
                drawRoundRect(if (i in onRoute) VISITED else TILE, Offset((i % n) * s + gap, (i / n) * s + gap), Size(s - 2 * gap, s - 2 * gap), CornerRadius(s * 0.16f))
            }
            // The route: a soft glow, then a gold-to-orange ribbon that runs across the whole board.
            val ribbon = Brush.linearGradient(ROUTE, Offset.Zero, Offset(this.size.width, this.size.height))
            for (k in 1 until path.size) drawLine(GOLD.copy(alpha = 0.25f), centre(path[k - 1]), centre(path[k]), s * 0.62f, StrokeCap.Round)
            for (k in 1 until path.size) drawLine(ribbon, centre(path[k - 1]), centre(path[k]), s * 0.4f, StrokeCap.Round)
            if (path.isNotEmpty()) { // pulsing head shows where to drag from
                drawCircle(GOLD.copy(alpha = 0.35f), s * 0.3f * pulse, centre(path.last()))
                drawCircle(Color.White, s * 0.12f, centre(path.last()))
            }
        }
        for (i in numbers.indices) if (numbers[i] != 0) {
            val on = i in path
            val pop by animateFloatAsState(if (on) 1.12f else 1f, spring(dampingRatio = 0.4f, stiffness = 700f), label = "stop")
            Box(Modifier.offset(cell * (i % n), cell * (i / n)).size(cell), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(cell * 0.6f).scale(pop).shadow(6.dp, CircleShape)
                        .background(if (on) Brush.verticalGradient(listOf(Color.White, Color(0xFFFFE9B0))) else Brush.verticalGradient(listOf(Color(0xFF3A1230), NODE)), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text("${numbers[i]}", style = displayStyle((cell.value * 0.34f).sp), color = if (on) NODE else Color.White) }
            }
        }
    }
}

@Composable
fun NumberPathGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val mode = LocalPlayMode.current
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val puzzle = remember(level) { newPuzzle(level, Random(mode.seedFor(level))) }
    val n = puzzle.n
    val slot = remember(level) { LevelSave(store, mode, level) }
    val saved = remember(level) { slot.load { decodeGame(it, puzzle) } }
    val pathState = remember(level) { mutableStateOf(saved?.first ?: start(puzzle)) }
    var path by pathState
    var seconds by remember(level) { mutableIntStateOf(saved?.second ?: 0) }
    val won = isSolved(puzzle, path)
    LaunchedEffect(level, won) { while (!won) { delay(1000); seconds++ } }
    // shortcut: the clock is saved with each move, not each second; save on pause if players notice lost time.
    LaunchedEffect(path, won) { if (won) slot.clear() else slot.save(encodeGame(path, seconds)) }

    // Kept for the whole level because the drag handler holds on to it; it reads the route from pathState, never a stale copy.
    val onTouch: (Int) -> Unit = remember(level) {
        { x ->
            val now = pathState.value
            val next = touch(puzzle, now, x)
            if (next != now && !isSolved(puzzle, now)) {
                if (next.size > now.size && puzzle.numbers[x] != 0) feedback.good() else feedback.tap()
                pathState.value = next
            }
        }
    }

    GameScaffold(
        ads, "NUMBER PATH",
        pills = listOf(Pill(mode.label(level)), Pill("${path.size}/${n * n}", GameIcons.Grid), Pill(time(seconds), GameIcons.Clock)),
        tools = listOf(
            Tool("Undo", GameIcons.Undo, enabled = path.size > 1) { path = path.dropLast(1) },
            Tool("Restart", GameIcons.Restart, enabled = path.size > 1) { path = start(puzzle) },
        ),
        hint = Hint("Hint", GameIcons.Bulb, levelKey = mode.hintKey(level)) { path = hinted(puzzle, path); feedback.good() },
    ) {
        Bezel(Modifier.fillMaxWidth().aspectRatio(1f), padding = 5.dp) {
            BoxWithConstraints(Modifier.fillMaxSize()) { PathBoard(n, maxWidth, puzzle.numbers, path, onTouch) }
        }
    }

    if (won) {
        val stars = when { seconds < 4 * n * n -> 3; seconds < 8 * n * n -> 2; else -> 1 }
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
