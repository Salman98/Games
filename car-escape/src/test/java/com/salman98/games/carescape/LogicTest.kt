package com.salman98.games.carescape

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    @Test
    fun levelsAreSolvableAndNotOverlapping() {
        for (level in listOf(1, 3, 6, 10, 15)) {
            val cars = newLevel(level, Random(level))
            val cells = cars.flatMap { it.cells() }
            assertEquals(cells.size, cells.toSet().size)
            val path = solve(cars)!!
            var s = cars
            for (m in path) {
                assertTrue(m.delta in slideRange(s, m.car))
                s = move(s, m)
            }
            assertTrue(isSolved(s))
        }
    }

    @Test
    fun slideStopsAtCarsAndWalls() {
        val cars = listOf(Car(EXIT_ROW, 0, 2, true), Car(1, 4, 2, false))
        assertEquals(0..2, slideRange(cars, 0)) // red slides right until the vertical car at column 4
        assertEquals(-1..3, slideRange(cars, 1))
        assertEquals(2, solve(cars)!!.size) // move blocker away, then drive out
    }
}
