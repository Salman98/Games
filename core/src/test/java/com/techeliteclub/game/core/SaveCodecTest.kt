package com.techeliteclub.game.core

import org.junit.Assert.assertEquals
import org.junit.Test

class SaveCodecTest {
    @Test
    fun roundTripsTypesThroughFirestoreLongs() {
        val prefs = mapOf("level" to 7, "streak_day" to 20764L, "howto_seen" to true, "skin" to "neon")
        // Firestore hands every whole number back as a Long.
        val fromCloud = SaveCodec.encode(prefs).mapValues { (_, group) ->
            (group as Map<*, *>).mapValues { (_, v) -> if (v is Int) v.toLong() else v }
        }
        assertEquals(prefs, SaveCodec.decode(fromCloud))
    }
}
