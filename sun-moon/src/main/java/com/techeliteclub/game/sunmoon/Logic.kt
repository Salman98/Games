package com.techeliteclub.game.sunmoon

import kotlin.random.Random

const val EMPTY = 0
const val SUN = 1
const val MOON = 2

fun sizeFor(level: Int) = if (level <= 3) 4 else 6

/** A sign between cell [a] and the cell to its right or below, [b]: the two must be the same, or must differ. */
class Link(val a: Int, val b: Int, val same: Boolean)

/** Grids are n x n cells, row-major. [givens] are the fixed cells (EMPTY elsewhere). */
class Puzzle(val n: Int, val givens: IntArray, val links: List<Link>, val solution: IntArray)

private fun lines(n: Int): List<List<Int>> =
    (0 until n).map { r -> (0 until n).map { r * n + it } } + (0 until n).map { c -> (0 until n).map { it * n + c } }

/** Cells that break a rule: more than half a line the same, three the same in a row, or a broken sign. */
fun violations(n: Int, g: IntArray, links: List<Link>): Set<Int> {
    val bad = HashSet<Int>()
    for (line in lines(n)) {
        for (v in SUN..MOON) if (line.count { g[it] == v } > n / 2) bad += line.filter { g[it] == v }
        for (k in 0..n - 3) {
            val t = line.subList(k, k + 3)
            if (g[t[0]] != EMPTY && g[t[0]] == g[t[1]] && g[t[1]] == g[t[2]]) bad += t
        }
    }
    for (l in links) if (g[l.a] != EMPTY && g[l.b] != EMPTY && (g[l.a] == g[l.b]) != l.same) { bad += l.a; bad += l.b }
    return bad
}

/** Up to [limit] full grids that extend [start] and break no rule. */
fun solutions(n: Int, start: IntArray, links: List<Link>, limit: Int, rng: Random? = null): List<IntArray> {
    val g = start.copyOf()
    val found = ArrayList<IntArray>()
    fun go() {
        if (found.size >= limit) return
        val i = g.indexOf(EMPTY)
        if (i < 0) { found += g.copyOf(); return }
        for (v in if (rng != null) listOf(SUN, MOON).shuffled(rng) else listOf(SUN, MOON)) {
            g[i] = v
            if (violations(n, g, links).isEmpty()) go()
        }
        g[i] = EMPTY
    }
    go()
    return found
}

/** Fixed cells to aim for; fewer on later levels. The puzzle keeps more when it needs them for a single answer. */
fun givensFor(level: Int, n: Int) = maxOf(n, n * n / 3 - level / 4)

fun newPuzzle(level: Int, rng: Random): Puzzle {
    val n = sizeFor(level)
    val solution = solutions(n, IntArray(n * n), emptyList(), 1, rng).first()
    val pairs = (0 until n * n).flatMap { i ->
        listOfNotNull(if (i % n < n - 1) i to i + 1 else null, if (i / n < n - 1) i to i + n else null)
    }
    val links = pairs.shuffled(rng).take(if (n == 4) 3 else minOf(6 + level / 5, 10)).map { (a, b) -> Link(a, b, solution[a] == solution[b]) }
    val givens = solution.copyOf()
    for (i in (0 until n * n).shuffled(rng)) {
        if (givens.count { it != EMPTY } <= givensFor(level, n)) break
        val keep = givens[i]
        givens[i] = EMPTY
        if (solutions(n, givens, links, 2).size != 1) givens[i] = keep // keep the answer unique
    }
    return Puzzle(n, givens, links, solution)
}

fun isSolved(p: Puzzle, g: IntArray) = g.none { it == EMPTY } && violations(p.n, g, p.links).isEmpty()

/** Saved level as text: one digit per cell, then the seconds played. */
fun encodeGame(g: IntArray, seconds: Int) = g.joinToString("") + ";" + seconds

/** Throws when the text is damaged or does not fit the puzzle's fixed cells. */
fun decodeGame(text: String, p: Puzzle): Pair<IntArray, Int> {
    val (c, s) = text.split(";")
    val g = c.map { it.digitToInt() }.toIntArray()
    require(g.size == p.n * p.n && g.all { it in EMPTY..MOON } && g.indices.all { p.givens[it] == EMPTY || p.givens[it] == g[it] })
    return g to s.toInt()
}
