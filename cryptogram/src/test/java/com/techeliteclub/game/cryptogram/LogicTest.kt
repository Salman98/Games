package com.techeliteclub.game.cryptogram

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun quotesFitThePhone() {
        assertEquals(QUOTES.size, QUOTES.toSet().size)
        for (q in QUOTES) {
            assertTrue(q, q.all { it in 'A'..'Z' || it == ' ' } && !q.contains("  ") && q == q.trim())
            assertTrue(q, q.split(" ").all { it.length <= 11 }) // longest word fits one line of 12
            assertTrue(q, wrap(q, 12).size <= 7)
        }
    }

    @Test
    fun puzzlesCodeEveryLetterOnce() {
        for (level in listOf(1, 5, 30, 200)) {
            val p = newPuzzle(level, Random(level))
            assertEquals(p.letters, p.code.keys)
            assertEquals(p.code.size, p.code.values.toSet().size)
            assertTrue(p.given.size >= 2 && p.given.size < p.letters.size)
            assertFalse(isSolved(p, emptySet()))
            assertTrue(isSolved(p, p.letters - p.given))
        }
        assertTrue(newPuzzle(1, Random(1)).given.size > newPuzzle(100, Random(1)).given.size)
    }

    @Test
    fun wrapAndSaves() {
        assertEquals(listOf(listOf("ALL", "IS"), listOf("WELL")), wrap("ALL IS WELL", 7))
        val p = newPuzzle(3, Random(3))
        val some = (p.letters - p.given).take(2).toSet()
        assertEquals(Triple(some, 1, 20), decodeGame(encodeGame(some, 1, 20), p))
        assertTrue(runCatching { decodeGame("A;9;0", p) }.isFailure)
    }
}
