package com.techeliteclub.game.numberpath

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun puzzlesHaveARouteThatWins() {
        for (level in listOf(1, 6, 12, 20, 40)) {
            val p = newPuzzle(level, Random(level))
            val n = p.n
            assertEquals((0 until n * n).toSet(), p.solution.toSet()) // every square once
            assertTrue((1 until n * n).all { adjacent(n, p.solution[it - 1], p.solution[it]) })
            assertEquals((1..p.stops).toList(), p.solution.map { p.numbers[it] }.filter { it != 0 }) // numbers in order
            assertEquals(1, p.numbers[p.solution.first()])
            assertEquals(p.stops, p.numbers[p.solution.last()])
            // Drawing the stored answer square by square solves it.
            var path = start(p)
            for (x in p.solution.drop(1)) path = touch(p, path, x)
            assertTrue("level $level", isSolved(p, path))
        }
    }

    @Test
    fun touchRules() {
        // 3x3: 1 at square 0, 2 at square 4, 3 at square 8.
        val p = Puzzle(3, intArrayOf(1, 0, 0, 0, 2, 0, 0, 0, 3), 3, intArrayOf(0, 1, 2, 5, 4, 3, 6, 7, 8))
        assertEquals(listOf(0), start(p))
        assertEquals(listOf(0), touch(p, listOf(0), 4)) // not next to the end
        assertEquals(listOf(0, 1), touch(p, listOf(0), 1))
        assertEquals(listOf(0, 1, 4), touch(p, listOf(0, 1), 4)) // number 2 is next
        assertEquals(listOf(0, 1, 2, 5), touch(p, listOf(0, 1, 2, 5), 8)) // number 3 before number 2
        assertEquals(listOf(0, 1), touch(p, listOf(0, 1, 2, 5), 1)) // cut back
        assertFalse(isSolved(p, listOf(0, 1, 4)))
        assertEquals(listOf(0, 1, 2, 5), hinted(p, listOf(0, 3))) // keeps square 0, adds three from the answer
    }

    @Test
    fun savedGameRoundTripsAndRejectsOtherRoutes() {
        val p = newPuzzle(3, Random(3))
        val path = p.solution.take(6)
        val (back, seconds) = decodeGame(encodeGame(path, 75), p)
        assertEquals(path, back)
        assertEquals(75, seconds)
        // Two squares apart on the route are never side by side on the board.
        assertTrue(runCatching { decodeGame(encodeGame(listOf(p.solution[0], p.solution[2]), 0), p) }.isFailure)
        assertTrue(runCatching { decodeGame(encodeGame(listOf(p.solution[1]), 0), p) }.isFailure)
    }
}
