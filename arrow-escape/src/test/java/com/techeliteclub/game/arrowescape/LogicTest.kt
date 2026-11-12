package com.techeliteclub.game.arrowescape

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

    @Test
    fun savedGameRoundTripsAndRejectsOtherBoards() {
        val start = newLevel(3, Random(3))
        val board = start.toMutableList().also { it[start.indexOfFirst { d -> d != null }] = null }
        assertTrue(decodeGame(encodeGame(board, 2), start) == (board to 2))
        assertTrue(runCatching { decodeGame(encodeGame(board, 2), newLevel(30, Random(30))) }.isFailure)
    }
}
