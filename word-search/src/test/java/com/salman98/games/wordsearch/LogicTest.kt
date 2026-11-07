package com.salman98.games.wordsearch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun placedWordsReadCorrectlyAndCanBeSelected() {
        for (level in 1..30) {
            val p = newPuzzle(level, Random(level))
            assertEquals(countFor(level), p.placed.size)
            for ((w, idx) in p.placed) {
                assertEquals(w, idx.map { p.letters[it] }.joinToString(""))
                assertEquals(w, wordAt(p, idx.first(), idx.last(), emptySet()))
                assertEquals(w, wordAt(p, idx.last(), idx.first(), emptySet())) // backwards selection works
            }
        }
    }

    @Test
    fun lineRejectsBentSelections() {
        assertEquals(null, line(8, 0, 10)) // (0,0) to (1,2)
        assertEquals(listOf(0, 9, 18), line(8, 0, 18)) // diagonal
        assertTrue(WORDS.all { it.all(Char::isUpperCase) })
    }
}
