package com.techeliteclub.game.dominologic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun puzzlesAreCoveredAndTheStoredAnswerWins() {
        for (level in listOf(1, 6, 16, 31, 80)) for (seed in 0..20) {
            val p = newPuzzle(level, Random(level * 100L + seed))
            val (rows, cols) = sizeFor(level)
            assertEquals(rows * cols / 2, p.tiles.size)
            assertEquals(p.tiles.size, p.tiles.map { (a, b) -> minOf(a, b) to maxOf(a, b) }.toSet().size) // no tile twice
            assertEquals((0 until rows * cols).toList(), p.regions.flatMap { it.cells }.sorted()) // every square in one region
            assertTrue(p.solution.all { adjacent(cols, it.a, it.b) })
            assertTrue("level $level seed $seed", isSolved(p, p.solution))
            assertFalse(isSolved(p, p.solution.drop(1)))
            var placed = emptyList<Placed>()
            repeat(p.tiles.size) { placed = hinted(p, placed) }
            assertTrue(isSolved(p, placed))
        }
    }

    @Test
    fun rules() {
        val v = mapOf(0 to 3, 1 to 3, 2 to 5)
        assertTrue(Region(listOf(0, 1), Rule.EQUAL).ok { v[it] })
        assertFalse(Region(listOf(0, 2), Rule.EQUAL).ok { v[it] })
        assertTrue(Region(listOf(0, 2), Rule.DIFFERENT).ok { v[it] })
        assertTrue(Region(listOf(0, 1, 2), Rule.SUM, 11).ok { v[it] })
        assertTrue(Region(listOf(2), Rule.LESS, 6).ok { v[it] })
        assertFalse(Region(listOf(2), Rule.MORE, 5).ok { v[it] })
        assertFalse(Region(listOf(2, 3), Rule.NONE).ok { v[it] }) // square 3 is empty
        assertEquals("<6", Region(listOf(2), Rule.LESS, 6).label)
    }

    @Test
    fun placingAndSaves() {
        val p = newPuzzle(1, Random(1)) // 3 rows of 4
        assertNull(place(p, emptyList(), 0, 0, 5, false)) // diagonal
        assertNull(place(p, emptyList(), 0, 3, 4, false)) // end of one row, start of the next
        val one = place(p, emptyList(), 0, 0, 1, true)
        assertEquals(listOf(Placed(0, 1, 0)), one)
        assertNull(place(p, one!!, 1, 1, 5, false)) // square taken
        assertNull(place(p, one, 0, 2, 3, false)) // tile already used
        assertNotNull(place(p, one, 1, 4, 8, false))
        assertEquals(p.tiles[0].first, pipsOf(p, one)[1])
        assertEquals(one to 12, decodeGame(encodeGame(one, 12), p))
        assertTrue(runCatching { decodeGame("0.0.1,1.1.2;0", p) }.isFailure)
    }
}
