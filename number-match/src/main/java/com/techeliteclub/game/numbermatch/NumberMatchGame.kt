package com.techeliteclub.game.numbermatch

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.techeliteclub.game.core.pressable
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlin.random.Random

private val NAVY = Color(0xFF14265E)
private val PICK = listOf(Color(0xFFFFD27A), Color(0xFFFF9F1C))
private val HINT = listOf(Color(0xFFB9F5D0), Color(0xFF3CCB7F))

/** One number square. Crossed-out squares fade to a faint digit; [picked] glows orange, [hinted] green. */
@Composable
fun NumberCell(value: Int, size: Dp, picked: Boolean, hinted: Boolean, onTap: (() -> Unit)?) {
    val fade by animateFloatAsState(if (value == 0) 0f else 1f, label = "fade")
    val pop by animateFloatAsState(if (picked) 1.1f else 1f, spring(dampingRatio = 0.45f, stiffness = 800f), label = "pop")
    Box(Modifier.size(size).padding(size * 0.05f), contentAlignment = Alignment.Center) {
        if (value != 0 || fade > 0f) {
            val bg = when {
                picked -> Brush.verticalGradient(PICK)
                hinted -> Brush.verticalGradient(HINT)
                else -> Brush.verticalGradient(listOf(Color.White, Color(0xFFF4F7FF)))
            }
            Box(
                Modifier.fillMaxSize().scale(pop).alpha(fade).shadow(if (picked) 6.dp else 1.dp, RoundedCornerShape(size * 0.22f))
                    .background(bg, RoundedCornerShape(size * 0.22f))
                    .then(if (onTap != null && value != 0) Modifier.pressable(label = "Number $value") { onTap() } else Modifier),
                contentAlignment = Alignment.Center,
            ) { Text("$value", style = displayStyle((size.value * 0.5f).sp), color = if (picked) Color(0xFF5A3000) else NAVY) }
        }
    }
}

/** The board as rows of nine. */
@Composable
fun MatchBoard(board: List<Int>, width: Dp, picked: Int?, hint: Pair<Int, Int>?, onTap: ((Int) -> Unit)?) {
    val cell = width / COLS
    Column(Modifier.semantics { contentDescription = "Number board, ${board.count { it != 0 }} numbers left" }) {
        for (row in board.indices.chunked(COLS)) Row {
            for (i in row) NumberCell(board[i], cell, picked == i, hint != null && (hint.first == i || hint.second == i), onTap?.let { f -> { f(i) } })
        }
    }
}

@Composable
fun NumberMatchGame(ads: Ads) {
    val t = LocalGameTheme.current
    val store = rememberStore()
    val feedback = rememberFeedback()
    val mode = LocalPlayMode.current
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val slot = remember(level) { LevelSave(store, mode, level) }
    val start = remember(level) { slot.load { decodeGame(it) } ?: Game(newBoard(level, Random(mode.seedFor(level))), START_ADDS, 0, 0) }
    var board by remember(level) { mutableStateOf(start.board) }
    var adds by remember(level) { mutableIntStateOf(start.adds) }
    var score by remember(level) { mutableIntStateOf(start.score) }
    var seconds by remember(level) { mutableIntStateOf(start.seconds) }
    var picked by remember(level) { mutableStateOf<Int?>(null) }
    var hint by remember(level) { mutableStateOf<Pair<Int, Int>?>(null) }
    var attempt by remember(level) { mutableIntStateOf(0) } // bumps on "Try again" so the board restarts
    val history = remember(level, attempt) { mutableStateListOf<Game>() }
    val won = isCleared(board)
    val stuck = !won && adds == 0 && findPair(board) == null
    LaunchedEffect(level, attempt, won, stuck) { while (!won && !stuck) { delay(1000); seconds++ } }
    LaunchedEffect(board, adds, won) { if (won) slot.clear() else slot.save(encodeGame(Game(board, adds, score, seconds))) }

    fun snapshot() { history.add(Game(board, adds, score, seconds)); if (history.size > 40) history.removeAt(0) }
    fun tap(i: Int) {
        val first = picked
        when {
            first == null -> { picked = i; feedback.tap() }
            first == i -> picked = null
            canPair(board, first, i) -> {
                snapshot()
                val (next, rows) = cross(board, first, i)
                board = next
                score += 10 + rows * 25
                picked = null
                hint = null
                if (rows > 0) feedback.clear() else feedback.good()
            }
            else -> { picked = i; feedback.bad() }
        }
    }

    GameScaffold(
        ads, "NUMBER MATCH",
        pills = listOf(Pill(mode.label(level)), Pill("$score", GameIcons.StarGold, gold = true), Pill(time(seconds), GameIcons.Clock)),
        tools = listOf(
            Tool("Undo", GameIcons.Undo, enabled = history.isNotEmpty()) {
                val g = history.removeAt(history.lastIndex)
                board = g.board; adds = g.adds; score = g.score; picked = null; hint = null
            },
            Tool("Add numbers $adds", GameIcons.Plus, enabled = adds > 0 && !won) {
                snapshot()
                board = addNumbers(board); adds--; picked = null; hint = null
                feedback.good()
            },
        ),
        hint = Hint("Hint", GameIcons.Bulb, levelKey = mode.hintKey(level), enabled = findPair(board) != null) {
            hint = findPair(board); picked = null; feedback.good()
        },
    ) {
        Bezel(Modifier.fillMaxWidth().aspectRatio(1f), padding = 6.dp) {
            BoxWithConstraints(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                MatchBoard(board, maxWidth, picked, hint) { tap(it) }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            if (findPair(board) == null && adds > 0) "No pairs left. Tap Add numbers." else "Equal numbers, or two that make 10. Nothing between them.",
            color = t.glassInk, fontSize = 14.sp, textAlign = TextAlign.Center,
            modifier = Modifier.glass(t, RoundedCornerShape(50)).padding(horizontal = 18.dp, vertical = 9.dp),
        )
    }

    if (won) {
        val stars = when { adds >= 2 -> 3; adds == 1 -> 2; else -> 1 }
        ResultDialog("Board cleared!", "Level: $level\nScore: $score\nTime: ${time(seconds)}", if (mode.daily) "Done" else "Next level", {
            ads.levelDone()
            mode.win(store, level, stars) {
                level++
                store.put("level", level)
            }
        }, stars = stars)
    } else if (stuck) {
        ResultDialog("Out of moves", "Level: $level\nScore: $score", "Try again", {
            slot.clear()
            board = newBoard(level, Random(mode.seedFor(level))); adds = START_ADDS; score = 0; seconds = 0; picked = null; hint = null
            attempt++
        }, win = false, stars = 0)
    }
}

fun time(s: Int) = "%d:%02d".format(s / 60, s % 60)
