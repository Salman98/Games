package com.techeliteclub.game.carescape

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.Bezel
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.Hint
import com.techeliteclub.game.core.LocalPlayMode
import com.techeliteclub.game.core.Pill
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.Tool
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlin.math.roundToInt
import kotlin.random.Random

private val CAR_COLORS = listOf(
    Color(0xFF33D6FF), Color(0xFF9B5CFF), Color(0xFFFFC233), Color(0xFF2EE59D), Color(0xFFFF8A3D),
    Color(0xFF4F8DFF), Color(0xFFC86BFF), Color(0xFF2FD0C0), Color(0xFFB0BEC5),
)
private val RED_CAR = Color(0xFFFF2D55)
private val NEON = Color(0xFF2EE59D)

/** A glossy car body with windows and headlights. Index 0 is the red hero car. */
@Composable
fun CarBody(color: Color, horizontal: Boolean, w: Dp, h: Dp, hero: Boolean, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier.size(w, h)
            .shadow(if (hero) 16.dp else 8.dp, shape, ambientColor = if (hero) RED_CAR else Color.Black, spotColor = if (hero) RED_CAR else Color.Black)
            .background(color, shape)
            .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.5f), Color.Transparent), end = Offset(160f, 160f)), shape)
            .then(if (hero) Modifier.border(3.dp, Color.White, shape) else Modifier)
            .drawBehind {
                val glass = Brush.verticalGradient(listOf(Color(0xFF0B1030), Color(0xFF2A3870)))
                val m = minOf(size.width, size.height)
                val r = CornerRadius(5.dp.toPx())
                if (horizontal) {
                    drawRoundRect(glass, Offset(size.width * 0.14f, m * 0.18f), Size(m * 0.3f, m * 0.64f), r)
                    drawRoundRect(glass, Offset(size.width - m * 0.62f, m * 0.18f), Size(m * 0.26f, m * 0.64f), r)
                    for (y in listOf(0.2f, 0.66f)) drawRoundRect(Color(0xFFFFF6C4), Offset(size.width - 6.dp.toPx(), m * y), Size(4.dp.toPx(), m * 0.14f), r)
                } else {
                    drawRoundRect(glass, Offset(m * 0.18f, size.height * 0.12f), Size(m * 0.64f, m * 0.3f), r)
                    drawRoundRect(glass, Offset(m * 0.18f, size.height - m * 0.56f), Size(m * 0.64f, m * 0.26f), r)
                }
            }
    )
}

@Composable
fun CarEscapeGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val mode = LocalPlayMode.current
    var level by remember { mutableIntStateOf(mode.startLevel(store)) }
    val start = remember(level) { newLevel(level, Random(mode.seedFor(level))) }
    val par = remember(start) { solve(start)?.size ?: 0 } // fewest moves possible
    var cars by remember(level) { mutableStateOf(start) }
    var history by remember(level) { mutableStateOf(listOf<List<Car>>()) }
    val moves = history.size
    val won = isSolved(cars)

    fun play(m: Move) {
        if (m.delta == 0) return
        history = history + listOf(cars)
        cars = move(cars, m)
        if (!isSolved(cars)) feedback.good()
    }

    GameScaffold(
        ads, "CAR ESCAPE",
        pills = listOf(Pill(mode.label(level)), Pill("MOVES $moves"), Pill("PAR $par", GameIcons.StarGold, gold = true)),
        tools = listOf(
            Tool("Undo", GameIcons.Undo, enabled = history.isNotEmpty()) { cars = history.last(); history = history.dropLast(1) },
            Tool("Restart", GameIcons.Restart) { cars = start; history = emptyList() },
        ),
        hint = Hint("Next move", GameIcons.Bulb, levelKey = mode.hintKey(level)) { solve(cars)?.firstOrNull()?.let(::play) },
    ) {
        Bezel(Modifier.fillMaxWidth().aspectRatio(1f), padding = 10.dp) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val cell = maxWidth / N
                val cellPx = with(LocalDensity.current) { cell.toPx() }
                // Faint parking-bay lines.
                Box(
                    Modifier.fillMaxSize().drawBehind {
                        val step = size.width / N
                        for (k in 1 until N) {
                            drawLine(Color(0x1F78C8FF), Offset(k * step, 0f), Offset(k * step, size.height), 1f)
                            drawLine(Color(0x1F78C8FF), Offset(0f, k * step), Offset(size.width, k * step), 1f)
                        }
                    }
                )
                // Neon exit gate at the end of the exit row.
                Box(
                    Modifier.offset(maxWidth - 5.dp, cell * EXIT_ROW + 6.dp).size(5.dp, cell - 12.dp)
                        .shadow(10.dp, RoundedCornerShape(3.dp), ambientColor = NEON, spotColor = NEON)
                        .background(NEON, RoundedCornerShape(3.dp))
                )
                cars.forEachIndexed { i, car ->
                    CarView(i, car, cell, cellPx, slideRange(cars, i)) { delta -> play(Move(i, delta)) }
                }
            }
        }
    }

    if (won) {
        val stars = when { moves <= par -> 3; moves <= par + 3 -> 2; else -> 1 }
        ResultDialog("Escaped!", "Level: $level\nMoves: $moves (par $par)", if (mode.daily) "Done" else "Next level", {
            ads.levelDone()
            mode.win(store, level, stars) {
                level++
                store.put("level", level)
            }
        }, stars = stars)
    }
}

@Composable
private fun CarView(index: Int, car: Car, cell: Dp, cellPx: Float, range: IntRange, onSlide: (Int) -> Unit) {
    var dragPx by remember(car) { mutableFloatStateOf(0f) }
    val currentRange by rememberUpdatedState(range)
    val shown = dragPx.coerceIn(range.first * cellPx, range.last * cellPx) / cellPx
    val (w, h) = if (car.horizontal) cell * car.len to cell else cell to cell * car.len
    CarBody(
        if (index == 0) RED_CAR else CAR_COLORS[index % CAR_COLORS.size], car.horizontal, w - 8.dp, h - 8.dp, hero = index == 0,
        Modifier
            .offset(
                cell * (car.c + if (car.horizontal) shown else 0f) + 4.dp,
                cell * (car.r + if (car.horizontal) 0f else shown) + 4.dp,
            )
            .semantics { contentDescription = if (index == 0) "Red car" else "Car ${index + 1}" }
            .pointerInput(car) {
                detectDragGestures(
                    onDragEnd = {
                        val r = currentRange
                        onSlide((dragPx / cellPx).roundToInt().coerceIn(r.first, r.last))
                        dragPx = 0f
                    },
                    onDragCancel = { dragPx = 0f },
                ) { change, amount ->
                    change.consume()
                    dragPx += if (car.horizontal) amount.x else amount.y
                }
            },
    )
}

/** Hero art helper for the home screen. */
@Composable
fun HeroCar(horizontal: Boolean, w: Dp, h: Dp, hero: Boolean, modifier: Modifier = Modifier) =
    CarBody(if (hero) RED_CAR else CAR_COLORS[0], horizontal, w, h, hero, modifier.padding(0.dp))
