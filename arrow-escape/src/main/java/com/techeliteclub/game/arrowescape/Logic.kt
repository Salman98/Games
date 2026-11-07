package com.techeliteclub.game.arrowescape

import kotlin.random.Random

enum class Dir(val dr: Int, val dc: Int, val symbol: String) {
    UP(-1, 0, "↑"), DOWN(1, 0, "↓"), LEFT(0, -1, "←"), RIGHT(0, 1, "→")
}

fun sizeFor(level: Int) = minOf(5 + (level - 1) / 5, 9)

/** Board is n*n cells, row-major; null = empty. */
fun pathClear(board: List<Dir?>, n: Int, i: Int, d: Dir): Boolean {
    var r = i / n + d.dr
    var c = i % n + d.dc
    while (r in 0 until n && c in 0 until n) {
        if (board[r * n + c] != null) return false
        r += d.dr; c += d.dc
    }
    return true
}

fun canExit(board: List<Dir?>, n: Int, i: Int): Boolean {
    val d = board[i] ?: return false
    return pathClear(board, n, i, d)
}

/**
 * Built in reverse: each new arrow must have a clear path past the arrows already placed.
 * Removing them in reverse order always works, and removing an arrow never blocks another, so no level can get stuck.
 */
fun newLevel(level: Int, rng: Random): List<Dir?> {
    val n = sizeFor(level)
    val board = MutableList<Dir?>(n * n) { null }
    val target = (n * n * 0.7).toInt()
    var placed = 0
    repeat(n * n * 30) {
        if (placed >= target) return board
        val i = rng.nextInt(n * n)
        val d = Dir.entries.random(rng)
        if (board[i] == null && pathClear(board, n, i, d)) { board[i] = d; placed++ }
    }
    return board
}
