package com.techeliteclub.game.numberpath

import kotlin.math.abs
import kotlin.random.Random

fun sizeFor(level: Int) = minOf(4 + (level - 1) / 5, 7)

/** How many numbered squares; fewer on later levels leaves more of the route to work out. */
fun stopsFor(level: Int, n: Int) = maxOf(4, n + 3 - level / 8)

/** [numbers] holds 1..[stops] on the numbered squares and 0 elsewhere (row-major). [solution] is one route that works. */
class Puzzle(val n: Int, val numbers: IntArray, val stops: Int, val solution: IntArray)

fun adjacent(n: Int, a: Int, b: Int) = abs(a / n - b / n) + abs(a % n - b % n) == 1

private fun neighbours(n: Int, i: Int) = listOfNotNull(
    if (i / n > 0) i - n else null, if (i / n < n - 1) i + n else null,
    if (i % n > 0) i - 1 else null, if (i % n < n - 1) i + 1 else null,
)

/** A random route through every square once: a snake, shuffled by repeatedly re-linking one end ("backbite"). */
fun randomPath(n: Int, rng: Random): IntArray {
    val path = ArrayList<Int>(n * n)
    for (r in 0 until n) for (c in if (r % 2 == 0) 0 until n else n - 1 downTo 0) path += r * n + c
    repeat(n * n * 20) {
        if (rng.nextBoolean()) path.reverse()
        // Join the first square to a neighbour further along and flip the part in between.
        val k = path.indexOf(neighbours(n, path[0]).filter { it != path[1] }.random(rng))
        path.subList(0, k).reverse()
    }
    return path.toIntArray()
}

fun newPuzzle(level: Int, rng: Random): Puzzle {
    val n = sizeFor(level)
    val path = randomPath(n, rng)
    val stops = stopsFor(level, n)
    val numbers = IntArray(n * n)
    for (k in 0 until stops) numbers[path[k * (n * n - 1) / (stops - 1)]] = k + 1
    return Puzzle(n, numbers, stops, path)
}

/** Where every route begins: the square numbered 1. */
fun start(p: Puzzle) = listOf(p.numbers.indexOf(1))

/**
 * The route after the player touches square [x]: cut back to [x] if it is already on the route,
 * else grow by one square when [x] is next to the end and is not a number out of order.
 */
fun touch(p: Puzzle, path: List<Int>, x: Int): List<Int> {
    val at = path.indexOf(x)
    if (at >= 0) return path.take(at + 1)
    val next = path.count { p.numbers[it] != 0 } + 1
    val ok = adjacent(p.n, path.last(), x) && (p.numbers[x] == 0 || p.numbers[x] == next)
    return if (ok) path + x else path
}

/** Every square visited, ending on the last number (numbers are in order by how [touch] grows the route). */
fun isSolved(p: Puzzle, path: List<Int>) = path.size == p.n * p.n && p.numbers[path.last()] == p.stops

/**
 * Hint: keep the part of the route that follows the stored answer and add its next three squares.
 * shortcut: a different but valid route is cut back to where it left the stored answer; store all answers if players complain.
 */
fun hinted(p: Puzzle, path: List<Int>): List<Int> {
    var k = 0
    while (k < path.size && path[k] == p.solution[k]) k++
    return p.solution.take(minOf(k + 3, p.solution.size))
}

/** Saved level as text: the route's squares, then the seconds played. */
fun encodeGame(path: List<Int>, seconds: Int) = path.joinToString(",") + ";" + seconds

/** Throws when the text is damaged or is not a route the player could have drawn on this puzzle. */
fun decodeGame(text: String, p: Puzzle): Pair<List<Int>, Int> {
    val (c, s) = text.split(";")
    val cells = c.split(",").map { it.toInt() }
    var path = start(p)
    require(cells.first() == path[0])
    for (x in cells.drop(1)) {
        require(x in p.numbers.indices)
        val grown = touch(p, path, x)
        require(grown.size == path.size + 1)
        path = grown
    }
    return path to s.toInt()
}
