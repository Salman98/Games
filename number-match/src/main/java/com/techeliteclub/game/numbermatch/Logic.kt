package com.techeliteclub.game.numbermatch

import kotlin.math.abs
import kotlin.random.Random

const val COLS = 9
const val START_ADDS = 4

/** Starting numbers: three rows on early levels, up to six later. */
fun countFor(level: Int) = COLS * (3 + minOf((level - 1) / 3, 3))

fun newBoard(level: Int, rng: Random): List<Int> = List(countFor(level)) { 1 + rng.nextInt(9) }

/**
 * Two numbers can be crossed out when they are equal or add to 10, and nothing but crossed-out squares (0)
 * lies between them: along a row, column or diagonal, or in reading order (end of a row wraps to the next row).
 */
fun canPair(d: List<Int>, a: Int, b: Int): Boolean {
    if (a == b || a !in d.indices || b !in d.indices || d[a] == 0 || d[b] == 0) return false
    if (d[a] != d[b] && d[a] + d[b] != 10) return false
    val lo = minOf(a, b)
    val hi = maxOf(a, b)
    if ((lo + 1 until hi).all { d[it] == 0 }) return true
    val dr = hi / COLS - lo / COLS
    val dc = hi % COLS - lo % COLS
    if (dc != 0 && abs(dc) != dr) return false
    val step = dr / dr * COLS + (if (dc == 0) 0 else dc / abs(dc))
    var i = lo + step
    while (i != hi) {
        if (d[i] != 0) return false
        i += step
    }
    return true
}

/** Crosses out [a] and [b], then removes every row that is now empty. Returns the new board and how many rows went. */
fun cross(d: List<Int>, a: Int, b: Int): Pair<List<Int>, Int> {
    val next = d.toMutableList()
    next[a] = 0
    next[b] = 0
    val rows = next.chunked(COLS)
    val kept = rows.filter { row -> row.any { it != 0 } }
    return kept.flatten() to rows.size - kept.size
}

/** Adds a copy of every number still on the board to the end. */
fun addNumbers(d: List<Int>) = d + d.filter { it != 0 }

fun findPair(d: List<Int>): Pair<Int, Int>? {
    for (a in d.indices) if (d[a] != 0) for (b in a + 1 until d.size) if (canPair(d, a, b)) return a to b
    return null
}

fun isCleared(d: List<Int>) = d.none { it != 0 }

/** Board, adds left, score, seconds. */
class Game(val board: List<Int>, val adds: Int, val score: Int, val seconds: Int)

fun encodeGame(g: Game) = g.board.joinToString("") + ";" + g.adds + ";" + g.score + ";" + g.seconds

fun decodeGame(text: String): Game {
    val (b, a, s, t) = text.split(";")
    val board = b.map { it.digitToInt() }
    require(a.toInt() in 0..START_ADDS)
    return Game(board, a.toInt(), s.toInt(), t.toInt())
}
