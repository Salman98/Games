package com.techeliteclub.solitaire

import kotlin.random.Random

/** [rank] 1 (Ace) to 13 (King); [suit] 0..3 = ♠ ♥ ♦ ♣. */
data class Card(val rank: Int, val suit: Int, val up: Boolean = false) {
    val red get() = suit == 1 || suit == 2
    val label get() = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")[rank - 1] + "♠♥♦♣"[suit]
}

/** [foundations] is indexed by suit. */
data class Board(
    val stock: List<Card>,
    val waste: List<Card>,
    val foundations: List<List<Card>>,
    val tableau: List<List<Card>>,
)

sealed interface Source {
    data object Waste : Source
    data class Foundation(val suit: Int) : Source
    data class Tableau(val col: Int, val index: Int) : Source
}

fun deal(rng: Random): Board {
    val deck = (0 until 4).flatMap { s -> (1..13).map { Card(it, s) } }.shuffled(rng).toMutableList()
    val tableau = (0 until 7).map { col ->
        List(col + 1) { k -> deck.removeAt(deck.size - 1).copy(up = k == col) }
    }
    return Board(deck, emptyList(), List(4) { emptyList() }, tableau)
}

/** Tap on the stock: draw one, or turn the waste back over when the stock is empty. */
fun draw(b: Board): Board =
    if (b.stock.isEmpty()) b.copy(stock = b.waste.asReversed().map { it.copy(up = false) }, waste = emptyList())
    else b.copy(stock = b.stock.dropLast(1), waste = b.waste + b.stock.last().copy(up = true))

fun fitsFoundation(b: Board, c: Card) = b.foundations[c.suit].size == c.rank - 1

fun fitsTableau(pile: List<Card>, c: Card): Boolean {
    val top = pile.lastOrNull() ?: return c.rank == 13
    return top.up && top.red != c.red && top.rank == c.rank + 1
}

/** Cards that move together when [src] is tapped; null if not movable. */
fun cardsAt(b: Board, src: Source): List<Card>? = when (src) {
    Source.Waste -> b.waste.lastOrNull()?.let { listOf(it) }
    is Source.Foundation -> b.foundations[src.suit].lastOrNull()?.let { listOf(it) }
    is Source.Tableau -> b.tableau[src.col].drop(src.index).takeIf { it.isNotEmpty() && it.first().up }
}

private fun without(b: Board, src: Source, count: Int): Board = when (src) {
    Source.Waste -> b.copy(waste = b.waste.dropLast(1))
    is Source.Foundation -> b.copy(foundations = b.foundations.mapIndexed { i, f -> if (i == src.suit) f.dropLast(1) else f })
    is Source.Tableau -> b.copy(tableau = b.tableau.mapIndexed { i, p ->
        if (i != src.col) p else p.dropLast(count).let { rest ->
            if (rest.isNotEmpty() && !rest.last().up) rest.dropLast(1) + rest.last().copy(up = true) else rest
        }
    })
}

/** Best move for a tapped card: foundation first, then a tableau column. Null if nothing fits. */
fun autoMove(b: Board, src: Source): Board? {
    val cards = cardsAt(b, src) ?: return null
    val first = cards.first()
    if (cards.size == 1 && src !is Source.Foundation && fitsFoundation(b, first)) {
        val after = without(b, src, 1)
        return after.copy(foundations = after.foundations.mapIndexed { i, f -> if (i == first.suit) f + first else f })
    }
    val fromCol = (src as? Source.Tableau)?.col
    for (col in 0 until 7) {
        if (col == fromCol) continue
        val pile = b.tableau[col]
        // Moving a whole column's king to another empty column changes nothing.
        if (pile.isEmpty() && src is Source.Tableau && src.index == 0) continue
        if (fitsTableau(pile, first)) {
            val after = without(b, src, cards.size)
            return after.copy(tableau = after.tableau.mapIndexed { i, p -> if (i == col) p + cards else p })
        }
    }
    return null
}

/** Every tappable source, for hints. */
fun allSources(b: Board): List<Source> =
    listOf<Source>(Source.Waste) + (0 until 4).map { Source.Foundation(it) } +
        b.tableau.flatMapIndexed { col, p -> p.indices.filter { p[it].up }.map { Source.Tableau(col, it) } }

/** A useful move (not from the foundation back down), or null. */
fun hint(b: Board): Source? = allSources(b).firstOrNull { it !is Source.Foundation && autoMove(b, it) != null }

fun isWon(b: Board) = b.foundations.all { it.size == 13 }
