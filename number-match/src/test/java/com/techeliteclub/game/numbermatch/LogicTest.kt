package com.techeliteclub.game.numbermatch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    private val board = listOf(
        1, 2, 3, 4, 5, 6, 7, 8, 9,
        9, 0, 0, 0, 0, 0, 0, 0, 1,
        5, 3, 4, 4, 2, 6, 3, 7, 5,
    )

    @Test
    fun pairRules() {
        assertTrue(canPair(board, 8, 9)) // 9 and 9: row end wraps to the next row start
        assertTrue(canPair(board, 9, 17)) // 9 + 1 across crossed-out squares
        assertTrue(canPair(board, 0, 9)) // 1 + 9 in a column
        assertTrue(canPair(board, 3, 21)) // 4 and 4 down a column through a crossed-out square
        assertFalse(canPair(board, 4, 22)) // 5 and 2
        assertFalse(canPair(board, 0, 17)) // 1 and 1, but numbers in between and not in a line
        assertFalse(canPair(board, 0, 0))
    }

    @Test
    fun diagonalsAndBlocking() {
        val d = MutableList(27) { 0 }
        d[0] = 4; d[20] = 6; d[8] = 3; d[16] = 7; d[12] = 5
        assertTrue(canPair(d, 0, 20)) // 4 + 6 down-right
        assertTrue(canPair(d, 8, 16)) // 3 + 7 down-left, even with a number between them in reading order
        d[10] = 9
        assertFalse(canPair(d, 0, 20)) // blocked
    }

    @Test
    fun crossingRemovesEmptyRowsAndGameEnds() {
        val (after, rows) = cross(board, 9, 17)
        assertEquals(1, rows)
        assertEquals(18, after.size)
        val (gone, r) = cross(listOf(5, 5), 0, 1)
        assertTrue(gone.isEmpty() && r == 1 && isCleared(gone))
        assertEquals(listOf(3, 0, 4, 3, 4), addNumbers(listOf(3, 0, 4)))
        assertNull(findPair(listOf(1, 2, 0, 4)))
        assertEquals(0 to 1, findPair(listOf(3, 7, 2)))
    }

    @Test
    fun newBoardsAndSaves() {
        assertEquals(27, newBoard(1, Random(1)).size)
        assertEquals(54, newBoard(30, Random(1)).size)
        assertTrue(newBoard(5, Random(5)).all { it in 1..9 })
        val g = decodeGame(encodeGame(Game(listOf(1, 0, 9), 2, 140, 33)))
        assertEquals(listOf(1, 0, 9), g.board)
        assertEquals(2, g.adds)
        assertEquals(140, g.score)
        assertEquals(33, g.seconds)
        assertTrue(runCatching { decodeGame("1x;1;0;0") }.isFailure)
    }
}
