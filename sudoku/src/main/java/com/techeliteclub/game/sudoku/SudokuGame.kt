package com.techeliteclub.game.sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlin.random.Random

private val INK = Color(0xFF1C2357)
private val BLUE = Color(0xFF3550E0)
private val RED = Color(0xFFE5383B)

private class Step(val grid: IntArray, val notes: Map<Int, Set<Int>>)

@Composable
fun SudokuGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val mode = LocalPlayMode.current
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val puzzle = remember(level) { newPuzzle(level, Random(mode.seedFor(level))) }
    var grid by remember(level) { mutableStateOf(puzzle.givens.copyOf()) }
    var notes by remember(level) { mutableStateOf(mapOf<Int, Set<Int>>()) }
    var history by remember(level) { mutableStateOf(listOf<Step>()) }
    var noteMode by remember(level) { mutableStateOf(false) }
    var selected by remember(level) { mutableStateOf<Int?>(null) }
    var mistakes by remember(level) { mutableIntStateOf(0) }
    var seconds by remember(level) { mutableIntStateOf(0) }
    val won = isSolved(grid, puzzle.solution)
    LaunchedEffect(level, won) { while (!won) { delay(1000); seconds++ } }

    // How many of each digit are correctly placed (to fade finished digits).
    val placed = IntArray(10).also { counts -> for (i in 0 until 81) if (grid[i] != 0 && grid[i] == puzzle.solution[i]) counts[grid[i]]++ }

    fun save() { history = (history + Step(grid, notes)).takeLast(200) }

    fun put(i: Int, d: Int) {
        if (puzzle.givens[i] != 0) { feedback.bad(); return }
        save()
        if (noteMode && d != 0) {
            val cur = notes[i].orEmpty()
            notes = notes + (i to if (d in cur) cur - d else cur + d)
            feedback.tap()
            return
        }
        grid = grid.copyOf().also { it[i] = d }
        notes = notes - i
        when {
            d == 0 -> feedback.tap()
            d == puzzle.solution[i] -> feedback.good()
            else -> { mistakes++; feedback.bad() }
        }
    }

    fun hint() {
        val i = selected?.takeIf { grid[it] != puzzle.solution[it] }
            ?: (0 until 81).firstOrNull { grid[it] != puzzle.solution[it] } ?: return
        selected = i
        val keepNotes = noteMode
        noteMode = false
        put(i, puzzle.solution[i])
        noteMode = keepNotes
    }

    GameScaffold(
        ads, "SUDOKU",
        pills = listOf(Pill(mode.label(level)), Pill("✕ $mistakes"), Pill(time(seconds), GameIcons.Clock)),
        tools = listOf(
            Tool("Undo", GameIcons.Undo, enabled = history.isNotEmpty()) {
                val last = history.last(); grid = last.grid; notes = last.notes; history = history.dropLast(1)
            },
            Tool("Erase", GameIcons.Erase) { selected?.let { put(it, 0) } },
            Tool(if (noteMode) "Notes ON" else "Notes", GameIcons.Pencil) { noteMode = !noteMode },
        ),
        hint = Hint("Hint", GameIcons.Bulb, levelKey = mode.hintKey(level), onHint = ::hint),
    ) {
        Bezel(Modifier.fillMaxWidth().aspectRatio(1f), padding = 4.dp, radius = 24.dp) {
            BoxWithConstraints(Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))) {
                val cell = maxWidth / 9
                val sel = selected
                Column {
                    for (r in 0 until 9) Row {
                        for (c in 0 until 9) {
                            val i = r * 9 + c
                            val d = grid[i]
                            val given = puzzle.givens[i] != 0
                            val wrong = d != 0 && d != puzzle.solution[i]
                            val peer = sel != null && (sel / 9 == r || sel % 9 == c || (sel / 27 == r / 3 && sel % 9 / 3 == c / 3))
                            val bg = when {
                                i == sel -> BLUE
                                sel != null && d != 0 && d == grid[sel] -> Color(0xFFDFE6FF)
                                peer -> Color(0xFFF2F5FF)
                                else -> Color.White
                            }
                            Box(
                                Modifier.size(cell).background(bg)
                                    .drawBehind {
                                        val w = size.width
                                        val h = size.height
                                        fun edge(thick: Boolean, a: Offset, b: Offset) =
                                            drawLine(if (thick) INK else Color(0xFFE3E6F3), a, b, (if (thick) 2.dp else 1.dp).toPx())
                                        if (r > 0) edge(r % 3 == 0, Offset(0f, 0f), Offset(w, 0f))
                                        if (c > 0) edge(c % 3 == 0, Offset(0f, 0f), Offset(0f, h))
                                    }
                                    .clickable { selected = i; feedback.tap() }
                                    .semantics { contentDescription = "Row ${r + 1} column ${c + 1}" + if (d != 0) ", $d" else ", empty" },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (d != 0) {
                                    Text(
                                        "$d", style = displayStyle((cell.value * 0.6f).sp),
                                        color = when {
                                            i == sel -> Color.White
                                            wrong -> RED
                                            given -> INK
                                            else -> BLUE
                                        },
                                    )
                                } else if (notes[i].orEmpty().isNotEmpty()) {
                                    NotesGrid(notes.getValue(i), if (i == sel) Color.White else Color(0xFF6B6F8A), (cell.value * 0.28f).sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        // Number pad: digit plus how many are still to place. Finished digits fade out.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (d in 1..9) {
                val left = 9 - placed[d]
                Column(
                    Modifier.weight(1f).height(58.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                        .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF1F3FB))), RoundedCornerShape(14.dp))
                        .border(1.dp, if (noteMode) BLUE.copy(alpha = 0.5f) else Color.White, RoundedCornerShape(14.dp))
                        .clickable(enabled = left > 0) { selected?.let { put(it, d) } }
                        .alpha(if (left > 0) 1f else 0.35f)
                        .semantics { contentDescription = "$d, $left left" },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("$d", style = displayStyle(24.sp), color = INK)
                    Text("$left LEFT", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF6B6F8A))
                }
            }
        }
    }

    if (won) {
        val stars = when (mistakes) { 0 -> 3; 1, 2 -> 2; else -> 1 }
        ResultDialog("Solved!", "Level: $level\nTime: ${time(seconds)}\nMistakes: $mistakes", if (mode.daily) "Done" else "Next level", {
            ads.levelDone()
            mode.win(store, level, stars) {
                level++
                store.put("level", level)
            }
        }, stars = stars)
    }
}

/** Small 3×3 pencil marks inside a cell. */
@Composable
private fun NotesGrid(digits: Set<Int>, color: Color, size: androidx.compose.ui.unit.TextUnit) {
    Column(Modifier.fillMaxSize().padding(1.dp)) {
        for (row in 0 until 3) Row(Modifier.weight(1f)) {
            for (col in 0 until 3) {
                val d = row * 3 + col + 1
                Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (d in digits) Text("$d", fontSize = size, color = color, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun time(s: Int) = "%d:%02d".format(s / 60, s % 60)
