package com.techeliteclub.game.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicTest {
    @Test
    fun streakGrowsBreaksAndHolds() {
        assertEquals(5, StreakMath.next(9, 4, 10))
        assertEquals(4, StreakMath.next(10, 4, 10))
        assertEquals(1, StreakMath.next(7, 4, 10))
        assertEquals(1, StreakMath.next(-1, 0, 10))
        assertEquals(4, StreakMath.shown(9, 4, 10))
        assertEquals(0, StreakMath.shown(8, 4, 10))
        assertTrue(StreakMath.inStreak(8, 10, 3))
        assertFalse(StreakMath.inStreak(7, 10, 3))
    }

    @Test
    fun weekStartsMonday() {
        assertEquals(20759L, StreakMath.weekStart(20764)) // Sat 2026-11-07 -> Mon 2026-11-02
        assertEquals(20759L, StreakMath.weekStart(20759))
    }
}
