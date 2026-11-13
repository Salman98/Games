package com.techeliteclub.game.minisudoku

import kotlin.random.Random

const val N = 6
const val CELLS = N * N

/** Grids are 36 cells, row-major; 0 = empty. Boxes are 2 rows by 3 columns. */
fun box(i: Int) = (i / N / 2) * 2 + (i % N) / 3

private fun allowed(g: IntArray, i: Int, d: Int) =
    (0 until CELLS).none { j -> g[j] == d && (j / N == i / N || j % N == i % N || box(j) == box(i)) }

/** Counts solutions up to [limit]; fills [start] with the first one found if [keep] is true. */
fun countSolutions(start: IntArray, limit: Int = 2, rng: Random? = null, keep: Boolean = false): Int {
    val g = start.copyOf()
    var count = 0
    fun go(): Boolean {
        val i = g.indexOf(0)
        if (i < 0) { count++; if (keep) g.copyInto(start); return count >= limit }
        val digits = (1..N).let { if (rng != null) it.shuffled(rng) else it.toList() }
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

fun holesFor(level: Int) = minOf(16 + level / 2, 24)

class Puzzle(val givens: IntArray, val solution: IntArray)

fun newPuzzle(level: Int, rng: Random): Puzzle {
    val solution = IntArray(CELLS)
    countSolutions(solution, limit = 1, rng = rng, keep = true)
    val puzzle = solution.copyOf()
    var holes = 0
    for (i in (0 until CELLS).shuffled(rng)) {
        if (holes >= holesFor(level)) break
        val keepDigit = puzzle[i]
        puzzle[i] = 0
        if (countSolutions(puzzle) == 1) holes++ else puzzle[i] = keepDigit // keep the answer unique
    }
    return Puzzle(puzzle, solution)
}

/** Cells whose digit repeats in their row, column or box. */
fun conflicts(g: IntArray): Set<Int> = (0 until CELLS).filter { i ->
    g[i] != 0 && (0 until CELLS).any { j ->
        j != i && g[j] == g[i] && (j / N == i / N || j % N == i % N || box(j) == box(i))
    }
}.toSet()

fun isSolved(g: IntArray, solution: IntArray) = g.contentEquals(solution)

class SavedLevel(val grid: IntArray, val notes: Map<Int, Set<Int>>, val mistakes: Int, val seconds: Int)

/** Saved level as text: "36 digits;cell:notes,cell:notes;mistakes;seconds". */
fun encodeGame(grid: IntArray, notes: Map<Int, Set<Int>>, mistakes: Int, seconds: Int) =
    grid.joinToString("") + ";" + notes.entries.joinToString(",") { (i, d) -> "$i:" + d.joinToString("") } + ";" + mistakes + ";" + seconds

/** Throws when the text is damaged or does not fit [givens]. */
fun decodeGame(text: String, givens: IntArray): SavedLevel {
    val (g, n, m, s) = text.split(";")
    val grid = g.map { it.digitToInt() }.toIntArray()
    require(grid.size == CELLS && givens.indices.all { givens[it] == 0 || givens[it] == grid[it] })
    val notes = if (n.isEmpty()) emptyMap() else n.split(",").associate { e ->
        e.split(":").let { it[0].toInt() to it[1].map { ch -> ch.digitToInt() }.toSet() }
    }
    return SavedLevel(grid, notes, m.toInt(), s.toInt())
}
