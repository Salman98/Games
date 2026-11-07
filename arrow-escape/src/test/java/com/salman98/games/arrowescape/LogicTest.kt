package com.salman98.games.arrowescape

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun everyLevelClearsInAnyGreedyOrder() {
        for (level in 1..40) {
            val n = sizeFor(level)
            val board = newLevel(level, Random(level)).toMutableList()
            assertTrue("level $level too empty", board.count { it != null } >= n * n / 2)
            while (board.any { it != null }) {
                val free = board.indices.filter { canExit(board, n, it) }
                assertTrue("stuck on level $level", free.isNotEmpty())
                board[free.random(Random(level))] = null
            }
        }
    }

    @Test
    fun blockedArrowCannotExit() {
        val board = listOf<Dir?>(Dir.RIGHT, Dir.UP, null, null)
        assertFalse(canExit(board, 2, 0)) // arrow at (0,1) blocks
        assertTrue(canExit(board, 2, 1))
    }
}
