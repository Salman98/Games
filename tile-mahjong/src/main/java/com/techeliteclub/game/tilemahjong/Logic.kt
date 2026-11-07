package com.techeliteclub.game.tilemahjong

import kotlin.random.Random

val SYMBOLS = listOf("🍎", "🍋", "🍇", "🍉", "🍒", "🥝", "🌸", "🌻", "🐟", "🐱", "🐶", "⭐", "🍀", "🍄", "🎈", "🔔")

const val COLS = 6
const val ROWS = 6

data class Pos(val layer: Int, val r: Int, val c: Int)

/** Bottom layer 6x6, top layer the 4x4 centre: 52 tiles = 26 pairs. */
val LAYOUT: List<Pos> =
    (0 until ROWS).flatMap { r -> (0 until COLS).map { c -> Pos(0, r, c) } } +
        (1..4).flatMap { r -> (1..4).map { c -> Pos(1, r, c) } }

/** Tiles on the board: position -> symbol index. */
typealias Tiles = Map<Pos, Int>

fun isFree(t: Tiles, p: Pos): Boolean {
    if (p !in t || Pos(p.layer + 1, p.r, p.c) in t) return false
    return Pos(p.layer, p.r, p.c - 1) !in t || Pos(p.layer, p.r, p.c + 1) !in t
}

fun freePairs(t: Tiles): List<Pair<Pos, Pos>> {
    val free = t.keys.filter { isFree(t, it) }
    return free.indices.flatMap { i -> (i + 1 until free.size).filter { t[free[i]] == t[free[it]] }.map { free[i] to free[it] } }
}

fun remove(t: Tiles, a: Pos, b: Pos): Tiles = t - a - b

class Level(val tiles: Tiles, val removalOrder: List<Pair<Pos, Pos>>)

fun typesFor(level: Int) = minOf(6 + level / 2, SYMBOLS.size)

/** Tiles in each layer row must stay one unbroken run while building, or a gap can never be filled. */
private fun rowContiguous(t: Map<Pos, Int>, p: Pos): Boolean {
    val cols = t.keys.filter { it.layer == p.layer && it.r == p.r }.map { it.c }
    return cols.isEmpty() || cols.max() - cols.min() + 1 == cols.size
}

/**
 * Built backwards: each pair is added where both tiles would be free, so removing pairs in the
 * reverse order always works. Random choices can still paint into a corner, so it retries.
 */
fun newLevel(level: Int, rng: Random): Level {
    val types = typesFor(level)
    repeat(2000) {
        val placed = HashMap<Pos, Int>()
        val order = ArrayList<Pair<Pos, Pos>>()
        var pairIndex = 0
        while (placed.size < LAYOUT.size) {
            val symbol = pairIndex % types
            val open = LAYOUT.filter { it !in placed && (it.layer == 0 || Pos(0, it.r, it.c) in placed) }.shuffled(rng)
            val pair = open.asSequence().flatMap { a -> open.asSequence().filter { it != a }.map { a to it } }
                .firstOrNull { (a, b) ->
                    val trial = placed + (a to symbol) + (b to symbol)
                    isFree(trial, a) && isFree(trial, b) && rowContiguous(trial, a) && rowContiguous(trial, b)
                } ?: break
            placed[pair.first] = symbol
            placed[pair.second] = symbol
            order += pair
            pairIndex++
        }
        if (placed.size == LAYOUT.size) {
            // Shuffle which symbol each pair gets so symbols aren't grouped by depth.
            val remap = (0 until types).shuffled(rng)
            return Level(placed.mapValues { remap[it.value] }, order.asReversed())
        }
    }
    error("could not build a mahjong layout")
}

/** Re-deals the remaining symbols randomly (used when the player has no moves). */
fun shuffle(t: Tiles, rng: Random): Tiles {
    val symbols = t.values.shuffled(rng)
    return t.keys.zip(symbols).toMap()
}
