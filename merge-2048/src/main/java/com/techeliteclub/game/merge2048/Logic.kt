package com.techeliteclub.game.merge2048

import kotlin.random.Random

const val N = 4

enum class Dir { LEFT, RIGHT, UP, DOWN }

class SlideResult(val board: IntArray, val gained: Int, val moved: Boolean)

/** Merges one line toward index 0. Each tile merges at most once per move. */
private fun mergeLine(line: List<Int>): Pair<List<Int>, Int> {
    val tiles = line.filter { it != 0 }
    val out = ArrayList<Int>()
    var gained = 0
    var i = 0
    while (i < tiles.size) {
        if (i + 1 < tiles.size && tiles[i] == tiles[i + 1]) {
            out += tiles[i] * 2; gained += tiles[i] * 2; i += 2
        } else {
            out += tiles[i]; i++
        }
    }
    return (out + List(N - out.size) { 0 }) to gained
}

fun slide(board: IntArray, dir: Dir): SlideResult {
    val out = IntArray(N * N)
    var gained = 0
    for (k in 0 until N) {
        // Cell indexes of line k, ordered from the side the tiles move toward.
        val idx = (0 until N).map { j ->
            when (dir) {
                Dir.LEFT -> k * N + j
                Dir.RIGHT -> k * N + (N - 1 - j)
                Dir.UP -> j * N + k
                Dir.DOWN -> (N - 1 - j) * N + k
            }
        }
        val (merged, g) = mergeLine(idx.map { board[it] })
        idx.forEachIndexed { j, i -> out[i] = merged[j] }
        gained += g
    }
    return SlideResult(out, gained, !out.contentEquals(board))
}

fun spawn(board: IntArray, rng: Random): IntArray {
    val empty = board.indices.filter { board[it] == 0 }
    if (empty.isEmpty()) return board
    return board.copyOf().also { it[empty.random(rng)] = if (rng.nextInt(10) == 0) 4 else 2 }
}

fun newBoard(rng: Random) = spawn(spawn(IntArray(N * N), rng), rng)

fun canMove(board: IntArray) = Dir.entries.any { slide(board, it).moved }
