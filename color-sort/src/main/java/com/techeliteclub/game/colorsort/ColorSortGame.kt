package com.techeliteclub.game.colorsort

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.LevelSave
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.GlassPill
import com.techeliteclub.game.core.Hint
import com.techeliteclub.game.core.LocalPlayMode
import com.techeliteclub.game.core.Pill
import com.techeliteclub.game.core.PillText
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.Tool
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlin.random.Random

/** Ball colours: light highlight + deep base. */
val BALLS = listOf(
    Color(0xFFFF6B7A) to Color(0xFFD6143A), Color(0xFF6AB0FF) to Color(0xFF1F5EE0), Color(0xFF7CF09A) to Color(0xFF16A346),
    Color(0xFFFFE27A) to Color(0xFFF0A800), Color(0xFFD39BFF) to Color(0xFF8326D1), Color(0xFFFFB36B) to Color(0xFFF06A00),
    Color(0xFF8BF0FF) to Color(0xFF0AA3C2), Color(0xFFFFB0D8) to Color(0xFFD8247A), Color(0xFFD7B59A) to Color(0xFF8A5A3A),
)

@Composable
fun Ball(color: Int, size: Dp, modifier: Modifier = Modifier) {
    val (hi, base) = BALLS[color % BALLS.size]
    Box(
        modifier.size(size).shadow(3.dp, CircleShape).background(
            Brush.radialGradient(0f to Color.White, 0.12f to hi, 0.45f to hi, 1f to base), CircleShape,
        )
    )
}

@Composable
fun Tube(balls: List<Int>, selected: Boolean, ball: Dp = 34.dp, onClick: (() -> Unit)? = null) {
    val lift by animateDpAsState(if (selected) (-16).dp else 0.dp, label = "lift")
    val done = balls.size == CAP && balls.all { it == balls[0] }
    val rim = when {
        selected -> Color(0xFFFFE27A)
        done -> Color(0xFFA6FFBF)
        else -> Color(0xBFDCFAFF)
    }
    val shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 26.dp, bottomEnd = 26.dp)
    Box(
        Modifier.offset(y = lift).semantics { contentDescription = if (done) "Finished tube" else "Tube with ${balls.size} balls" },
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier
                .padding(top = 6.dp)
                .width(ball + 16.dp)
                .height(ball * CAP + 26.dp)
                .then(if (done) Modifier.shadow(16.dp, shape, ambientColor = rim, spotColor = rim) else Modifier)
                .background(
                    Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.04f), Color.White.copy(alpha = 0.2f))),
                    shape,
                )
                .border(2.5.dp, rim, shape)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(bottom = 7.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val shown = if (selected && balls.isNotEmpty()) balls.dropLast(1) else balls
            for (c in shown.asReversed()) Ball(c, ball)
        }
        // lip of the tube
        Box(Modifier.width(ball + 28.dp).height(8.dp).background(rim, RoundedCornerShape(5.dp)))
        if (selected && balls.isNotEmpty()) Ball(balls.last(), ball, Modifier.offset(y = -ball - 18.dp))
        if (done) {
            Box(
                Modifier.offset(y = (-38).dp).size(30.dp).shadow(6.dp, CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF63E47E), Color(0xFF1FA046))), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(GameIcons.Check, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
        }
    }
}

@Composable
fun ColorSortGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val mode = LocalPlayMode.current
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val slot = remember(level) { LevelSave(store, mode, level) }
    val saved = remember(level) { slot.load { decodeStates(it, level) } }
    var tubes by remember(level) { mutableStateOf(saved?.last() ?: newLevel(level, Random(mode.seedFor(level)))) }
    var history by remember(level) { mutableStateOf(saved?.dropLast(1) ?: listOf<Tubes>()) }
    var selected by remember(level) { mutableStateOf<Int?>(null) }
    val won = isSolved(tubes)
    LaunchedEffect(tubes, history, won) { if (won) slot.clear() else slot.save(encodeStates(history + listOf(tubes))) }

    fun restart() {
        tubes = newLevel(level, Random(mode.seedFor(level))); history = emptyList(); selected = null
    }

    fun tap(i: Int) {
        val from = selected
        if (from == null) {
            if (tubes[i].isNotEmpty()) { selected = i; feedback.tap() }
            return
        }
        if (from != i) {
            if (canMove(tubes, from, i)) {
                history = history + listOf(tubes)
                tubes = move(tubes, from, i)
                val t = tubes[i]
                if (t.size == CAP && t.all { it == t[0] }) feedback.clear() else feedback.good()
            } else feedback.bad()
        }
        selected = null
    }

    val canAddTube = tubes.size < colorsFor(level) + 4
    GameScaffold(
        ads, "COLOR SORT",
        pills = listOf(Pill(mode.label(level)), Pill("${history.size} MOVES")),
        tools = listOf(
            Tool("Undo", GameIcons.Undo, enabled = history.isNotEmpty()) {
                tubes = history.last(); history = history.dropLast(1); selected = null
            },
            Tool("Restart", GameIcons.Restart, onClick = ::restart),
        ),
        hint = Hint("Extra tube", GameIcons.Plus, levelKey = mode.hintKey(level), enabled = canAddTube) { tubes = tubes + listOf(emptyList()) },
    ) {
        // 4 tubes per row (5 with smaller balls on big levels); rows spaced so lifted balls never overlap.
        val perRow = if (tubes.size > 8) 5 else 4
        val ball = if (tubes.size > 8) 28.dp else 34.dp
        for (rowStart in tubes.indices step perRow) {
            Row(Modifier.fillMaxWidth().padding(vertical = if (tubes.size > 8) 12.dp else 30.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (i in rowStart until minOf(rowStart + perRow, tubes.size)) Tube(tubes[i], selected == i, ball) { tap(i) }
            }
        }
        if (!won && !hasMoves(tubes)) {
            Spacer(Modifier.height(8.dp))
            GlassPill { PillText("No moves left — undo, restart or add a tube") }
        }
    }

    if (won) {
        val stars = when { history.size <= colorsFor(level) * 4 -> 3; history.size <= colorsFor(level) * 6 -> 2; else -> 1 }
        ResultDialog("Level complete", "Level: $level\nMoves: ${history.size}", if (mode.daily) "Done" else "Next level", {
            ads.levelDone()
            mode.win(store, level, stars) {
                level++
                store.put("level", level)
            }
        }, stars = stars)
    }
}
