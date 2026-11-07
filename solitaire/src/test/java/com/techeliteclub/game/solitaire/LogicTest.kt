package com.techeliteclub.game.solitaire

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LogicTest {
    private fun all(b: Board) = b.stock + b.waste + b.foundations.flatten() + b.tableau.flatten()

    @Test
    fun dealIsAFullDeck() {
        val b = deal(Random(1))
        assertEquals(52, all(b).map { it.rank to it.suit }.toSet().size)
        assertEquals(28, b.tableau.sumOf { it.size })
        assertTrue(b.tableau.all { it.last().up && it.dropLast(1).none { c -> c.up } })
    }

    @Test
    fun drawAndRecycle() {
        var b = deal(Random(2))
        repeat(24) { b = draw(b) }
        assertEquals(0, b.stock.size)
        b = draw(b)
        assertEquals(24, b.stock.size)
        assertFalse(b.stock.any { it.up })
    }

    @Test
    fun movesFollowRules() {
        val b = Board(
            stock = emptyList(),
            waste = listOf(Card(1, 0, true)),
            foundations = List(4) { emptyList() },
            tableau = listOf(listOf(Card(5, 3), Card(8, 1, true)), listOf(Card(7, 0, true))) + List(5) { emptyList() },
        )
        val ace = autoMove(b, Source.Waste)!!
        assertEquals(1, ace.foundations[0].size) // ace goes home
        val seven = autoMove(b, Source.Tableau(1, 0))!!
        assertEquals(listOf(8, 7), seven.tableau[0].drop(1).map { it.rank }) // black 7 on red 8
        val eight = autoMove(b, Source.Tableau(0, 1))
        assertEquals(null, eight) // red 8 has nowhere to go (no 9, not a king)
        assertTrue(hint(b) != null)
    }

    @Test
    fun movingFlipsHiddenCard() {
        val b = Board(emptyList(), emptyList(), List(4) { emptyList() },
            listOf(listOf(Card(5, 3), Card(13, 1, true))) + List(6) { emptyList() })
        val after = autoMove(b, Source.Tableau(0, 1))!!
        assertTrue(after.tableau[0].single().up)
    }

    @Test
    fun randomPlayKeepsAllCards() {
        val rng = Random(3)
        var b = deal(rng)
        repeat(2000) {
            b = allSources(b).shuffled(rng).firstNotNullOfOrNull { autoMove(b, it) } ?: draw(b)
            assertEquals(52, all(b).map { it.rank to it.suit }.toSet().size)
        }
    }

    @Test
    fun autoFinishWinsWhenAllCardsAreUp() {
        // Every card face up, spread over tableau and stock: always finishable.
        val deck = (0 until 4).flatMap { s -> (1..13).map { Card(it, s, true) } }.shuffled(Random(5))
        val tableau = List(7) { col -> deck.drop(col * 4).take(4).sortedByDescending { it.rank } }
        val b = Board(deck.drop(28).map { it.copy(up = false) }, emptyList(), List(4) { emptyList() }, tableau)
        assertTrue(canAutoFinish(b))
        assertTrue(isWon(autoFinish(b)))
    }
}
