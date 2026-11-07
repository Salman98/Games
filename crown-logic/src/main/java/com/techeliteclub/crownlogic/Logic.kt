package com.techeliteclub.crownlogic

import kotlin.math.abs
import kotlin.random.Random

fun sizeFor(level: Int) = minOf(5 + (level - 1) / 4, 9)

/** [regions] holds a region id (0 until n) per cell, row-major. [solution] holds the crown column for each row. */
class Puzzle(val n: Int, val regions: IntArray, val solution: IntArray)

fun newPuzzle(level: Int, rng: Random): Puzzle {
    val n = sizeFor(level)
    var last: Puzzle? = null
    repeat(20) {
        val sol = randomSolution(n, rng)
        val reg = growRegions(n, sol, rng)
        val p = Puzzle(n, reg, sol)
        // Break other answers: move a cell of a rival answer into a neighbouring region, until one answer is left.
        repeat(300) {
            val alt = solutions(p, 2).firstOrNull { !it.contentEquals(sol) } ?: return p
            val cells = (0 until n).filter { alt[it] != sol[it] }.map { it * n + alt[it] }.shuffled(rng)
            for (x in cells) {
                val owners = neighbours(n, x).map { reg[it] }.filter { it != reg[x] }.distinct()
                if (owners.isEmpty() || !stillConnectedWithout(n, reg, x)) continue
                reg[x] = owners.random(rng)
                break
            }
        }
        last = p
    }
    return last!! // rare: may have more than one answer; any valid answer is accepted
}

/** True if region of [x] stays in one piece when [x] leaves it. */
private fun stillConnectedWithout(n: Int, reg: IntArray, x: Int): Boolean {
    val cells = (0 until n * n).filter { it != x && reg[it] == reg[x] }
    if (cells.isEmpty()) return false
    val seen = hashSetOf(cells[0])
    val queue = ArrayDeque(listOf(cells[0]))
    while (queue.isNotEmpty()) {
        for (nb in neighbours(n, queue.removeFirst())) {
            if (nb != x && reg[nb] == reg[x] && seen.add(nb)) queue.add(nb)
        }
    }
    return seen.size == cells.size
}

private fun randomSolution(n: Int, rng: Random): IntArray {
    val cols = IntArray(n)
    fun fill(r: Int, used: Set<Int>): Boolean {
        if (r == n) return true
        for (c in (0 until n).shuffled(rng)) {
            if (c in used || (r > 0 && abs(c - cols[r - 1]) <= 1)) continue
            cols[r] = c
            if (fill(r + 1, used + c)) return true
        }
        return false
    }
    check(fill(0, emptySet())) { "no layout for n=$n" }
    return cols
}

/** Each region starts at its row's crown and grows into random neighbouring empty cells. */
private fun growRegions(n: Int, sol: IntArray, rng: Random): IntArray {
    val reg = IntArray(n * n) { -1 }
    for (r in 0 until n) reg[r * n + sol[r]] = r
    while (reg.any { it < 0 }) {
        val i = (0 until n * n).filter { reg[it] < 0 }.random(rng)
        val owners = neighbours(n, i).map { reg[it] }.filter { it >= 0 }
        if (owners.isNotEmpty()) reg[i] = owners.random(rng)
    }
    return reg
}

private fun neighbours(n: Int, i: Int): List<Int> {
    val r = i / n
    val c = i % n
    return listOfNotNull(
        if (r > 0) i - n else null, if (r < n - 1) i + n else null,
        if (c > 0) i - 1 else null, if (c < n - 1) i + 1 else null,
    )
}

/** Up to [limit] answers, each as the crown column per row. */
fun solutions(p: Puzzle, limit: Int): List<IntArray> {
    val n = p.n
    val found = ArrayList<IntArray>()
    val cols = IntArray(n)
    fun go(r: Int, usedCols: Int, usedRegs: Int) {
        if (found.size >= limit) return
        if (r == n) { found += cols.copyOf(); return }
        for (c in 0 until n) {
            val g = p.regions[r * n + c]
            if (usedCols and (1 shl c) != 0 || usedRegs and (1 shl g) != 0 || (r > 0 && abs(c - cols[r - 1]) <= 1)) continue
            cols[r] = c
            go(r + 1, usedCols or (1 shl c), usedRegs or (1 shl g))
        }
    }
    go(0, 0, 0)
    return found
}

/** Crowns (cell indexes) that break a rule: same row, column or region, or touching. */
fun conflicts(p: Puzzle, crowns: Collection<Int>): Set<Int> {
    val n = p.n
    val bad = HashSet<Int>()
    for (a in crowns) for (b in crowns) {
        if (a >= b) continue
        val ra = a / n; val ca = a % n; val rb = b / n; val cb = b % n
        if (ra == rb || ca == cb || p.regions[a] == p.regions[b] || (abs(ra - rb) <= 1 && abs(ca - cb) <= 1)) {
            bad += a; bad += b
        }
    }
    return bad
}

fun isSolved(p: Puzzle, crowns: Collection<Int>) = crowns.size == p.n && conflicts(p, crowns).isEmpty()
