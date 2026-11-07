package com.salman98.games.crownlogic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun puzzlesAreValidAndUnique() {
        var unique = 0
        for (level in 1..40) {
            val p = newPuzzle(level, Random(level))
            val n = p.n
            assertEquals(n, p.regions.toSet().size)
            val crowns = p.solution.mapIndexed { r, c -> r * n + c }
            assertTrue(isSolved(p, crowns))
            if (solutions(p, 2).size == 1) unique++
        }
        assertTrue("unique $unique/40", unique >= 38)
    }

    @Test
    fun conflictRules() {
        val p = newPuzzle(1, Random(1))
        assertTrue(conflicts(p, listOf(0, 1)).isNotEmpty()) // same row
        assertTrue(conflicts(p, listOf(0, p.n + 1)).isNotEmpty()) // touching diagonally
        assertFalse(isSolved(p, listOf(0)))
    }
}
