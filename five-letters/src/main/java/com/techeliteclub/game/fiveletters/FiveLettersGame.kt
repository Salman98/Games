package com.techeliteclub.game.fiveletters

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.Hint
import com.techeliteclub.game.core.KEY_DELETE
import com.techeliteclub.game.core.KEY_ENTER
import com.techeliteclub.game.core.LetterKeyboard
import com.techeliteclub.game.core.LevelSave
import com.techeliteclub.game.core.LocalPlayMode
import com.techeliteclub.game.core.Pill
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.displayStyle
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.launch

private val OK = listOf(Color(0xFF43E0B8), Color(0xFF14A580))
private val NEAR = listOf(Color(0xFFFFCF5C), Color(0xFFF09A12))
private val NO = listOf(Color(0xFF3A4458), Color(0xFF3A4458))
private val SLOT = Color(0xFF1D2433)

private fun colors(mark: Int) = when (mark) { CORRECT -> OK; PRESENT -> NEAR; else -> NO }

/** One letter square. [mark] null = not scored yet; [ghost] shows a hinted letter faintly. */
@Composable
fun LetterBox(ch: Char?, mark: Int?, size: Dp, delayMs: Int = 0, ghost: Boolean = false) {
    // Scored squares flip over one after another.
    val flip = remember(mark != null) { Animatable(if (mark != null) 0f else 1f) }
    LaunchedEffect(mark != null) { if (mark != null) flip.animateTo(1f, tween(280, delayMs)) }
    val scored = mark != null && flip.value > 0.5f
    Box(
        Modifier.size(size).graphicsLayer { scaleY = if (mark != null) kotlin.math.abs(flip.value * 2 - 1).coerceAtLeast(0.05f) else 1f }
            .background(if (scored) Brush.verticalGradient(colors(mark!!)) else Brush.verticalGradient(listOf(SLOT, SLOT)), RoundedCornerShape(size * 0.18f))
            .then(if (scored) Modifier else Modifier.border(2.dp, if (ch != null && !ghost) Color(0xFF7F8BA3) else Color(0xFF2C3548), RoundedCornerShape(size * 0.18f))),
        contentAlignment = Alignment.Center,
    ) { if (ch != null) Text("$ch", style = displayStyle((size.value * 0.55f).sp), color = Color.White.copy(alpha = if (ghost) 0.3f else 1f)) }
}

/** Rows of guesses, the row being typed (with hinted letters as ghosts), then empty rows. */
@Composable
fun GuessGrid(guesses: List<String>, typing: String, answer: String, shown: Set<Int>, box: Dp, rows: Int = TRIES, shake: Float = 0f) {
    Column(
        Modifier.semantics { contentDescription = "Guesses: " + guesses.joinToString().ifEmpty { "none yet" } },
        verticalArrangement = Arrangement.spacedBy(box * 0.12f),
    ) {
        for (r in 0 until rows) Row(
            Modifier.offset(x = if (r == guesses.size) (shake * 8).dp else 0.dp),
            horizontalArrangement = Arrangement.spacedBy(box * 0.12f),
        ) {
            val marks = guesses.getOrNull(r)?.let { score(it, answer) }
            for (c in 0 until LEN) when {
                marks != null -> LetterBox(guesses[r][c], marks[c], box, c * 120)
                r == guesses.size && c < typing.length -> LetterBox(typing[c], null, box)
                r == guesses.size && c in shown -> LetterBox(answer[c], null, box, ghost = true)
                else -> LetterBox(null, null, box)
            }
        }
    }
}

@Composable
fun FiveLettersGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val mode = LocalPlayMode.current
    val scope = rememberCoroutineScope()
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val answer = remember(level) { wordFor(mode.seedFor(level)) }
    val slot = remember(level) { LevelSave(store, mode, level) }
    val saved = remember(level) { slot.load { decodeGame(it) } }
    var guesses by remember(level) { mutableStateOf(saved?.first ?: emptyList()) }
    var shown by remember(level) { mutableStateOf(saved?.second ?: emptySet()) }
    var typing by remember(level) { mutableStateOf("") }
    val shake = remember { Animatable(0f) }
    val won = guesses.lastOrNull() == answer
    val lost = !won && guesses.size >= TRIES
    LaunchedEffect(guesses, shown, won) { if (won || lost) slot.clear() else slot.save(encodeGame(guesses, shown)) }

    fun key(k: Char) {
        if (won || lost) return
        when (k) {
            KEY_DELETE -> { typing = typing.dropLast(1); feedback.tap() }
            KEY_ENTER -> if (isGuess(typing)) {
                guesses = guesses + typing
                if (typing == answer) feedback.clear() else feedback.good()
                typing = ""
            } else {
                feedback.bad()
                scope.launch { for (x in listOf(1f, -1f, 0.6f, -0.6f, 0f)) shake.animateTo(x, tween(50)) }
            }
            else -> if (typing.length < LEN) { typing += k; feedback.tap() }
        }
    }

    val marks = keyMarks(guesses, answer)
    GameScaffold(
        ads, "FIVE LETTERS",
        pills = listOf(Pill(mode.label(level)), Pill("Guess ${minOf(guesses.size + 1, TRIES)}/$TRIES", GameIcons.Grid)),
        tools = emptyList(),
        hint = Hint("Show a letter", GameIcons.Bulb, levelKey = mode.hintKey(level), enabled = nextHint(guesses, shown, answer) != null) {
            nextHint(guesses, shown, answer)?.let { shown = shown + it }
            feedback.good()
        },
    ) {
        // Squares shrink on short screens so the grid and the keyboard always fit.
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val box = minOf(54.dp, (maxHeight - 172.dp) / 6.7f).coerceAtLeast(28.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GuessGrid(guesses, typing, answer, shown, box, shake = shake.value)
                Spacer(Modifier.height(16.dp))
                LetterKeyboard(colors = marks.mapValues { (_, m) -> colors(m)[1] }) { key(it) }
            }
        }
    }

    if (won) {
        val stars = when { guesses.size <= 3 -> 3; guesses.size <= 5 -> 2; else -> 1 }
        ResultDialog("You got it!", "Word: $answer\nGuesses: ${guesses.size}", if (mode.daily) "Done" else "Next word", {
            ads.levelDone()
            mode.win(store, level, stars) {
                level++
                store.put("level", level)
            }
        }, stars = stars)
    } else if (lost) {
        // The word stays hidden so trying again is still a puzzle.
        ResultDialog("Out of guesses", "Same word, six new tries.", "Try again", {
            ads.levelDone()
            guesses = emptyList(); typing = ""
        }, win = false, stars = 0)
    }
}
