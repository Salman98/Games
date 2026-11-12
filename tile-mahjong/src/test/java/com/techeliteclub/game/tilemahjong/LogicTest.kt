package com.techeliteclub.game.tilemahjong

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun removalOrderClearsBoard() {
        for (level in 1..30) {
            val lv = newLevel(level, Random(level))
            var t = lv.tiles
            assertEquals(LAYOUT.size, t.size)
            for ((a, b) in lv.removalOrder) {
                assertTrue("level $level", isFree(t, a) && isFree(t, b) && t[a] == t[b])
                t = remove(t, a, b)
            }
            assertTrue(t.isEmpty())
        }
    }

    @Test
    fun freeRules() {
        val t: Tiles = mapOf(Pos(0, 0, 0) to 1, Pos(0, 0, 1) to 1, Pos(0, 0, 2) to 2, Pos(1, 0, 2) to 2)
        assertTrue(isFree(t, Pos(0, 0, 0))) // left edge open
        assertFalse(isFree(t, Pos(0, 0, 1))) // both sides taken
        assertFalse(isFree(t, Pos(0, 0, 2))) // covered from above
        assertEquals(0, freePairs(t).size) // only one 1 is free; the 2 on top has no free twin
    }

    @Test
    fun savedTilesRoundTripAndRejectTilesOffTheLayout() {
        val t = newLevel(4, Random(4)).tiles
        assertEquals(t, decodeTiles(encodeTiles(t)))
        assertTrue(runCatching { decodeTiles("9.9.9.1") }.isFailure)
    }
}
