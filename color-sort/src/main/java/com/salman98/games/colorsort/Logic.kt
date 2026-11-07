package com.salman98.games.colorsort

import kotlin.random.Random

const val CAP = 4

/** Each tube is a list of colour indexes, bottom first. */
typealias Tubes = List<List<Int>>

fun colorsFor(level: Int) = minOf(3 + (level - 1) / 3, 9)

// shortcut: a random shuffle with 2 empty tubes is not proven solvable per level (LogicTest checks 40 seeds, all solvable);
// the "extra tube" reward is the escape hatch. Move the test's solver here if players report stuck levels.
fun newLevel(level: Int, rng: Random): Tubes {
    val colors = colorsFor(level)
    while (true) {
        val balls = (0 until colors).flatMap { c -> List(CAP) { c } }.shuffled(rng)
        val tubes = balls.chunked(CAP) + List(2) { emptyList() }
        if (!isSolved(tubes)) return tubes
    }
}

fun canMove(t: Tubes, from: Int, to: Int): Boolean {
    if (from == to || t[from].isEmpty() || t[to].size >= CAP) return false
    return t[to].isEmpty() || t[to].last() == t[from].last()
}

/** Pours as many same-colour top balls as fit. Caller checks [canMove] first. */
fun move(t: Tubes, from: Int, to: Int): Tubes {
    val src = t[from].toMutableList()
    val dst = t[to].toMutableList()
    val color = src.last()
    while (src.isNotEmpty() && src.last() == color && dst.size < CAP) dst.add(src.removeAt(src.size - 1))
    return t.mapIndexed { i, tube -> if (i == from) src else if (i == to) dst else tube }
}

fun isSolved(t: Tubes) = t.all { it.isEmpty() || (it.size == CAP && it.all { c -> c == it[0] }) }

fun hasMoves(t: Tubes) = t.indices.any { a -> t.indices.any { b -> canMove(t, a, b) } }
