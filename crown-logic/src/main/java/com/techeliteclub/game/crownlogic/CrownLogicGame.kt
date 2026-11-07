package com.techeliteclub.game.crownlogic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawBehind
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
import com.techeliteclub.game.core.GlassPill
import com.techeliteclub.game.core.GoldIcon
import com.techeliteclub.game.core.Hint
import com.techeliteclub.game.core.LocalPlayMode
import com.techeliteclub.game.core.Pill
import com.techeliteclub.game.core.PillText
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.Tool
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlin.random.Random

const val EMPTY = 0
const val MARK = 1
const val CROWN = 2

/** Soft jewel colours for the regions (from the mockup). */
val REGION_COLORS = listOf(
    Color(0xFFF4A6B7), Color(0xFF9FCBF2), Color(0xFFA9DFB4), Color(0xFFF3D48A), Color(0xFFC9B7F2),
    Color(0xFFF6BF9A), Color(0xFF9EE6E0), Color(0xFFE8B4E8), Color(0xFFD5E39A),
)
private val NAVY = Color(0xFF10183F)

/**
 * The board: regions with thick borders between them, crowns, ✕ marks, an optional glowing hint square.
 * [region] gives the region of cell (r, c); [state] gives EMPTY / MARK / CROWN.
 */
@Composable
fun RegionBoard(
    n: Int, size: Dp, region: (Int, Int) -> Int, state: (Int, Int) -> Int,
    bad: Set<Int> = emptySet(), hint: Int? = null, onTap: ((Int, Int) -> Unit)? = null,
) {
    val cell = size / n
    Column(Modifier.size(size).clip(RoundedCornerShape(14.dp))) {
        for (r in 0 until n) Row {
            for (c in 0 until n) {
                val me = region(r, c)
                val top = r == 0 || region(r - 1, c) != me
                val bottom = r == n - 1 || region(r + 1, c) != me
                val left = c == 0 || region(r, c - 1) != me
                val right = c == n - 1 || region(r, c + 1) != me
                val i = r * n + c
                val s = state(r, c)
                val base = if (i in bad) Color(0xFFE57373) else REGION_COLORS[me % REGION_COLORS.size]
                Box(
                    Modifier.size(cell)
                        .then(if (i == hint) Modifier.shadow(14.dp, RoundedCornerShape(4.dp), ambientColor = Color(0xFFFFEC96), spotColor = Color(0xFFFFEC96)) else Modifier)
                        .background(base.copy(alpha = 0.92f))
                        .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent), endY = 60f))
                        .drawBehind {
                            val thin = 0.7.dp.toPx()
                            val thick = 3.dp.toPx()
                            fun edge(on: Boolean, a: Offset, b: Offset) =
                                drawLine(if (on) NAVY else NAVY.copy(alpha = 0.18f), a, b, if (on) thick else thin)
                            edge(top, Offset(0f, 0f), Offset(this.size.width, 0f))
                            edge(bottom, Offset(0f, this.size.height), Offset(this.size.width, this.size.height))
                            edge(left, Offset(0f, 0f), Offset(0f, this.size.height))
                            edge(right, Offset(this.size.width, 0f), Offset(this.size.width, this.size.height))
                            if (i == hint) drawRect(Color.White.copy(alpha = 0.45f))
                        }
                        .then(if (onTap != null) Modifier.clickable { onTap(r, c) } else Modifier)
                        .semantics { contentDescription = "Row ${r + 1} column ${c + 1}, " + listOf("empty", "marked", "crown")[s] },
                    contentAlignment = Alignment.Center,
                ) {
                    when (s) {
                        MARK -> Icon(GameIcons.Close, null, tint = NAVY.copy(alpha = 0.5f), modifier = Modifier.size(cell * 0.32f))
                        CROWN -> GoldIcon(GameIcons.CrownGold, cell * 0.72f)
                    }
                }
            }
        }
    }
}

@Composable
fun CrownLogicGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val mode = LocalPlayMode.current
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val puzzle = remember(level) { newPuzzle(level, Random(mode.seedFor(level))) }
    val n = puzzle.n
    var cells by remember(level) { mutableStateOf(IntArray(n * n)) }
    var history by remember(level) { mutableStateOf(listOf<IntArray>()) }
    var hintCell by remember(level) { mutableStateOf<Int?>(null) }
    val crowns = cells.indices.filter { cells[it] == CROWN }
    val bad = conflicts(puzzle, crowns)
    val won = isSolved(puzzle, crowns)
    var seconds by remember(level) { mutableIntStateOf(0) }
    LaunchedEffect(level, won) { while (!won) { delay(1000); seconds++ } }

    fun change(next: IntArray) { history = (history + listOf(cells)).takeLast(100); cells = next }

    fun tap(r: Int, c: Int) {
        val i = r * n + c
        change(cells.copyOf().also { it[i] = (it[i] + 1) % 3 })
        if (hintCell == i && cells[i] == CROWN) hintCell = null
        val nowCrowns = cells.indices.filter { cells[it] == CROWN }
        when {
            cells[i] == CROWN && i in conflicts(puzzle, nowCrowns) -> feedback.bad()
            cells[i] == CROWN -> feedback.good()
            else -> feedback.tap()
        }
    }

    fun hint() { // light up the square of one crown the player is missing
        val r = (0 until n).firstOrNull { cells[it * n + puzzle.solution[it]] != CROWN } ?: return
        hintCell = r * n + puzzle.solution[r]
    }

    GameScaffold(
        ads, "CROWN LOGIC",
        pills = listOf(Pill(mode.label(level)), Pill(time(seconds), GameIcons.Clock)),
        tools = listOf(
            Tool("Undo", GameIcons.Undo, enabled = history.isNotEmpty()) { cells = history.last(); history = history.dropLast(1) },
            Tool("Clear ✕", GameIcons.Erase) { change(IntArray(n * n) { if (cells[it] == MARK) EMPTY else cells[it] }) },
            Tool("Restart", GameIcons.Restart) { change(IntArray(n * n)); hintCell = null; seconds = 0 },
        ),
        hint = Hint("Hint", GameIcons.Bulb, levelKey = mode.hintKey(level), onHint = ::hint),
    ) {
        Bezel(Modifier.fillMaxWidth().aspectRatio(1f), padding = 5.dp) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                RegionBoard(n, maxWidth, { r, c -> puzzle.regions[r * n + c] }, { r, c -> cells[r * n + c] }, bad, hintCell, ::tap)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 0 until n) GoldIcon(if (i < crowns.size - bad.size) GameIcons.CrownGold else GameIcons.CrownEmpty, 26.dp)
        }
        if (hintCell != null) {
            Spacer(Modifier.height(12.dp))
            GlassPill {
                Icon(GameIcons.Bulb, null, tint = Color(0xFFFFD34D), modifier = Modifier.size(18.dp))
                PillText("A crown goes in the glowing square")
            }
        }
    }

    if (won) {
        val stars = when { seconds < 60 * n / 2 -> 3; seconds < 60 * n -> 2; else -> 1 }
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
