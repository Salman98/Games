package com.techeliteclub.game.sunmoon

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
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

val CELL = Color(0xFF2A2170)
val CELL_GIVEN = Color(0xFF3B3192)
private val CELL_BAD = Color(0xFF8E2A52)
private val GOLD = Color(0xFFFFC533)

/** A glowing sun (gold disc with rays) or a moon (pale crescent with a soft halo). */
@Composable
fun Symbol(v: Int, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val r = this.size.minDimension / 2
        if (v == SUN) {
            drawCircle(Brush.radialGradient(listOf(GOLD.copy(alpha = 0.5f), Color.Transparent), center, r * 1.15f), r * 1.15f)
            for (k in 0 until 8) rotate(k * 45f) {
                drawLine(GOLD, center + Offset(0f, -r * 0.72f), center + Offset(0f, -r * 0.96f), r * 0.14f, StrokeCap.Round)
            }
            drawCircle(Brush.verticalGradient(listOf(Color(0xFFFFF6C4), Color(0xFFFFB81E))), r * 0.56f)
            drawCircle(Color.White.copy(alpha = 0.55f), r * 0.14f, center + Offset(-r * 0.2f, -r * 0.22f))
        } else if (v == MOON) {
            drawCircle(Brush.radialGradient(listOf(Color(0x66BFD3FF), Color.Transparent), center, r * 1.1f), r * 1.1f)
            val disc = Path().apply { addOval(Rect(center, r * 0.78f)) }
            val bite = Path().apply { addOval(Rect(center + Offset(r * 0.36f, -r * 0.22f), r * 0.62f)) }
            drawPath(Path.combine(PathOperation.Difference, disc, bite), Brush.verticalGradient(listOf(Color.White, Color(0xFF9DB8F5))))
        }
    }
}

@Composable
fun SunMoonGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val mode = LocalPlayMode.current
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val puzzle = remember(level) { newPuzzle(level, Random(mode.seedFor(level))) }
    val n = puzzle.n
    val slot = remember(level) { LevelSave(store, mode, level) }
    val saved = remember(level) { slot.load { decodeGame(it, puzzle) } }
    var grid by remember(level) { mutableStateOf(saved?.first ?: puzzle.givens.copyOf()) }
    var history by remember(level) { mutableStateOf(listOf<IntArray>()) }
    var seconds by remember(level) { mutableIntStateOf(saved?.second ?: 0) }
    val bad = violations(n, grid, puzzle.links)
    val won = isSolved(puzzle, grid)
    LaunchedEffect(level, won) { while (!won) { delay(1000); seconds++ } }
    // shortcut: the clock is saved with each move, not each second; save on pause if players notice lost time.
    LaunchedEffect(grid, won) { if (won) slot.clear() else slot.save(encodeGame(grid, seconds)) }

    fun change(next: IntArray) { history = (history + listOf(grid)).takeLast(100); grid = next }

    fun tap(i: Int) {
        if (puzzle.givens[i] != EMPTY) { feedback.bad(); return }
        val next = grid.copyOf().also { it[i] = (it[i] + 1) % 3 }
        change(next)
        when {
            next[i] == EMPTY -> feedback.tap()
            i in violations(n, next, puzzle.links) -> feedback.bad()
            else -> feedback.good()
        }
    }

    fun hint() { // fill one square the player has empty or wrong
        val i = grid.indices.firstOrNull { grid[it] != puzzle.solution[it] } ?: return
        change(grid.copyOf().also { it[i] = puzzle.solution[i] })
        feedback.good()
    }

    GameScaffold(
        ads, "SUN & MOON",
        pills = listOf(Pill(mode.label(level)), Pill(time(seconds), GameIcons.Clock)),
        tools = listOf(
            Tool("Undo", GameIcons.Undo, enabled = history.isNotEmpty()) { grid = history.last(); history = history.dropLast(1) },
            Tool("Restart", GameIcons.Restart) { change(puzzle.givens.copyOf()) },
        ),
        hint = Hint("Hint", GameIcons.Bulb, levelKey = mode.hintKey(level), onHint = ::hint),
    ) {
        Bezel(Modifier.fillMaxWidth().aspectRatio(1f), padding = 5.dp) {
            BoxWithConstraints(Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))) {
                val cell = maxWidth / n
                Column {
                    for (r in 0 until n) Row {
                        for (c in 0 until n) {
                            val i = r * n + c
                            val given = puzzle.givens[i] != EMPTY
                            val bg = when { i in bad -> CELL_BAD; given -> CELL_GIVEN; else -> CELL }
                            val tile = RoundedCornerShape(12.dp)
                            Box(
                                // Raised glossy tiles; fixed squares sit flat with a gold edge so they read as locked.
                                Modifier.size(cell).padding(2.5.dp).shadow(if (given) 0.dp else 4.dp, tile).background(bg, tile)
                                    .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.16f), Color.Transparent)), tile)
                                    .border(1.dp, if (given) GOLD.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.1f), tile)
                                    .clip(tile).clickable { tap(i) }
                                    .semantics {
                                        contentDescription = "Row ${r + 1} column ${c + 1}, " + listOf("empty", "sun", "moon")[grid[i]] + if (given) ", fixed" else ""
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                // Each new sun or moon pops in.
                                if (grid[i] != EMPTY) key(grid[i]) {
                                    val pop = remember { Animatable(0.5f) }
                                    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 900f)) }
                                    Symbol(grid[i], cell * 0.66f, Modifier.scale(pop.value))
                                }
                            }
                        }
                    }
                }
                // Signs sit on the line between two squares: = same, × different.
                val badge = cell * 0.34f
                for (l in puzzle.links) {
                    val across = l.b == l.a + 1
                    val x = if (across) cell * (l.a % n + 1) - badge / 2 else cell * (l.a % n) + cell / 2 - badge / 2
                    val y = if (across) cell * (l.a / n) + cell / 2 - badge / 2 else cell * (l.a / n + 1) - badge / 2
                    Box(
                        Modifier.offset(x, y).size(badge).background(Color(0xFF140E3C), CircleShape).border(1.dp, GOLD.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text(if (l.same) "=" else "×", style = displayStyle((badge.value * 0.7f).sp), color = GOLD) }
                }
            }
        }
    }

    if (won) {
        val stars = when { seconds < 20 * n -> 3; seconds < 45 * n -> 2; else -> 1 }
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
