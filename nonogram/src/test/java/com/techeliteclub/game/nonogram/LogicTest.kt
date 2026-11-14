package com.techeliteclub.game.nonogram

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun runsAndLineSolver() {
        assertEquals(listOf(2, 1), runs(listOf(true, true, false, true, false)))
        assertEquals(listOf(0), runs(listOf(false, false)))
        // A 4 in a line of 5: the middle three are certain.
        assertArrayEquals(intArrayOf(-1, 1, 1, 1, -1), solveLine(listOf(4), IntArray(5) { -1 }))
        // 0 clue empties the line; an impossible line gives null.
        assertArrayEquals(intArrayOf(0, 0, 0), solveLine(listOf(0), IntArray(3) { -1 }))
        assertNull(solveLine(listOf(3), intArrayOf(-1, 0, -1)))
        assertArrayEquals(intArrayOf(1, 0, 1), solveLine(listOf(1, 1), IntArray(3) { -1 }))
    }

    @Test
    fun puzzlesAreSymmetricSolvableAndWinOnlyWithTheRightPicture() {
        for (level in listOf(1, 7, 19, 31, 60)) {
            val p = newPuzzle(level, Random(level))
            val n = p.n
            assertEquals(sizeFor(level), n)
            for (r in 0 until n) for (c in 0 until n) assertEquals(p.solution[r * n + c], p.solution[r * n + n - 1 - c])
            val cells = IntArray(n * n) { if (p.solution[it]) FILL else EMPTY }
            assertTrue(isSolved(p, cells))
            assertFalse(isSolved(p, IntArray(n * n)) && p.solution.any { it })
            // Hints finish the picture from blank.
            var g = IntArray(n * n)
            repeat(n * n) { g = hinted(p, g) }
            assertTrue("level $level", isSolved(p, g))
        }
    }

    @Test
    fun hintFixesWrongSquaresFirstAndSavesRoundTrip() {
        val p = Puzzle(2, booleanArrayOf(true, false, false, false))
        assertArrayEquals(intArrayOf(FILL, CROSS, EMPTY, EMPTY), hinted(p, intArrayOf(FILL, FILL, EMPTY, EMPTY)))
        assertArrayEquals(intArrayOf(FILL, EMPTY, EMPTY, EMPTY), hinted(p, IntArray(4)))
        val (back, s) = decodeGame(encodeGame(intArrayOf(1, 2, 0, 1), 42), 2)
        assertArrayEquals(intArrayOf(1, 2, 0, 1), back)
        assertEquals(42, s)
        assertTrue(runCatching { decodeGame("12;3", 2) }.isFailure)
        assertTrue(runCatching { decodeGame("1239;3", 2) }.isFailure)
    }
}
