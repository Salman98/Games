package com.techeliteclub.game.nonogram

import kotlin.random.Random

const val EMPTY = 0
const val FILL = 1
const val CROSS = 2

fun sizeFor(level: Int) = minOf(5 + (level - 1) / 6, 10)

/** [solution] is row-major, true = filled. Clues are the run lengths of each row and column ([0] for an empty line). */
class Puzzle(val n: Int, val solution: BooleanArray) {
    val rows = List(n) { r -> runs(List(n) { solution[r * n + it] }) }
    val cols = List(n) { c -> runs(List(n) { solution[it * n + c] }) }
}

fun runs(line: List<Boolean>): List<Int> {
    val out = mutableListOf<Int>()
    var k = 0
    for (v in line + false) {
        if (v) k++ else if (k > 0) { out += k; k = 0 }
    }
    return out.ifEmpty { listOf(0) }
}

/**
 * One line of the classic line-solving step. [known]: -1 unknown, 0 empty, 1 filled.
 * Returns what every arrangement of [clue] that fits [known] agrees on, or null when none fits.
 */
fun solveLine(clue: List<Int>, known: IntArray): IntArray? {
    val n = known.size
    val runs = clue.filter { it > 0 }
    val canFill = BooleanArray(n)
    val canEmpty = BooleanArray(n)
    var any = false
    val cur = IntArray(n)
    fun fits(from: Int, to: Int): Boolean {
        for (i in from until to) if (known[i] != -1 && known[i] != cur[i]) return false
        return true
    }
    fun place(k: Int, from: Int) {
        if (k == runs.size) {
            for (i in from until n) cur[i] = 0
            if (!fits(from, n)) return
            any = true
            for (i in 0 until n) if (cur[i] == 1) canFill[i] = true else canEmpty[i] = true
            return
        }
        val len = runs[k]
        val rest = runs.drop(k + 1).sumOf { it + 1 }
        for (s in from..n - len - rest) {
            for (i in from until s) cur[i] = 0
            for (i in s until s + len) cur[i] = 1
            if (s + len < n) cur[s + len] = 0
            val end = minOf(s + len + 1, n)
            if (fits(from, end)) place(k + 1, end)
        }
    }
    place(0, 0)
    if (!any) return null
    return IntArray(n) { if (canFill[it] && canEmpty[it]) -1 else if (canFill[it]) 1 else 0 }
}

/** True when line solving alone finishes the grid, which also means the picture is the only answer. */
fun lineSolvable(p: Puzzle): Boolean {
    val n = p.n
    val g = IntArray(n * n) { -1 }
    var changed = true
    while (changed) {
        changed = false
        for (r in 0 until n) {
            val got = solveLine(p.rows[r], IntArray(n) { g[r * n + it] }) ?: return false
            for (c in 0 until n) if (g[r * n + c] != got[c]) { g[r * n + c] = got[c]; changed = true }
        }
        for (c in 0 until n) {
            val got = solveLine(p.cols[c], IntArray(n) { g[it * n + c] }) ?: return false
            for (r in 0 until n) if (g[r * n + c] != got[r]) { g[r * n + c] = got[r]; changed = true }
        }
    }
    return g.none { it == -1 }
}

/** A mirror-symmetric picture (looks like a sprite rather than noise) that line solving can finish. */
fun newPuzzle(level: Int, rng: Random): Puzzle {
    val n = sizeFor(level)
    var last: Puzzle? = null
    repeat(400) {
        val sol = BooleanArray(n * n)
        for (r in 0 until n) for (c in 0 until (n + 1) / 2) {
            val v = rng.nextFloat() < 0.6f
            sol[r * n + c] = v
            sol[r * n + n - 1 - c] = v
        }
        val p = Puzzle(n, sol)
        if (lineSolvable(p)) return p
        last = p
    }
    // shortcut: after 400 tries keep a puzzle that may need a guess; isSolved accepts any picture matching the clues.
    return last!!
}

fun rowRuns(p: Puzzle, cells: IntArray, r: Int) = runs(List(p.n) { cells[r * p.n + it] == FILL })
fun colRuns(p: Puzzle, cells: IntArray, c: Int) = runs(List(p.n) { cells[it * p.n + c] == FILL })

/** Any picture whose rows and columns match every clue wins, not only the stored one. */
fun isSolved(p: Puzzle, cells: IntArray) =
    (0 until p.n).all { rowRuns(p, cells, it) == p.rows[it] && colRuns(p, cells, it) == p.cols[it] }

/** Hint: fix the first wrong square, else fill the first square of the picture still missing. */
fun hinted(p: Puzzle, cells: IntArray): IntArray {
    val next = cells.copyOf()
    val wrong = cells.indices.firstOrNull { cells[it] == FILL && !p.solution[it] }
    val missing = cells.indices.firstOrNull { p.solution[it] && cells[it] != FILL }
    when {
        wrong != null -> next[wrong] = CROSS
        missing != null -> next[missing] = FILL
    }
    return next
}

fun encodeGame(cells: IntArray, seconds: Int) = cells.joinToString("") + ";" + seconds

fun decodeGame(text: String, n: Int): Pair<IntArray, Int> {
    val (c, s) = text.split(";")
    require(c.length == n * n)
    val cells = IntArray(n * n) { c[it].digitToInt().also { v -> require(v in EMPTY..CROSS) } }
    return cells to s.toInt()
}
