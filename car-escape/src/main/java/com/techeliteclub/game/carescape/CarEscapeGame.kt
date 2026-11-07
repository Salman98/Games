package com.techeliteclub.game.carescape

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.PALETTE
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.rememberStore
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun CarEscapeGame(ads: Ads) {
    val store = rememberStore()
    var level by remember { mutableIntStateOf(store.get("level", 1)) }
    val start = remember(level) { newLevel(level, Random(level.toLong())) }
    var cars by remember(level) { mutableStateOf(start) }
    var moves by remember(level) { mutableIntStateOf(0) }
    val won = isSolved(cars)

    fun play(m: Move) {
        if (m.delta == 0) return
        cars = move(cars, m)
        moves++
    }

    GameScaffold("Car Escape", "Level $level · Moves $moves", ads, { cars = start; moves = 0 }, "Next move",
        onHint = { solve(cars)?.firstOrNull()?.let(::play) }) {
        Text("Slide the cars so the red car can drive out on the right.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        BoxWithConstraints(
            Modifier.fillMaxWidth().aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
        ) {
            val cell = maxWidth / N
            val cellPx = with(LocalDensity.current) { cell.toPx() }
            // Exit gap on the right edge of the exit row.
            Box(Modifier.offset(maxWidth - 4.dp, cell * EXIT_ROW).size(4.dp, cell).background(Color(0xFF43A047)))
            cars.forEachIndexed { i, car ->
                CarView(i, car, cell, cellPx, slideRange(cars, i)) { delta -> play(Move(i, delta)) }
            }
        }
    }

    if (won) {
        ResultDialog("Escaped!", "Level $level in $moves moves.", "Next level", {
            ads.levelDone()
            level++
            store.put("level", level)
        })
    }
}

@Composable
private fun CarView(
    index: Int, car: Car, cell: androidx.compose.ui.unit.Dp, cellPx: Float, range: IntRange, onSlide: (Int) -> Unit,
) {
    var dragPx by remember(car) { mutableFloatStateOf(0f) }
    val currentRange by rememberUpdatedState(range)
    val color = if (index == 0) Color(0xFFD32F2F) else PALETTE[1 + index % (PALETTE.size - 1)]
    val shown = dragPx.coerceIn(range.first * cellPx, range.last * cellPx) / cellPx
    val (w, h) = if (car.horizontal) cell * car.len to cell else cell to cell * car.len
    Box(
        Modifier
            .offset(
                cell * (car.c + if (car.horizontal) shown else 0f),
                cell * (car.r + if (car.horizontal) 0f else shown),
            )
            .size(w, h).padding(3.dp)
            .background(color, RoundedCornerShape(10.dp))
            .border(if (index == 0) 3.dp else 0.dp, Color.White, RoundedCornerShape(10.dp))
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
            }
    )
}
