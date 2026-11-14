package com.techeliteclub.game.dominologic

import kotlin.random.Random

/** Board size by level: 3×4 (6 dominoes), then 4×4, 4×5 and 4×6. */
fun sizeFor(level: Int): Pair<Int, Int> = when {
    level <= 5 -> 3 to 4
    level <= 15 -> 4 to 4
    level <= 30 -> 4 to 5
    else -> 4 to 6
}

enum class Rule { NONE, SUM, EQUAL, DIFFERENT, LESS, MORE }

/** A coloured area of squares and what their pips must do. [n] is the target for SUM, LESS and MORE. */
class Region(val cells: List<Int>, val rule: Rule, val n: Int = 0) {
    val label get() = when (rule) { Rule.NONE -> ""; Rule.SUM -> "$n"; Rule.EQUAL -> "="; Rule.DIFFERENT -> "≠"; Rule.LESS -> "<$n"; Rule.MORE -> ">$n" }

    /** [pips] is the value on each square, or null where nothing is placed. An unfinished region is not ok. */
    fun ok(pips: (Int) -> Int?): Boolean {
        val v = cells.map { pips(it) ?: return false }
        return when (rule) {
            Rule.NONE -> true
            Rule.SUM -> v.sum() == n
            Rule.EQUAL -> v.toSet().size == 1
            Rule.DIFFERENT -> v.toSet().size == v.size
            Rule.LESS -> v.sum() < n
            Rule.MORE -> v.sum() > n
        }
    }
}

/** A domino lying on the board: tile [id] with its first half on square [a] and its second on [b]. */
data class Placed(val id: Int, val a: Int, val b: Int)

/** [tiles] are the dominoes to place (first half, second half). [solution] is one way that satisfies every region. */
class Puzzle(val rows: Int, val cols: Int, val tiles: List<Pair<Int, Int>>, val regions: List<Region>, val solution: List<Placed>) {
    val regionOf = IntArray(rows * cols).also { m -> regions.forEachIndexed { k, r -> r.cells.forEach { m[it] = k } } }
}

fun adjacent(cols: Int, a: Int, b: Int) = (a / cols == b / cols && kotlin.math.abs(a - b) == 1) || kotlin.math.abs(a - b) == cols

private fun neighbours(rows: Int, cols: Int, i: Int) = listOfNotNull(
    if (i % cols > 0) i - 1 else null, if (i % cols < cols - 1) i + 1 else null,
    if (i >= cols) i - cols else null, if (i < (rows - 1) * cols) i + cols else null,
)

/** Random way to cover the board with dominoes: always pair the first free square with a free right or lower neighbour. */
private fun tiling(rows: Int, cols: Int, rng: Random): List<Pair<Int, Int>>? {
    val used = BooleanArray(rows * cols)
    val out = mutableListOf<Pair<Int, Int>>()
    for (i in used.indices) {
        if (used[i]) continue
        val options = listOfNotNull(if (i % cols < cols - 1 && !used[i + 1]) i + 1 else null, if (i + cols < used.size && !used[i + cols]) i + cols else null)
        val j = options.randomOrNull(rng) ?: return null
        used[i] = true
        used[j] = true
        out += i to j
    }
    return out
}

fun newPuzzle(level: Int, rng: Random): Puzzle {
    val (rows, cols) = sizeFor(level)
    var cover: List<Pair<Int, Int>>? = null
    while (cover == null) cover = tiling(rows, cols, rng)
    // Every tile is different, like a real domino set (0-0 up to 6-6).
    val set = (0..6).flatMap { a -> (a..6).map { b -> a to b } }.shuffled(rng).take(cover.size)
    val tiles = set.map { (a, b) -> if (rng.nextBoolean()) a to b else b to a }
    val solution = cover.mapIndexed { id, (a, b) -> Placed(id, a, b) }
    val pips = IntArray(rows * cols)
    for (s in solution) { pips[s.a] = tiles[s.id].first; pips[s.b] = tiles[s.id].second }

    // Grow regions of 1 to 3 squares, then give each the rule its pips happen to satisfy.
    val regionOf = IntArray(rows * cols) { -1 }
    val regions = mutableListOf<Region>()
    for (start in (0 until rows * cols).shuffled(rng)) {
        if (regionOf[start] != -1) continue
        val cells = mutableListOf(start)
        regionOf[start] = regions.size
        repeat(rng.nextInt(3)) {
            val next = cells.flatMap { neighbours(rows, cols, it) }.filter { regionOf[it] == -1 }.randomOrNull(rng)
            if (next != null) { cells += next; regionOf[next] = regions.size }
        }
        val v = cells.map { pips[it] }
        val sum = v.sum()
        val rule = when {
            cells.size > 1 && v.toSet().size == 1 -> Region(cells, Rule.EQUAL)
            rng.nextInt(6) == 0 -> Region(cells, Rule.NONE)
            cells.size > 1 && v.toSet().size == v.size && rng.nextInt(4) == 0 -> Region(cells, Rule.DIFFERENT)
            rng.nextInt(5) == 0 && sum > 0 -> Region(cells, Rule.LESS, sum + 1 + rng.nextInt(2))
            rng.nextInt(5) == 0 && sum > 1 -> Region(cells, Rule.MORE, sum - 1 - rng.nextInt(minOf(2, sum - 1)))
            else -> Region(cells, Rule.SUM, sum)
        }
        regions += rule
    }
    return Puzzle(rows, cols, tiles, regions, solution)
}

/** Pips on each square under [placed], null where empty. */
fun pipsOf(p: Puzzle, placed: List<Placed>): Array<Int?> {
    val out = arrayOfNulls<Int>(p.rows * p.cols)
    for (s in placed) { out[s.a] = p.tiles[s.id].first; out[s.b] = p.tiles[s.id].second }
    return out
}

/** Any full layout that satisfies every region wins, not only the stored one. */
fun isSolved(p: Puzzle, placed: List<Placed>): Boolean {
    val pips = pipsOf(p, placed)
    return placed.size == p.tiles.size && p.regions.all { r -> r.ok { pips[it] } }
}

/** Puts tile [id] on [a] and [b] ([flipped] swaps its halves). Null when the squares are not free neighbours or the tile is in use. */
fun place(p: Puzzle, placed: List<Placed>, id: Int, a: Int, b: Int, flipped: Boolean): List<Placed>? {
    if (!adjacent(p.cols, a, b) || placed.any { it.id == id || it.a == a || it.b == a || it.a == b || it.b == b }) return null
    return placed + if (flipped) Placed(id, b, a) else Placed(id, a, b)
}

/** Hint: put the next domino of the stored answer where it belongs, lifting whatever is in its way. */
fun hinted(p: Puzzle, placed: List<Placed>): List<Placed> {
    val next = p.solution.firstOrNull { it !in placed } ?: return placed
    return placed.filterNot { it.id == next.id || it.a == next.a || it.b == next.a || it.a == next.b || it.b == next.b } + next
}

fun encodeGame(placed: List<Placed>, seconds: Int) = placed.joinToString(",") { "${it.id}.${it.a}.${it.b}" } + ";" + seconds

fun decodeGame(text: String, p: Puzzle): Pair<List<Placed>, Int> {
    val (a, s) = text.split(";")
    var placed = emptyList<Placed>()
    if (a.isNotEmpty()) for (part in a.split(",")) {
        val (id, x, y) = part.split(".").map { it.toInt() }
        require(id in p.tiles.indices && x in 0 until p.rows * p.cols && y in 0 until p.rows * p.cols)
        placed = requireNotNull(place(p, placed, id, x, y, false))
    }
    return placed to s.toInt()
}
