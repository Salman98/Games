package com.techeliteclub.game.bridges

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
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
import kotlin.math.abs
import kotlin.random.Random

private val WOOD = Color(0xFF8A5A2B)
private val ROPE = Color(0xFFE9B77A)
private val SAND = listOf(Color(0xFFFFF2C9), Color(0xFFF2C46A))
private val DONE = listOf(Color(0xFF9DF0A8), Color(0xFF2FBF5A))
private val OVER = listOf(Color(0xFFFFB0A8), Color(0xFFE5484D))

/**
 * Water, plank bridges and numbered islands. An island turns green with exactly its number of bridges, red with too many.
 * [selected] is the island tapped first. [onLink] gets (island, row step, column step) from a drag, [onIsland] a tapped island.
 */
@Composable
fun BridgeBoard(
    p: Puzzle, size: Dp, bridges: Map<Link, Int>, selected: Int? = null,
    onIsland: ((Int) -> Unit)? = null, onLink: ((Int, Int, Int) -> Unit)? = null,
) {
    val n = p.n
    val cell = size / n
    val input = if (onIsland == null || onLink == null) Modifier else Modifier
        .pointerInput(p) {
            fun islandAt(o: Offset): Int? {
                val s = this.size.width.toFloat() / n
                val sq = (o.y / s).toInt().coerceIn(0, n - 1) * n + (o.x / s).toInt().coerceIn(0, n - 1)
                return p.islands.indexOf(sq).takeIf { it >= 0 }
            }
            var from: Int? = null
            var moved = Offset.Zero
            detectDragGestures(
                onDragStart = { o -> from = islandAt(o); moved = Offset.Zero },
                onDragEnd = {
                    val i = from
                    // The longer axis of the drag picks the direction.
                    if (i != null && maxOf(abs(moved.x), abs(moved.y)) > this.size.width / n * 0.4f) {
                        if (abs(moved.x) > abs(moved.y)) onLink(i, 0, if (moved.x > 0) 1 else -1) else onLink(i, if (moved.y > 0) 1 else -1, 0)
                    }
                },
            ) { change, amount -> change.consume(); moved += amount }
        }
        .pointerInput(p) {
            detectTapGestures { o ->
                val s = this.size.width.toFloat() / n
                val sq = (o.y / s).toInt().coerceIn(0, n - 1) * n + (o.x / s).toInt().coerceIn(0, n - 1)
                p.islands.indexOf(sq).takeIf { it >= 0 }?.let(onIsland)
            }
        }
    Box(
        Modifier.size(size).then(input)
            .semantics { contentDescription = "Bridges board, ${p.islands.indices.count { count(bridges, it) == p.need[it] }} of ${p.islands.size} islands done" },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val s = this.size.width / n
            fun centre(sq: Int) = Offset((sq % n + 0.5f) * s, (sq / n + 0.5f) * s)
            for (i in 0 until n * n) drawCircle(Color.White.copy(alpha = 0.35f), s * 0.035f, centre(i))
            for ((l, k) in bridges) {
                val a = centre(p.islands[l.a])
                val b = centre(p.islands[l.b])
                val across = a.y == b.y
                for (shift in if (k == 1) listOf(0f) else listOf(-s * 0.11f, s * 0.11f)) {
                    val d = if (across) Offset(0f, shift) else Offset(shift, 0f)
                    drawLine(WOOD, a + d, b + d, s * 0.13f, StrokeCap.Round)
                    drawLine(ROPE, a + d, b + d, s * 0.05f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(s * 0.04f, s * 0.09f)))
                }
            }
        }
        p.islands.forEachIndexed { i, sq ->
            val have = count(bridges, i)
            val fill = when { have == p.need[i] -> DONE; have > p.need[i] -> OVER; else -> SAND }
            val pop by animateFloatAsState(if (have == p.need[i] || i == selected) 1.1f else 1f, spring(dampingRatio = 0.4f, stiffness = 700f), label = "island")
            Box(Modifier.offset(cell * (sq % n), cell * (sq / n)).size(cell), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(cell * 0.8f).scale(pop).shadow(5.dp, CircleShape).background(Brush.verticalGradient(fill), CircleShape)
                        .border(if (i == selected) 4.dp else 2.5.dp, if (i == selected) Color(0xFFFF9F1C) else Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text("${p.need[i]}", style = displayStyle((cell.value * 0.4f).sp), color = if (fill === SAND) Color(0xFF5A3300) else Color(0xFF0D3A1A)) }
            }
        }
    }
}

