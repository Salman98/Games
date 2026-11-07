package com.salman98.games.colorsort

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun levelHasEachColourCapTimes() {
        for (level in 1..30) {
            val t = newLevel(level, Random(level))
            val n = colorsFor(level)
            assertEquals(n + 2, t.size)
            assertEquals(List(n) { CAP }, t.flatten().groupingBy { it }.eachCount().toSortedMap().values.toList())
            assertFalse(isSolved(t))
        }
    }

    @Test
    fun pourMovesAllMatchingThatFit() {
        val t: Tubes = listOf(listOf(0, 1, 1), listOf(2, 1), emptyList())
        assertTrue(canMove(t, 0, 1))
        val r = move(t, 0, 1)
        assertEquals(listOf(0), r[0]) // both 1s fit (cap 4)
        assertEquals(listOf(2, 1, 1, 1), r[1])
        assertFalse(canMove(r, 0, 1)) // destination full
        assertFalse(canMove(t, 2, 0)) // source empty
    }

    // Depth-first search over pours; checks the "random shuffle is solvable" shortcut in Logic.kt.
    private fun solvable(start: Tubes): Boolean {
        val seen = HashSet<String>()
        val stack = ArrayDeque(listOf(start))
        while (stack.isNotEmpty()) {
            val t = stack.removeLast()
            if (isSolved(t)) return true
            if (!seen.add(t.map { it.joinToString(",") }.sorted().joinToString("|"))) continue
            if (seen.size > 300_000) return false // treat "too big to prove" as a failure
            for (a in t.indices) for (b in t.indices) if (canMove(t, a, b)) stack.add(move(t, a, b))
        }
        return false
    }

    @Test
    fun generatedLevelsAreSolvable() {
        for (level in 1..40) assertTrue("level $level", solvable(newLevel(level, Random(level * 7L))))
    }

    @Test
    fun solvedDetection() {
        assertTrue(isSolved(listOf(List(CAP) { 0 }, emptyList())))
        assertFalse(isSolved(listOf(listOf(0, 0, 0), listOf(0))))
    }
}
