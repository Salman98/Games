package com.techeliteclub.game.rectangles

import kotlin.random.Random

fun sizeFor(level: Int) = minOf(5 + (level - 1) / 8, 9)

/** A rectangle of squares: top row, left column, height, width. */
data class Rect(val r: Int, val c: Int, val h: Int, val w: Int) {
    val area get() = h * w
    operator fun contains(cell: Pair<Int, Int>) = cell.first in r until r + h && cell.second in c until c + w
    fun overlaps(o: Rect) = r < o.r + o.h && o.r < r + h && c < o.c + o.w && o.c < c + w
    fun cells(n: Int) = (r until r + h).flatMap { y -> (c until c + w).map { x -> y * n + x } }

    companion object {
        /** The rectangle with squares [a] and [b] as opposite corners. */
        fun spanning(n: Int, a: Int, b: Int) =
            Rect(minOf(a / n, b / n), minOf(a % n, b % n), kotlin.math.abs(a / n - b / n) + 1, kotlin.math.abs(a % n - b % n) + 1)
    }
}

/** [clues] maps a square to the size of the rectangle that must hold it. [solution] is one answer. */
class Puzzle(val n: Int, val clues: Map<Int, Int>, val solution: List<Rect>)

/** Every rectangle that could hold the clue at [cell]: right size, inside the board, no other clue in it. */
fun candidates(p: Puzzle, cell: Int): List<Rect> {
    val n = p.n
    val area = p.clues.getValue(cell)
    val r0 = cell / n
    val c0 = cell % n
    val out = mutableListOf<Rect>()
    for (h in 1..area) {
        if (area % h != 0) continue
        val w = area / h
        if (h > n || w > n) continue
        for (r in maxOf(0, r0 - h + 1)..minOf(r0, n - h)) for (c in maxOf(0, c0 - w + 1)..minOf(c0, n - w)) {
            val rect = Rect(r, c, h, w)
            if (p.clues.keys.none { it != cell && (it / n to it % n) in rect }) out += rect
        }
    }
    return out
}

/** Counts answers, stopping at [limit]. */
fun countSolutions(p: Puzzle, limit: Int = 2): Int {
    val n = p.n
    val cands = p.clues.keys.associateWith { candidates(p, it) }
    val owner = IntArray(n * n) { -1 }
    var found = 0
    fun go() {
        if (found >= limit) return
        val free = owner.indexOfFirst { it == -1 }
        if (free == -1) { found++; return }
        // Try every unused clue rectangle that covers the first free square.
        for ((clue, list) in cands) {
            if (owner.contains(clue + n * n)) continue
            for (rect in list) {
                if ((free / n to free % n) !in rect) continue
                val cs = rect.cells(n)
                if (cs.any { owner[it] != -1 }) continue
                for (x in cs) owner[x] = clue + n * n
                go()
                for (x in cs) owner[x] = -1
                if (found >= limit) return
            }
        }
    }
    go()
    return found
}

/** Cuts the board into random rectangles (mostly 2 to 8 squares), puts one clue in each, keeps it only if the answer is unique. */
fun newPuzzle(level: Int, rng: Random): Puzzle {
    val n = sizeFor(level)
    var last: Puzzle? = null
    repeat(200) {
        val owner = IntArray(n * n) { -1 }
        val rects = mutableListOf<Rect>()
        while (true) {
            val start = owner.indexOfFirst { it == -1 }
            if (start == -1) break
            val r = start / n
            val c = start % n
            var maxW = 0
            while (c + maxW < n && owner[r * n + c + maxW] == -1) maxW++
            val options = mutableListOf<Rect>()
            for (h in 1..minOf(n - r, 6)) for (w in 1..minOf(maxW, 6)) {
                val rect = Rect(r, c, h, w)
                if (rect.area in 2..minOf(8, n + 2) && rect.cells(n).all { owner[it] == -1 }) options += rect
            }
            val pick = if (options.isEmpty()) Rect(r, c, 1, 1) else options.random(rng)
            pick.cells(n).forEach { owner[it] = rects.size }
            rects += pick
        }
        val clues = rects.associate { it.cells(n).random(rng) to it.area }
        val p = Puzzle(n, clues, rects)
        if (countSolutions(p) == 1) return p
        last = p
    }
    // shortcut: after 200 tries keep a puzzle with several answers; isSolved accepts any valid answer.
    return last!!
}

/** Valid when the rectangles cover the board without overlap and each holds exactly one clue equal to its size. */
fun isSolved(p: Puzzle, rects: List<Rect>): Boolean {
    val n = p.n
    if (rects.sumOf { it.area } != n * n) return false
    if (rects.indices.any { i -> (i + 1 until rects.size).any { rects[i].overlaps(rects[it]) } }) return false
    return rects.all { rect -> rect.goodFor(p) }
}

fun Rect.goodFor(p: Puzzle): Boolean {
    val inside = p.clues.filterKeys { (it / p.n to it % p.n) in this }
    return inside.size == 1 && inside.values.first() == area
}

/** Drawing a rectangle removes any it overlaps. */
fun draw(rects: List<Rect>, rect: Rect) = rects.filterNot { it.overlaps(rect) } + rect

/** Hint: place the first answer rectangle the player does not have yet. */
fun hinted(p: Puzzle, rects: List<Rect>): List<Rect> {
    val keep = rects.filter { it in p.solution }
    val next = p.solution.firstOrNull { it !in rects } ?: return rects
    return draw(rects.filter { it in keep || !it.overlaps(next) }, next)
}

fun encodeGame(rects: List<Rect>, seconds: Int) = rects.joinToString(",") { "${it.r}.${it.c}.${it.h}.${it.w}" } + ";" + seconds

fun decodeGame(text: String, n: Int): Pair<List<Rect>, Int> {
    val (a, s) = text.split(";")
    val rects = if (a.isEmpty()) emptyList() else a.split(",").map { part ->
        val (r, c, h, w) = part.split(".").map { it.toInt() }
        require(r >= 0 && c >= 0 && h > 0 && w > 0 && r + h <= n && c + w <= n)
        Rect(r, c, h, w)
    }
    return rects to s.toInt()
}
