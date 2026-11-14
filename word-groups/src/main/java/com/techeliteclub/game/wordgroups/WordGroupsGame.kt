package com.techeliteclub.game.wordgroups

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.Hint
import com.techeliteclub.game.core.LevelSave
import com.techeliteclub.game.core.LocalGameTheme
import com.techeliteclub.game.core.LocalPlayMode
import com.techeliteclub.game.core.Pill
import com.techeliteclub.game.core.PrimaryButton
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.Tool
import com.techeliteclub.game.core.displayStyle
import com.techeliteclub.game.core.pressable
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Bar colours, in the order groups are found. */
private val BARS = listOf(
    listOf(Color(0xFFFFD27A), Color(0xFFF5A524)) to Color(0xFF4A2A00),
    listOf(Color(0xFF9BE8A8), Color(0xFF3CBF6A)) to Color(0xFF0D3A1A),
    listOf(Color(0xFF9CC8FF), Color(0xFF4A8DF0)) to Color(0xFF0B2A5E),
    listOf(Color(0xFFFFA8C8), Color(0xFFEF5D94)) to Color(0xFF5A0A2A),
)
private val PURPLE = listOf(Color(0xFF8F5CFF), Color(0xFF5A28C8))
private val INK = Color(0xFF2E1366)

/** A found group: its name and words on a coloured bar. [order] picks the colour. */
@Composable
fun GroupBar(group: Category, order: Int, height: Dp = 56.dp) {
    val (fill, ink) = BARS[order % BARS.size]
    val drop = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) { drop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 500f)) }
    Column(
        Modifier.fillMaxWidth().height(height).scale(drop.value).background(Brush.verticalGradient(fill), RoundedCornerShape(14.dp)),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(group.name, style = displayStyle(17.sp), color = ink)
        Text(group.words.joinToString(", "), color = ink, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

/** One word tile; purple and lifted when picked. */
@Composable
fun WordTile(word: String, picked: Boolean, modifier: Modifier = Modifier, height: Dp = 56.dp, onTap: (() -> Unit)? = null) {
    val lift by animateFloatAsState(if (picked) 1.05f else 1f, spring(dampingRatio = 0.5f, stiffness = 700f), label = "lift")
    Box(
        modifier.height(height).scale(lift).shadow(if (picked) 8.dp else 2.dp, RoundedCornerShape(14.dp))
            .background(if (picked) Brush.verticalGradient(PURPLE) else Brush.verticalGradient(listOf(Color.White, Color(0xFFF1EAFF))), RoundedCornerShape(14.dp))
            .then(if (onTap != null) Modifier.pressable(label = word) { onTap() } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(word, style = displayStyle(if (word.length > 7) 12.sp else if (word.length > 5) 14.sp else 17.sp), color = if (picked) Color.White else INK, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
fun WordGroupsGame(ads: Ads) {
    val t = LocalGameTheme.current
    val store = rememberStore()
    val feedback = rememberFeedback()
    val mode = LocalPlayMode.current
    val scope = rememberCoroutineScope()
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val puzzle = remember(level) { newPuzzle(Random(mode.seedFor(level))) }
    val slot = remember(level) { LevelSave(store, mode, level) }
    val saved = remember(level) { slot.load { decodeGame(it) } }
    var solved by remember(level) { mutableStateOf(saved?.first ?: emptyList()) }
    var mistakes by remember(level) { mutableIntStateOf(saved?.second ?: 0) }
    var seconds by remember(level) { mutableIntStateOf(saved?.third ?: 0) }
    var order by remember(level) { mutableStateOf(puzzle.words) }
    var picked by remember(level) { mutableStateOf(emptySet<String>()) }
    var note by remember(level) { mutableStateOf("Pick four words that belong together.") }
    val shake = remember { Animatable(0f) }
    val won = solved.size == GROUPS
    val lost = !won && mistakes >= MAX_MISTAKES
    LaunchedEffect(level, won, lost) { while (!won && !lost) { delay(1000); seconds++ } }
    LaunchedEffect(solved, mistakes, won) { if (won || lost) slot.clear() else slot.save(encodeGame(solved, mistakes, seconds)) }

    val left = order.filter { w -> solved.none { w in puzzle.groups[it].words } }
    fun submit() {
        val g = groupOf(puzzle, picked)
        if (g != null) {
            solved = solved + g
            picked = emptySet()
            note = "Found: ${puzzle.groups[g].name.lowercase().replaceFirstChar { it.uppercase() }}"
            feedback.clear()
        } else {
            mistakes++
            note = if (oneAway(puzzle, picked)) "One away!" else "Not a group. Try another four."
            feedback.bad()
            scope.launch { for (x in listOf(1f, -1f, 0.6f, -0.6f, 0f)) shake.animateTo(x, tween(50)) }
        }
    }

    GameScaffold(
        ads, "WORD GROUPS",
        pills = listOf(Pill(mode.label(level)), Pill("${solved.size}/$GROUPS", GameIcons.Grid), Pill(time(seconds), GameIcons.Clock)),
        tools = listOf(
            Tool("Shuffle", GameIcons.Shuffle) { order = order.shuffled() },
            Tool("Deselect", GameIcons.Erase, enabled = picked.isNotEmpty()) { picked = emptySet() },
        ),
        hint = Hint("Reveal group", GameIcons.Bulb, levelKey = mode.hintKey(level), enabled = !won) {
            (0 until GROUPS).firstOrNull { it !in solved }?.let { solved = solved + it; picked = emptySet(); feedback.good() }
        },
    ) {
        Column(Modifier.fillMaxWidth().offset(x = (shake.value * 8).dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            solved.forEachIndexed { k, g -> GroupBar(puzzle.groups[g], k) }
            for (row in left.chunked(4)) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (w in row) WordTile(w, w in picked, Modifier.weight(1f)) {
                    picked = if (w in picked) picked - w else if (picked.size < PER_GROUP) picked + w else picked
                    feedback.tap()
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Mistakes left", color = t.ink, fontSize = 14.sp, fontWeight = FontWeight.Black)
            for (k in 0 until MAX_MISTAKES) Box(Modifier.size(14.dp).background(if (k < MAX_MISTAKES - mistakes) PURPLE[1] else Color(0xFFD9CDF5), CircleShape))
        }
        Text(note, color = t.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(12.dp))
        PrimaryButton("SUBMIT", Modifier.fillMaxWidth(0.6f).scale(if (picked.size == PER_GROUP) 1f else 0.96f), height = 56.dp, fontSize = 24.sp) {
            if (picked.size == PER_GROUP && !won && !lost) submit() else feedback.tap()
        }
    }

    if (won) {
        val stars = when (mistakes) { 0 -> 3; 1, 2 -> 2; else -> 1 }
        ResultDialog("All four groups!", "Level: $level\nMistakes: $mistakes\nTime: ${time(seconds)}", if (mode.daily) "Done" else "Next level", {
            ads.levelDone()
            mode.win(store, level, stars) {
                level++
                store.put("level", level)
            }
        }, stars = stars)
    } else if (lost) {
        ResultDialog("Four mistakes", "Same words, fresh start.", "Try again", {
            ads.levelDone()
            solved = emptyList(); mistakes = 0; seconds = 0; picked = emptySet(); order = order.shuffled()
            note = "Pick four words that belong together."
        }, win = false, stars = 0)
    }
}

fun time(s: Int) = "%d:%02d".format(s / 60, s % 60)
