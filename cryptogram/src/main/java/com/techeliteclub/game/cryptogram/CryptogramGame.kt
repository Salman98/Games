package com.techeliteclub.game.cryptogram

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.Bezel
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.Hint
import com.techeliteclub.game.core.LetterKeyboard
import com.techeliteclub.game.core.LevelSave
import com.techeliteclub.game.core.LocalPlayMode
import com.techeliteclub.game.core.Pill
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.displayStyle
import com.techeliteclub.game.core.pressable
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlin.random.Random

private val INK = Color(0xFF133D2C)
private val PAPER = Color(0xFFFFFAF0)
private val BLANK = Color(0xFFEFE3C4)
private val EDGE = Color(0xFFC9A85E)
private val GOLD = Color(0xFFFFE58A)
private val GOLD_EDGE = Color(0xFFE09A00)
private val CODE_INK = Color(0xFF7A5A1C)

/** One letter of the quote: the tile (letter if [shown]) with its code number underneath. */
@Composable
private fun LetterTile(ch: Char, number: Int, shown: Boolean, selected: Boolean, w: Dp, onTap: (() -> Unit)?) {
    val pop by animateFloatAsState(if (shown) 1f else 0.94f, spring(dampingRatio = 0.4f, stiffness = 700f), label = "tile")
    Column(
        Modifier.width(w).then(if (onTap != null && !shown) Modifier.pressable(label = "Code $number") { onTap() } else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.padding(horizontal = 1.5.dp).fillMaxWidth().height(w * 1.15f).scale(pop)
                .background(if (selected) GOLD_EDGE else EDGE, RoundedCornerShape(7.dp)).padding(bottom = 3.dp)
                .background(if (selected) GOLD else if (shown) PAPER else BLANK, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center,
        ) { if (shown) Text("$ch", style = displayStyle((w.value * 0.7f).sp), color = INK) }
        Text("$number", color = CODE_INK, fontSize = (w.value * 0.4f).sp, fontWeight = FontWeight.Black)
    }
}

/** The quote as rows of tiles. [selected] is the hidden letter the player is working on (all its tiles glow). */
@Composable
fun QuoteBoard(p: Puzzle, width: Dp, perLine: Int, known: Set<Char>, selected: Char?, onTap: ((Char) -> Unit)? = null) {
    val w = width / (perLine + 1)
    Column(
        Modifier.semantics { contentDescription = "Coded quote, ${(p.letters - known).size} letters to find" },
        verticalArrangement = Arrangement.spacedBy(w * 0.35f), horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (line in wrap(p.quote, perLine)) Row(horizontalArrangement = Arrangement.spacedBy(w * 0.5f)) {
            for (word in line) Row {
                for (ch in word) LetterTile(ch, p.code.getValue(ch), ch in known, ch == selected, w, onTap?.let { f -> { f(ch) } })
            }
        }
    }
}

@Composable
fun CryptogramGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val mode = LocalPlayMode.current
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val puzzle = remember(level) { newPuzzle(level, Random(mode.seedFor(level))) }
    val slot = remember(level) { LevelSave(store, mode, level) }
    val saved = remember(level) { slot.load { decodeGame(it, puzzle) } }
    var found by remember(level) { mutableStateOf(saved?.first ?: emptySet()) }
    var mistakes by remember(level) { mutableIntStateOf(saved?.second ?: 0) }
    var seconds by remember(level) { mutableIntStateOf(saved?.third ?: 0) }
    val known = puzzle.given + found
    /** First hidden letter in reading order. */
    fun nextMissing(k: Set<Char>) = puzzle.quote.firstOrNull { it.isLetter() && it !in k }
    var selected by remember(level) { mutableStateOf(nextMissing(known)) }
    val won = isSolved(puzzle, found)
    val lost = mistakes >= MAX_MISTAKES
    LaunchedEffect(level, won, lost) { while (!won && !lost) { delay(1000); seconds++ } }
    LaunchedEffect(found, mistakes, won) { if (won || lost) slot.clear() else slot.save(encodeGame(found, mistakes, seconds)) }

    fun reveal(ch: Char) {
        found = found + ch
        selected = nextMissing(known + ch)
        feedback.good()
    }

    GameScaffold(
        ads, "CRYPTOGRAM",
        pills = listOf(Pill(mode.label(level)), Pill("$mistakes/$MAX_MISTAKES", GameIcons.Close), Pill(time(seconds), GameIcons.Clock)),
        tools = emptyList(),
        hint = Hint("Reveal a letter", GameIcons.Bulb, levelKey = mode.hintKey(level), enabled = selected != null) { selected?.let { reveal(it) } },
    ) {
        // The quote takes the room the keyboard leaves and scrolls if a long one does not fit.
        Bezel(Modifier.fillMaxWidth().weight(1f, fill = false), padding = 12.dp) {
            BoxWithConstraints(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
                QuoteBoard(puzzle, maxWidth, 12, known, selected) { selected = it; feedback.tap() }
            }
        }
        Spacer(Modifier.height(14.dp))
        // Letters already on the board are greyed: each letter has one number.
        LetterKeyboard(dim = known, actions = false) { key ->
            val want = selected
            if (want != null && !won && !lost) {
                if (key == want) reveal(want) else { mistakes++; feedback.bad() }
            }
        }
    }

    if (won) {
        val stars = 3 - mistakes
        ResultDialog("Decoded!", "Level: $level\nMistakes: $mistakes\nTime: ${time(seconds)}", if (mode.daily) "Done" else "Next level", {
            ads.levelDone()
            mode.win(store, level, stars) {
                level++
                store.put("level", level)
            }
        }, stars = stars)
    } else if (lost) {
        ResultDialog("Three mistakes", "The code stays the same. Have another go.", "Try again", {
            ads.levelDone()
            found = emptySet(); mistakes = 0; seconds = 0; selected = nextMissing(puzzle.given)
        }, win = false, stars = 0)
    }
}

fun time(s: Int) = "%d:%02d".format(s / 60, s % 60)
