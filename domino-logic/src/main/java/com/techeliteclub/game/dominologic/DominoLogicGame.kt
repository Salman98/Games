package com.techeliteclub.game.dominologic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.techeliteclub.game.core.pressable
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlin.random.Random

private val REGION = listOf(0xFFFF8A5C, 0xFF4FD1C0, 0xFFFFCF4A, 0xFF8FA7FF, 0xFFFF7FA6, 0xFF7BD88A, 0xFFC48BFF, 0xFF5EC3FF).map { Color(it) }
private val PIP = Color(0xFF2A0E02)
private val BONE = listOf(Color.White, Color(0xFFF1E6D6))
private val BONE_EDGE = Color(0xFFD8C6AC)
private val GOLD = Color(0xFFFFC533)

/** Where the pips sit on a face, as fractions of its size. */
private val PIPS = listOf(
    emptyList(), listOf(.5f to .5f), listOf(.28f to .28f, .72f to .72f), listOf(.26f to .26f, .5f to .5f, .74f to .74f),
    listOf(.28f to .28f, .72f to .28f, .28f to .72f, .72f to .72f), listOf(.26f to .26f, .74f to .26f, .5f to .5f, .26f to .74f, .74f to .74f),
    listOf(.28f to .24f, .72f to .24f, .28f to .5f, .72f to .5f, .28f to .76f, .72f to .76f),
)

@Composable
private fun Face(value: Int, size: Dp) {
    Canvas(Modifier.size(size)) { for ((x, y) in PIPS[value]) drawCircle(PIP, this.size.width * 0.085f, Offset(this.size.width * x, this.size.height * y)) }
}

/** A domino: two faces of [half] size with a line between. */
@Composable
fun Domino(a: Int, b: Int, half: Dp, vertical: Boolean = false, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(half * 0.22f)
    val body = modifier.shadow(5.dp, shape).background(BONE_EDGE, shape).padding(bottom = 3.dp).background(Brush.verticalGradient(BONE), shape)
    val line = @Composable { Box(Modifier.background(BONE_EDGE).then(if (vertical) Modifier.size(half * 0.7f, 2.dp) else Modifier.size(2.dp, half * 0.7f))) }
    if (vertical) Column(body, horizontalAlignment = Alignment.CenterHorizontally) { Face(a, half); line(); Face(b, half) }
    else Row(body, verticalAlignment = Alignment.CenterVertically) { Face(a, half); line(); Face(b, half) }
}

/**
 * Regions (coloured squares with a rule badge), the dominoes on them and the square picked as a domino's first half.
 * [onTap] is null on picture boards.
 */
@Composable
fun DominoBoard(p: Puzzle, width: Dp, placed: List<Placed>, pending: Int? = null, onTap: ((Int) -> Unit)? = null) {
    val cell = width / p.cols
    val pips = pipsOf(p, placed)
    Box(Modifier.size(width, cell * p.rows).semantics { contentDescription = "Domino board, ${placed.size} of ${p.tiles.size} placed" }) {
        for (i in 0 until p.rows * p.cols) {
            val col = REGION[p.regionOf[i] % REGION.size]
            Box(
                Modifier.offset(cell * (i % p.cols), cell * (i / p.cols)).size(cell).padding(2.5.dp)
                    .background(col.copy(alpha = 0.34f), RoundedCornerShape(10.dp))
                    .border(if (i == pending) 3.dp else 2.dp, if (i == pending) GOLD else col, RoundedCornerShape(10.dp))
                    .then(if (onTap != null) Modifier.pressable(label = "Square ${i + 1}") { onTap(i) } else Modifier),
            )
        }
        for (s in placed) {
            val vertical = s.a % p.cols == s.b % p.cols
            val first = minOf(s.a, s.b)
            val (x, y) = if (s.a == first) p.tiles[s.id] else p.tiles[s.id].let { it.second to it.first }
            Domino(
                x, y, cell - 9.dp, vertical,
                Modifier.offset(cell * (first % p.cols) + 3.5.dp, cell * (first / p.cols) + 3.5.dp)
                    .then(if (onTap != null) Modifier.pressable(label = "Lift domino") { onTap(s.a) } else Modifier),
            )
        }
        p.regions.forEachIndexed { k, r ->
            if (r.rule != Rule.NONE) {
                val at = r.cells.min()
                val done = r.ok { pips[it] }
                val pop by animateFloatAsState(if (done) 1.12f else 1f, spring(dampingRatio = 0.4f, stiffness = 700f), label = "badge")
                Box(
                    Modifier.offset(cell * (at % p.cols) - 4.dp, cell * (at / p.cols) - 4.dp).size(cell * 0.42f).scale(pop).shadow(3.dp, CircleShape)
                        .background(if (done) Color(0xFF3CCB7F) else REGION[k % REGION.size], CircleShape).border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text(r.label, style = displayStyle((cell.value * 0.2f).sp), color = if (done) Color.White else PIP) }
            }
        }
    }
}

