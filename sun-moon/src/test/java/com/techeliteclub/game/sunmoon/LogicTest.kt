package com.techeliteclub.game.sunmoon

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun puzzlesHaveOneAnswer() {
        for (level in listOf(1, 3, 4, 12, 25, 40)) {
            val p = newPuzzle(level, Random(level))
            assertEquals(sizeFor(level) * sizeFor(level), p.solution.size)
            assertTrue(isSolved(p, p.solution))
            assertTrue(p.givens.indices.all { p.givens[it] == EMPTY || p.givens[it] == p.solution[it] })
            val all = solutions(p.n, p.givens, p.links, 2)
            assertEquals("level $level", 1, all.size)
            assertTrue(all[0].contentEquals(p.solution))
        }
    }

    @Test
    fun rules() {
        val none = emptyList<Link>()
        val g = IntArray(16)
        g[0] = SUN; g[1] = SUN
        assertTrue(violations(4, g, none).isEmpty())
        g[2] = SUN // three suns in a row of four: too many and three in a row
        assertEquals(setOf(0, 1, 2), violations(4, g, none))
        val h = IntArray(16)
        h[0] = SUN; h[4] = MOON
        assertEquals(setOf(0, 4), violations(4, h, listOf(Link(0, 4, same = true))))
        assertTrue(violations(4, h, listOf(Link(0, 4, same = false))).isEmpty())
        assertFalse(isSolved(Puzzle(4, IntArray(16), none, IntArray(16)), h))
    }

    @Test
    fun savedGameRoundTripsAndRejectsOtherPuzzles() {
        val p = newPuzzle(5, Random(5))
        val (g, seconds) = decodeGame(encodeGame(p.solution, 42), p)
        assertTrue(g.contentEquals(p.solution))
        assertEquals(42, seconds)
        val other = p.solution.map { if (it == SUN) MOON else SUN }.toIntArray()
        assertTrue(runCatching { decodeGame(encodeGame(other, 0), p) }.isFailure)
    }
}
