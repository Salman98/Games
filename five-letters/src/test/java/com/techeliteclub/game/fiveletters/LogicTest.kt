package com.techeliteclub.game.fiveletters

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicTest {
    @Test
    fun wordListIsClean() {
        assertTrue(WORDS.size > 500)
        assertEquals(WORDS.size, WORDS.toSet().size)
        assertTrue(WORDS.all { isGuess(it) })
        assertEquals(WORDS.size, (0 until WORDS.size).map { wordFor(it.toLong()) }.toSet().size) // no repeats
        assertTrue(wordFor(-3) in WORDS)
    }

    @Test
    fun scoringHandlesRepeatedLetters() {
        assertArrayEquals(intArrayOf(2, 2, 2, 2, 2), score("PLANT", "PLANT"))
        assertArrayEquals(intArrayOf(0, 2, 2, 1, 0), score("SLATE", "PLANT"))
        // One E in the answer: the right-place E wins and the other E gets no mark.
        assertArrayEquals(intArrayOf(0, 0, 0, 2, 0), score("EXXEX", "ABCED"))
        assertArrayEquals(intArrayOf(1, 0, 0, 0, 0), score("EXXXZ", "ABCDE"))
        // Two L in the guess, one in the answer: only the first is marked.
        assertArrayEquals(intArrayOf(1, 0, 0, 0, 0), score("LLXXX", "ABCDL"))
    }

    @Test
    fun keysHintsAndSaves() {
        val marks = keyMarks(listOf("SLATE", "CLAMP"), "PLANT")
        assertEquals(CORRECT, marks['L'])
        assertEquals(PRESENT, marks['T'])
        assertEquals(PRESENT, marks['P'])
        assertEquals(ABSENT, marks['S'])
        assertEquals(0, nextHint(listOf("SLATE"), emptySet(), "PLANT")) // P not found yet
        assertEquals(3, nextHint(listOf("SLATE"), setOf(0), "PLANT")) // L and A are known, N is next
        assertNull(nextHint(listOf("PLANT"), emptySet(), "PLANT"))
        assertFalse(isGuess("PLAN"))
        assertFalse(isGuess("plant"))
        assertEquals(listOf("SLATE", "CLAMP") to setOf(0, 3), decodeGame(encodeGame(listOf("SLATE", "CLAMP"), setOf(3, 0))))
        assertEquals(emptyList<String>() to emptySet<Int>(), decodeGame(";"))
        assertTrue(runCatching { decodeGame("SLAT;") }.isFailure)
    }
}