@Composable
fun DominoLogicGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val mode = LocalPlayMode.current
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val puzzle = remember(level) { newPuzzle(level, Random(mode.seedFor(level))) }
    val slot = remember(level) { LevelSave(store, mode, level) }
    val saved = remember(level) { slot.load { decodeGame(it, puzzle) } }
    var placed by remember(level) { mutableStateOf(saved?.first ?: emptyList()) }
    var seconds by remember(level) { mutableIntStateOf(saved?.second ?: 0) }
    var picked by remember(level) { mutableStateOf<Int?>(null) } // tile chosen in the tray
    var flipped by remember(level) { mutableStateOf(false) }
    var pending by remember(level) { mutableStateOf<Int?>(null) } // square chosen for the first half
    val history = remember(level) { mutableStateListOf<List<Placed>>() }
    val won = isSolved(puzzle, placed)
    LaunchedEffect(level, won) { while (!won) { delay(1000); seconds++ } }
    LaunchedEffect(placed, won) { if (won) slot.clear() else slot.save(encodeGame(placed, seconds)) }

    val free = puzzle.tiles.indices.filter { id -> placed.none { it.id == id } }
    val tile = picked?.takeIf { it in free } ?: free.firstOrNull()
    fun commit(next: List<Placed>) { history.add(placed); placed = next; pending = null }
    fun tap(i: Int) {
        if (won) return
        val on = placed.firstOrNull { it.a == i || it.b == i }
        val first = pending
        when {
            on != null -> { commit(placed - on); picked = on.id; flipped = false; feedback.tap() }
            tile == null -> Unit
            first == null || !adjacent(puzzle.cols, first, i) -> { pending = i; feedback.tap() }
            else -> place(puzzle, placed, tile, first, i, flipped)?.let {
                commit(it); picked = null; flipped = false
                val pips = pipsOf(puzzle, it)
                if (puzzle.regions[puzzle.regionOf[i]].ok { c -> pips[c] } || puzzle.regions[puzzle.regionOf[first]].ok { c -> pips[c] }) feedback.good() else feedback.tap()
            }
        }
    }

    GameScaffold(
        ads, "DOMINO LOGIC",
        pills = listOf(Pill(mode.label(level)), Pill("${placed.size}/${puzzle.tiles.size}", GameIcons.Grid), Pill(time(seconds), GameIcons.Clock)),
        tools = listOf(
            Tool("Undo", GameIcons.Undo, enabled = history.isNotEmpty()) { placed = history.removeAt(history.lastIndex); pending = null },
            Tool("Rotate", GameIcons.Restart, enabled = tile != null) { flipped = !flipped },
        ),
        hint = Hint("Hint", GameIcons.Bulb, levelKey = mode.hintKey(level)) { commit(hinted(puzzle, placed)); picked = null; feedback.good() },
    ) {
        Bezel(Modifier.fillMaxWidth().aspectRatio(puzzle.cols.toFloat() / puzzle.rows), padding = 5.dp) {
            BoxWithConstraints(Modifier.fillMaxSize()) { DominoBoard(puzzle, maxWidth, placed, pending) { tap(it) } }
        }
        Spacer(Modifier.height(14.dp))
        // The tray: tap a domino to choose it, then tap two squares side by side. The first square gets the left half.
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            for (row in free.chunked(5)) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (id in row) {
                    val (a, b) = puzzle.tiles[id].let { if (id == tile && flipped) it.second to it.first else it }
                    val lift by animateFloatAsState(if (id == tile) 1.12f else 1f, spring(dampingRatio = 0.5f, stiffness = 700f), label = "tray")
                    Domino(
                        a, b, 27.dp,
                        modifier = Modifier.scale(lift)
                            .then(if (id == tile) Modifier.border(2.dp, GOLD, RoundedCornerShape(8.dp)) else Modifier)
                            .pressable(label = "Domino $a $b") { picked = id; flipped = false; feedback.tap() },
                    )
                }
            }
        }
    }

    if (won) {
        val n = puzzle.tiles.size
        val stars = when { seconds < 12 * n -> 3; seconds < 25 * n -> 2; else -> 1 }
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
