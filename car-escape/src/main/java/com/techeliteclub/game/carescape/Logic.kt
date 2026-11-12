package com.techeliteclub.game.carescape

import kotlin.random.Random

const val N = 6
const val EXIT_ROW = 2

/** Car 0 is the red car; it escapes when its front reaches the right edge of [EXIT_ROW]. */
data class Car(val r: Int, val c: Int, val len: Int, val horizontal: Boolean) {
    fun cells() = (0 until len).map { k -> if (horizontal) r * N + c + k else (r + k) * N + c }
}

data class Move(val car: Int, val delta: Int)

fun isSolved(cars: List<Car>) = cars[0].c + cars[0].len == N

/** How far car [i] can slide: negative = left/up, positive = right/down. */
fun slideRange(cars: List<Car>, i: Int): IntRange {
    val taken = BooleanArray(N * N)
    cars.forEachIndexed { j, car -> if (j != i) car.cells().forEach { taken[it] = true } }
    val car = cars[i]
    fun free(k: Int) = if (car.horizontal) car.c + k in 0 until N && !taken[car.r * N + car.c + k]
    else car.r + k in 0 until N && !taken[(car.r + k) * N + car.c]
    var lo = 0
    while (free(lo - 1)) lo--
    var hi = 0
    while (free(car.len + hi)) hi++
    return lo..hi
}

fun move(cars: List<Car>, m: Move): List<Car> = cars.mapIndexed { j, car ->
    if (j != m.car) car else if (car.horizontal) car.copy(c = car.c + m.delta) else car.copy(r = car.r + m.delta)
}

/** Shortest solution (one slide of any distance = one move), or null if none within [maxStates]. */
fun solve(start: List<Car>, maxStates: Int = 60_000): List<Move>? {
    val prev = HashMap<List<Car>, Pair<List<Car>, Move>?>()
    prev[start] = null
    val queue = ArrayDeque(listOf(start))
    while (queue.isNotEmpty() && prev.size < maxStates) {
        val s = queue.removeFirst()
        if (isSolved(s)) {
            val path = ArrayList<Move>()
            var cur = s
            while (true) { val (p, m) = prev[cur] ?: break; path += m; cur = p }
            return path.asReversed()
        }
        for (i in s.indices) for (d in slideRange(s, i)) {
            if (d == 0) continue
            val next = move(s, Move(i, d))
            if (next !in prev) { prev[next] = s to Move(i, d); queue.addLast(next) }
        }
    }
    return null
}

fun targetMoves(level: Int) = minOf(2 + level, 12)

/** Random boards, kept if solvable; returns the first that needs [targetMoves], else the hardest found. */
fun newLevel(level: Int, rng: Random): List<Car> {
    var best: List<Car>? = null
    var bestMoves = -1
    repeat(150) {
        val cars = mutableListOf(Car(EXIT_ROW, rng.nextInt(3), 2, true))
        val taken = BooleanArray(N * N).also { t -> cars[0].cells().forEach { t[it] = true } }
        repeat(6 + rng.nextInt(7)) {
            val horizontal = rng.nextBoolean()
            val len = if (rng.nextInt(4) == 0) 3 else 2
            val car = if (horizontal) Car(rng.nextInt(N), rng.nextInt(N - len + 1), len, true)
            else Car(rng.nextInt(N - len + 1), rng.nextInt(N), len, false)
            // A sideways car in the exit row would block the red car forever.
            if (!(horizontal && car.r == EXIT_ROW) && car.cells().none { taken[it] }) {
                cars += car
                car.cells().forEach { taken[it] = true }
            }
        }
        val moves = solve(cars)?.size ?: return@repeat
        if (moves >= targetMoves(level)) return cars
        if (moves > bestMoves) { best = cars; bestMoves = moves }
    }
    return best ?: listOf(Car(EXIT_ROW, 0, 2, true))
}

/** Saved level as text: every state from the first move to now, one digit per car (its column if sideways, else its row). */
fun encodeStates(states: List<List<Car>>) = states.joinToString(";") { s -> s.joinToString("") { "${if (it.horizontal) it.c else it.r}" } }

/** Throws when the text is damaged or does not fit [start] (the level as dealt): cars off the board or overlapping. */
fun decodeStates(text: String, start: List<Car>): List<List<Car>> = text.split(";").map { s ->
    require(s.length == start.size)
    start.mapIndexed { i, car -> s[i].digitToInt().let { if (car.horizontal) car.copy(c = it) else car.copy(r = it) } }.also { cars ->
        val cells = cars.flatMap { it.cells() }
        require(cars.all { (if (it.horizontal) it.c else it.r) + it.len <= N } && cells.size == cells.toSet().size)
    }
}