@Composable
fun BridgesGame(ads: Ads) {
    val t = LocalGameTheme.current
    val store = rememberStore()
    val feedback = rememberFeedback()
    val mode = LocalPlayMode.current
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val puzzle = remember(level) { newPuzzle(level, Random(mode.seedFor(level))) }
    val slot = remember(level) { LevelSave(store, mode, level) }
    val saved = remember(level) { slot.load { decodeGame(it, puzzle) } }
    val state = remember(level) { mutableStateOf(saved?.first ?: emptyMap()) }
    var bridges by state
    var selected by remember(level) { mutableStateOf<Int?>(null) }
    val history = remember(level) { mutableStateListOf<Map<Link, Int>>() }
    var seconds by remember(level) { mutableIntStateOf(saved?.second ?: 0) }
    val won = isSolved(puzzle, bridges)
    LaunchedEffect(level, won) { while (!won) { delay(1000); seconds++ } }
    LaunchedEffect(bridges, won) { if (won) slot.clear() else slot.save(encodeGame(bridges, seconds)) }

    // Kept for the level because the gesture handlers hold on to them; they read the bridges from state, never a stale copy.
    val use: (Link) -> Unit = remember(level) {
        { l ->
            val now = state.value
            if (!isSolved(puzzle, now)) {
                val next = cycle(puzzle, now, l)
                if (next == null) feedback.bad()
                else {
                    history.add(now)
                    state.value = next
                    if (count(next, l.a) == puzzle.need[l.a] || count(next, l.b) == puzzle.need[l.b]) feedback.good() else feedback.tap()
                }
            }
        }
    }
    val onLink: (Int, Int, Int) -> Unit = remember(level) { { i, dr, dc -> selected = null; neighbour(puzzle, i, dr, dc)?.let(use) } }
    val onIsland: (Int) -> Unit = remember(level) {
        { i ->
            val first = selected
            val link = first?.let { f -> puzzle.links.firstOrNull { it == Link(minOf(f, i), maxOf(f, i)) } }
            if (link != null) { use(link); selected = null } else { selected = if (first == i) null else i; feedback.tap() }
        }
    }

    val done = puzzle.islands.indices.count { count(bridges, it) == puzzle.need[it] }
    GameScaffold(
        ads, "BRIDGES",
        pills = listOf(Pill(mode.label(level)), Pill("$done/${puzzle.islands.size}", GameIcons.Grid), Pill(time(seconds), GameIcons.Clock)),
        tools = listOf(
            Tool("Undo", GameIcons.Undo, enabled = history.isNotEmpty()) { bridges = history.removeAt(history.lastIndex) },
            Tool("Restart", GameIcons.Restart, enabled = bridges.isNotEmpty()) { history.add(bridges); bridges = emptyMap() },
        ),
        hint = Hint("Hint", GameIcons.Bulb, levelKey = mode.hintKey(level)) { history.add(bridges); bridges = hinted(puzzle, bridges); feedback.good() },
    ) {
        Bezel(Modifier.fillMaxWidth().aspectRatio(1f), padding = 5.dp) {
            BoxWithConstraints(Modifier.fillMaxSize()) { BridgeBoard(puzzle, maxWidth, bridges, selected, onIsland, onLink) }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            if (done == puzzle.islands.size && !won) "Every island must be reachable from every other."
            else "Drag from an island towards another. Again for a double bridge, a third time to remove.",
            color = t.glassInk, fontSize = 14.sp, textAlign = TextAlign.Center,
            modifier = Modifier.glass(t, RoundedCornerShape(22.dp)).padding(horizontal = 18.dp, vertical = 9.dp),
        )
    }

    if (won) {
        val k = puzzle.islands.size
        val stars = when { seconds < 8 * k -> 3; seconds < 16 * k -> 2; else -> 1 }
        ResultDialog("All linked!", "Level: $level\nTime: ${time(seconds)}", if (mode.daily) "Done" else "Next level", {
            ads.levelDone()
            mode.win(store, level, stars) {
                level++
                store.put("level", level)
            }
        }, stars = stars)
    }
}

fun time(s: Int) = "%d:%02d".format(s / 60, s % 60)
