package com.salman98.games.sudoku

import kotlin.random.Random

/** Grids are 81 cells, row-major; 0 = empty. */
private fun box(i: Int) = (i / 27) * 3 + (i % 9) / 3

private fun allowed(g: IntArray, i: Int, d: Int): Boolean {
    val r = i / 9
    val c = i % 9
    for (k in 0 until 9) {
        if (g[r * 9 + k] == d || g[k * 9 + c] == d) return false
    }
    val br = (r / 3) * 3
    val bc = (c / 3) * 3
    for (rr in br until br + 3) for (cc in bc until bc + 3) if (g[rr * 9 + cc] == d) return false
    return true
}

/** Counts solutions up to [limit]; fills [g] with the first one found if [keep] is true. */
fun countSolutions(start: IntArray, limit: Int = 2, rng: Random? = null, keep: Boolean = false): Int {
    val g = start.copyOf()
    var count = 0
    fun go(): Boolean {
        val i = g.indexOf(0)
        if (i < 0) { count++; if (keep) g.copyInto(start); return count >= limit }
        val digits = (1..9).let { if (rng != null) it.shuffled(rng) else it.toList() }
        for (d in digits) {
            if (!allowed(g, i, d)) continue
            g[i] = d
            if (go()) return true
            g[i] = 0
        }
        return false
    }
    go()
    return count
}

fun holesFor(level: Int) = minOf(30 + level * 2, 56)

class Puzzle(val givens: IntArray, val solution: IntArray)

fun newPuzzle(level: Int, rng: Random): Puzzle {
    val solution = IntArray(81)
    countSolutions(solution, limit = 1, rng = rng, keep = true)
    val puzzle = solution.copyOf()
    var holes = 0
    for (i in (0 until 81).shuffled(rng)) {
        if (holes >= holesFor(level)) break
        val keepDigit = puzzle[i]
        puzzle[i] = 0
        if (countSolutions(puzzle) == 1) holes++ else puzzle[i] = keepDigit // keep the answer unique
    }
    return Puzzle(puzzle, solution)
}

/** Cells whose digit repeats in their row, column or 3x3 box. */
fun conflicts(g: IntArray): Set<Int> = (0 until 81).filter { i ->
    g[i] != 0 && (0 until 81).any { j ->
        j != i && g[j] == g[i] && (j / 9 == i / 9 || j % 9 == i % 9 || box(j) == box(i))
    }
}.toSet()

fun isSolved(g: IntArray, solution: IntArray) = g.contentEquals(solution)
