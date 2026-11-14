package com.techeliteclub.game.bridges

import kotlin.random.Random

fun sizeFor(level: Int) = minOf(5 + (level - 1) / 6, 9)

/** A bridge between islands [a] and [b] (indexes into [Puzzle.islands], a < b). */
data class Link(val a: Int, val b: Int)

/**
 * [islands] are board squares (row * n + column); [need] is how many bridges each must have.
 * [solution] maps a link to 1 or 2 bridges and is one answer.
 */
class Puzzle(val n: Int, val islands: List<Int>, val need: List<Int>, val solution: Map<Link, Int>) {
    /** Every pair of islands in the same row or column with only water between them. */
    val links: List<Link> = buildList {
        for (a in islands.indices) for (b in a + 1 until islands.size) {
            val between = between(n, islands[a], islands[b]) ?: continue
            if (between.isNotEmpty() && between.none { it in islands }) add(Link(a, b))
        }
    }

    fun water(l: Link) = between(n, islands[l.a], islands[l.b])!!
}

/** Squares strictly between two squares of one row or column; null when they are not in line. */
fun between(n: Int, x: Int, y: Int): List<Int>? {
    val lo = minOf(x, y)
    val hi = maxOf(x, y)
    return when {
        lo / n == hi / n -> (lo + 1 until hi).toList()
        lo % n == hi % n -> (lo + n until hi step n).toList()
        else -> null
    }
}

fun crosses(p: Puzzle, x: Link, y: Link) = x != y && p.water(x).any { it in p.water(y) }

fun newPuzzle(level: Int, rng: Random): Puzzle {
    val n = sizeFor(level)
    val target = n * n / 5 + 2
    while (true) {
        val islands = mutableListOf(rng.nextInt(n * n))
        val used = HashSet<Int>() // water squares under a bridge
        val bridges = LinkedHashMap<Pair<Int, Int>, Int>() // by square
        var tries = 0
        while (islands.size < target && tries++ < 400) {
            val from = islands.random(rng)
            val (dr, dc) = listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0).random(rng)
            val dist = 2 + rng.nextInt(n - 2)
            val r = from / n + dr * dist
            val c = from % n + dc * dist
            if (r !in 0 until n || c !in 0 until n) continue
            val to = r * n + c
            val path = between(n, from, to)!!
            // The new island must be on open water, not touch another island, and the bridge must cross nothing.
            if (to in islands || to in used || path.any { it in islands || it in used }) continue
            val touching = listOfNotNull(if (c > 0) to - 1 else null, if (c < n - 1) to + 1 else null, if (r > 0) to - n else null, if (r < n - 1) to + n else null)
            if (touching.any { it in islands }) continue
            islands += to
            used += path
            bridges[from to to] = 1 + rng.nextInt(2)
        }
        if (islands.size < minOf(target, 5)) continue
        val sorted = islands.sorted()
        val solution = LinkedHashMap<Link, Int>()
        for ((ends, k) in bridges) {
            val a = sorted.indexOf(ends.first)
            val b = sorted.indexOf(ends.second)
            solution[Link(minOf(a, b), maxOf(a, b))] = k
        }
        val draft = Puzzle(n, sorted, List(sorted.size) { 0 }, solution)
        // A new island can land between two that were joined: such a puzzle is thrown away.
        if (solution.keys.any { it !in draft.links }) continue
        // A few extra bridges make loops, so the answer is not just a tree.
        for (l in draft.links.shuffled(rng)) {
            if (l !in solution && rng.nextInt(4) == 0 && solution.keys.none { crosses(draft, it, l) }) solution[l] = 1 + rng.nextInt(2)
        }
        val need = sorted.indices.map { i -> solution.filterKeys { it.a == i || it.b == i }.values.sum() }
        return Puzzle(n, sorted, need, solution)
    }
}

/** Bridges touching island [i]. */
fun count(bridges: Map<Link, Int>, i: Int) = bridges.filterKeys { it.a == i || it.b == i }.values.sum()

/** Next state of a link when the player uses it: none, one, two, none. Null when a new bridge would cross another. */
fun cycle(p: Puzzle, bridges: Map<Link, Int>, l: Link): Map<Link, Int>? {
    val now = bridges[l] ?: 0
    if (now == 0 && bridges.keys.any { crosses(p, it, l) }) return null
    return if (now == 2) bridges - l else bridges + (l to now + 1)
}

fun connected(p: Puzzle, bridges: Map<Link, Int>): Boolean {
    val seen = hashSetOf(0)
    val todo = ArrayDeque(listOf(0))
    while (todo.isNotEmpty()) {
        val i = todo.removeFirst()
        for (l in bridges.keys) {
            val j = when (i) { l.a -> l.b; l.b -> l.a; else -> continue }
            if (seen.add(j)) todo += j
        }
    }
    return seen.size == p.islands.size
}

/** Any layout where every island has its number and all islands are joined wins, not only the stored one. */
fun isSolved(p: Puzzle, bridges: Map<Link, Int>) = p.islands.indices.all { count(bridges, it) == p.need[it] } && connected(p, bridges)

/** Hint: make one link match the stored answer, removing bridges that cross it. */
fun hinted(p: Puzzle, bridges: Map<Link, Int>): Map<Link, Int> {
    val wrong = p.solution.entries.firstOrNull { bridges[it.key] != it.value }
    if (wrong != null) return bridges.filterKeys { !crosses(p, it, wrong.key) } + (wrong.key to wrong.value)
    val extra = bridges.keys.firstOrNull { it !in p.solution } ?: return bridges
    return bridges - extra
}

/** The neighbour of island [i] in direction ([dr], [dc]) that it can link to, or null. */
fun neighbour(p: Puzzle, i: Int, dr: Int, dc: Int): Link? = p.links.firstOrNull { l ->
    val other = when (i) { l.a -> l.b; l.b -> l.a; else -> return@firstOrNull false }
    val row = p.islands[other] / p.n - p.islands[i] / p.n
    val col = p.islands[other] % p.n - p.islands[i] % p.n
    Integer.signum(row) == dr && Integer.signum(col) == dc
}

fun encodeGame(bridges: Map<Link, Int>, seconds: Int) = bridges.entries.joinToString(",") { "${it.key.a}.${it.key.b}.${it.value}" } + ";" + seconds

fun decodeGame(text: String, p: Puzzle): Pair<Map<Link, Int>, Int> {
    val (a, s) = text.split(";")
    var bridges = emptyMap<Link, Int>()
    if (a.isNotEmpty()) for (part in a.split(",")) {
        val (x, y, k) = part.split(".").map { it.toInt() }
        val l = Link(x, y)
        require(l in p.links && k in 1..2 && l !in bridges)
        repeat(k) { bridges = requireNotNull(cycle(p, bridges, l)) }
    }
    return bridges to s.toInt()
}
