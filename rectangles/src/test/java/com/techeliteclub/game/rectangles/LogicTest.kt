package com.techeliteclub.game.rectangles

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun puzzlesHaveOneAnswerThatWins() {
        for (level in listOf(1, 9, 20, 40)) {
            val p = newPuzzle(level, Random(level))
            assertEquals(sizeFor(level), p.n)
            assertTrue(isSolved(p, p.solution))
            assertEquals(1, countSolutions(p))
            assertEquals(p.solution.size, p.clues.size)
            // Hints alone finish the board.
            var rects = emptyList<Rect>()
            repeat(p.solution.size) { rects = hinted(p, rects) }
            assertTrue("level $level", isSolved(p, rects))
        }
    }

    @Test
    fun drawingAndChecking() {
        // 2x2 board: two clues of 2, answer is two rows.
        val p = Puzzle(2, mapOf(0 to 2, 2 to 2), listOf(Rect(0, 0, 1, 2), Rect(1, 0, 1, 2)))
        assertEquals(Rect(0, 0, 2, 1), Rect.spanning(2, 2, 0))
        var rects = draw(emptyList(), Rect(0, 0, 2, 1)) // a column holds both clues: bad
        assertFalse(rects.first().goodFor(p))
        rects = draw(rects, Rect(0, 0, 1, 2)) // overlaps, replaces it
        assertEquals(listOf(Rect(0, 0, 1, 2)), rects)
        assertFalse(isSolved(p, rects))
        assertTrue(isSolved(p, draw(rects, Rect(1, 0, 1, 2))))
        assertEquals(1, countSolutions(p))
    }

    @Test
    fun savesRoundTrip() {
        val rects = listOf(Rect(0, 0, 1, 2), Rect(1, 1, 2, 1))
        val (back, s) = decodeGame(encodeGame(rects, 9), 3)
        assertEquals(rects, back)
        assertEquals(9, s)
        assertEquals(emptyList<Rect>(), decodeGame(";0", 3).first)
        assertTrue(runCatching { decodeGame("2.2.2.2;0", 3) }.isFailure)
    }
}
