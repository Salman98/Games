package com.techeliteclub.game.minisudoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun puzzlesHaveOneValidAnswer() {
        for (level in listOf(1, 5, 12, 20, 40)) {
            val p = newPuzzle(level, Random(level))
            assertTrue(p.solution.all { it in 1..N })
            assertTrue(conflicts(p.solution).isEmpty())
            assertEquals(1, countSolutions(p.givens))
            assertTrue((0 until CELLS).all { p.givens[it] == 0 || p.givens[it] == p.solution[it] })
            assertTrue("level $level holes", p.givens.count { it == 0 } >= holesFor(level) - 6)
        }
    }

    @Test
    fun boxesAreTwoRowsByThreeColumns() {
        assertEquals(0, box(0)); assertEquals(0, box(8)) // row 1, column 2
        assertEquals(1, box(3)); assertEquals(2, box(12)); assertEquals(5, box(35))
        val g = IntArray(CELLS)
        g[0] = 5; g[8] = 5 // same box, different row and column
        g[20] = 3
        assertEquals(setOf(0, 8), conflicts(g))
    }

    @Test
    fun savedGameRoundTripsAndRejectsOtherPuzzles() {
        val p = newPuzzle(1, Random(1))
        val notes = mapOf(3 to setOf(1, 6), 35 to emptySet())
        val s = decodeGame(encodeGame(p.solution, notes, 2, 300), p.givens)
        assertTrue(s.grid.contentEquals(p.solution))
        assertEquals(notes, s.notes)
        assertEquals(2, s.mistakes)
        assertEquals(300, s.seconds)
        assertTrue(runCatching { decodeGame(encodeGame(newPuzzle(2, Random(2)).solution, notes, 0, 0), p.givens) }.isFailure)
    }
}
