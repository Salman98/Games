package com.techeliteclub.blockpuzzle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicTest {
    private val dot = Piece(listOf(0 to 0), 1)

    @Test
    fun shapesAreNormalised() {
        for (s in SHAPES) {
            assertTrue(s.isNotEmpty())
            assertEquals(0, s.minOf { it.first })
            assertEquals(0, s.minOf { it.second })
        }
    }

    @Test
    fun fullRowAndColumnClear() {
        var b = IntArray(SIZE * SIZE)
        for (c in 1 until SIZE) b = place(b, dot, 0, c).board // row 0 except corner
        for (r in 1 until SIZE) b = place(b, dot, r, 0).board // column 0 except corner
        val res = place(b, dot, 0, 0)
        assertEquals(2, res.linesCleared)
        assertTrue(res.board.all { it == 0 })
        assertEquals(1 + 40, res.points)
    }

    @Test
    fun boundsAndOverlap() {
        val bar = Piece(parseShape("XXX"), 2)
        val b = IntArray(SIZE * SIZE)
        assertTrue(canPlace(b, bar, 0, 5))
        assertFalse(canPlace(b, bar, 0, 6))
        val placed = place(b, bar, 3, 3).board
        assertFalse(canPlace(placed, dot, 3, 4))
    }

    @Test
    fun gameOverWhenNothingFits() {
        val full = IntArray(SIZE * SIZE) { if (it == 0) 0 else 1 }
        assertTrue(fitsAnywhere(full, dot))
        assertFalse(fitsAnywhere(full, Piece(parseShape("XX"), 1)))
    }
}
