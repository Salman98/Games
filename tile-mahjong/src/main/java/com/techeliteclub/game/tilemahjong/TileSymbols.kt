package com.techeliteclub.game.tilemahjong

import androidx.compose.foundation.Canvas
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.runtime.Composable

/** Two colour sets, so 8 shapes give 16 different tiles. Each set: main, second, accent. */
private val SETS = listOf(
    Triple(Color(0xFFD32F2F), Color(0xFF1E66E5), Color(0xFF2E9E4F)),
    Triple(Color(0xFF7E57C2), Color(0xFFF57C00), Color(0xFF00897B)),
)

/** Draws tile picture [symbol] (0 until SYMBOL_COUNT). */
@Composable
fun TileSymbol(symbol: Int, modifier: Modifier) {
    val (a, b, c) = SETS[(symbol / 8) % SETS.size]
    Canvas(modifier) { drawShape(symbol % 8, a, b, c) }
}

private fun DrawScope.drawShape(shape: Int, a: Color, b: Color, c: Color) {
    val w = size.width
    val h = size.height
    val m = minOf(w, h)
    val center = Offset(w / 2, h / 2)
    when (shape) {
        0 -> { // dot ring
            drawCircle(a, m * 0.32f, center, style = Stroke(m * 0.09f))
            drawCircle(b, m * 0.13f, center)
        }
        1 -> { // bamboo: two sticks with joints
            for (x in listOf(0.3f, 0.62f)) {
                drawRoundRect(c, Offset(w * x, h * 0.1f), Size(w * 0.16f, h * 0.8f), CornerRadius(w * 0.08f))
                drawLine(Color.White, Offset(w * x, h * 0.5f), Offset(w * (x + 0.16f), h * 0.5f), m * 0.04f)
            }
        }
        2 -> { // sun with rays
            drawCircle(a, m * 0.2f, center)
            for (i in 0 until 8) {
                val ang = Math.toRadians(i * 45.0)
                val dx = kotlin.math.cos(ang).toFloat()
                val dy = kotlin.math.sin(ang).toFloat()
                drawLine(b, center + Offset(dx, dy) * (m * 0.3f), center + Offset(dx, dy) * (m * 0.44f), m * 0.07f, StrokeCap.Round)
            }
        }
        3 -> { // flower: four petals
            for ((dx, dy) in listOf(0f to -1f, 1f to 0f, 0f to 1f, -1f to 0f)) drawCircle(a, m * 0.16f, center + Offset(dx, dy) * (m * 0.2f))
            drawCircle(Color(0xFFFFD54F), m * 0.12f, center)
        }
        4 -> { // three dots diagonal
            drawCircle(b, m * 0.12f, Offset(w * 0.25f, h * 0.25f))
            drawCircle(a, m * 0.12f, center)
            drawCircle(c, m * 0.12f, Offset(w * 0.75f, h * 0.75f))
        }
        5 -> { // crescent moon
            drawCircle(b, m * 0.34f, center)
            drawCircle(Color(0xFFF6EED8), m * 0.28f, center + Offset(m * 0.16f, -m * 0.1f))
        }
        6 -> { // leaf
            val p = Path().apply {
                moveTo(w * 0.2f, h * 0.8f)
                cubicTo(w * 0.2f, h * 0.35f, w * 0.45f, h * 0.15f, w * 0.85f, h * 0.15f)
                cubicTo(w * 0.85f, h * 0.55f, w * 0.6f, h * 0.8f, w * 0.2f, h * 0.8f)
                close()
            }
            drawPath(p, c)
            drawLine(Color.White, Offset(w * 0.22f, h * 0.78f), Offset(w * 0.62f, h * 0.38f), m * 0.04f)
        }
        else -> { // drop
            val p = Path().apply {
                moveTo(w * 0.5f, h * 0.1f)
                cubicTo(w * 0.5f, h * 0.1f, w * 0.8f, h * 0.45f, w * 0.8f, h * 0.6f)
                cubicTo(w * 0.8f, h * 0.78f, w * 0.66f, h * 0.9f, w * 0.5f, h * 0.9f)
                cubicTo(w * 0.34f, h * 0.9f, w * 0.2f, h * 0.78f, w * 0.2f, h * 0.6f)
                cubicTo(w * 0.2f, h * 0.45f, w * 0.5f, h * 0.1f, w * 0.5f, h * 0.1f)
                close()
            }
            drawPath(p, b)
        }
    }
}
