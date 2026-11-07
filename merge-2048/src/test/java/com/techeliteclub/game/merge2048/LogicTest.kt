package com.techeliteclub.game.merge2048

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    private fun row(vararg v: Int) = v + IntArray(N * N - N)

    @Test
    fun mergesOncePerMove() {
        val r = slide(row(2, 2, 2, 2), Dir.LEFT)
        assertEquals(listOf(4, 4, 0, 0), r.board.take(4))
        assertEquals(8, r.gained)
    }

    @Test
    fun rightAndVertical() {
        assertEquals(listOf(0, 0, 8, 8), slide(row(4, 0, 4, 8), Dir.RIGHT).board.take(4))
        val col = IntArray(N * N).also { it[0] = 2; it[N] = 2; it[2 * N] = 4 }
        val up = slide(col, Dir.UP).board
        assertEquals(listOf(4, 4, 0, 0), listOf(up[0], up[N], up[2 * N], up[3 * N]))
    }

    @Test
    fun noMoveDetection() {
        val stuck = IntArray(N * N) { if ((it / N + it % N) % 2 == 0) 2 else 4 }
        assertFalse(canMove(stuck))
        assertFalse(slide(row(2, 4, 8, 16), Dir.LEFT).moved)
        assertTrue(newBoard(Random(1)).count { it != 0 } == 2)
    }
}
