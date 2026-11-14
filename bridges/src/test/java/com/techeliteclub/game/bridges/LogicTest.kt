package com.techeliteclub.game.bridges

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun puzzlesAreValidAndTheStoredAnswerWins() {
        for (level in listOf(1, 7, 13, 25, 60)) for (seed in 0..30) {
            val p = newPuzzle(level, Random(level * 100L + seed))
            val n = p.n
            assertTrue(p.islands.size >= 5)
            assertEquals(p.islands, p.islands.distinct().sorted())
            // No two islands touch, every need is 1..8, and no two answer bridges cross.
            for (i in p.islands) assertFalse((i % n < n - 1 && i + 1 in p.islands) || i + n in p.islands)
            assertTrue(p.need.all { it in 1..8 })
            assertTrue(p.solution.keys.all { it in p.links })
            val keys = p.solution.keys.toList()
            for (x in keys) for (y in keys) assertFalse(crosses(p, x, y))
            assertTrue("level $level seed $seed", isSolved(p, p.solution))
            assertFalse(isSolved(p, emptyMap()))
            var b = emptyMap<Link, Int>()
            repeat(p.solution.size + 2) { b = hinted(p, b) }
            assertTrue(isSolved(p, b))
        }
    }

    @Test
    fun cyclingCrossingAndConnection() {
        // 5x5 plus sign: islands at top(2), left(10), right(14), bottom(22). Top-bottom and left-right cross in the middle.
        val p = Puzzle(5, listOf(2, 10, 14, 22), listOf(1, 1, 1, 1), emptyMap())
        val vertical = Link(0, 3)
        val horizontal = Link(1, 2)
        assertEquals(setOf(vertical, horizontal), p.links.toSet())
        assertTrue(crosses(p, vertical, horizontal))
        var b = cycle(p, emptyMap(), vertical)!!
        assertEquals(1, b[vertical])
        assertNull(cycle(p, b, horizontal)) // would cross
        b = cycle(p, b, vertical)!!
        assertEquals(2, count(b, 0))
        b = cycle(p, b, vertical)!!
        assertTrue(b.isEmpty())
        assertFalse(connected(p, mapOf(vertical to 1)))
        assertEquals(vertical, neighbour(p, 0, 1, 0))
        assertNull(neighbour(p, 0, 0, 1))
        assertEquals(horizontal, neighbour(p, 2, 0, -1))
    }

    @Test
    fun savesRoundTrip() {
        val p = newPuzzle(8, Random(8))
        val some = p.solution.entries.take(3).associate { it.key to it.value }
        assertEquals(some to 77, decodeGame(encodeGame(some, 77), p))
        assertEquals(emptyMap<Link, Int>() to 0, decodeGame(";0", p))
        assertTrue(runCatching { decodeGame("0.0.1;0", p) }.isFailure)
    }
}
