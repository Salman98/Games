package com.techeliteclub.game.wordgroups

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun everyWordBelongsToOneCategory() {
        val all = CATEGORIES.flatMap { it.words }
        assertEquals(all.groupBy { it }.filter { it.value.size > 1 }.keys, emptySet<String>())
        assertTrue(CATEGORIES.all { it.words.size >= PER_GROUP })
        assertTrue(all.all { w -> w.length in 2..9 && w.all { it in 'A'..'Z' } })
        assertEquals(CATEGORIES.size, CATEGORIES.map { it.name }.toSet().size)
    }

    @Test
    fun puzzlesAndChecks() {
        for (seed in 1..50) {
            val p = newPuzzle(Random(seed))
            assertEquals(16, p.words.toSet().size)
            assertEquals(p.words.toSet(), p.groups.flatMap { it.words }.toSet())
            for ((i, g) in p.groups.withIndex()) assertEquals(i, groupOf(p, g.words.toSet()))
            val mixed = (p.groups[0].words.take(3) + p.groups[1].words.take(1)).toSet()
            assertNull(groupOf(p, mixed))
            assertTrue(oneAway(p, mixed))
            assertFalse(oneAway(p, (p.groups[0].words.take(2) + p.groups[1].words.take(2)).toSet()))
        }
    }

    @Test
    fun savesRoundTrip() {
        assertEquals(Triple(listOf(2, 0), 3, 40), decodeGame(encodeGame(listOf(2, 0), 3, 40)))
        assertEquals(Triple(emptyList<Int>(), 0, 0), decodeGame(";0;0"))
        assertTrue(runCatching { decodeGame("22;0;0") }.isFailure)
        assertTrue(runCatching { decodeGame("0;4;0") }.isFailure)
    }
}
