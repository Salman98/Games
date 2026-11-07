package com.techeliteclub.game.blockpuzzle

import kotlin.random.Random

const val SIZE = 8

// Shapes drawn as text: rows split by '|', 'X' = filled.
private val SHAPE_ART = listOf(
    "X", "XX", "X|X", "XXX", "X|X|X", "XX|XX", "XXXX", "X|X|X|X", "XXXXX", "X|X|X|X|X", "XXX|XXX|XXX",
    "XX|X.", "XX|.X", "X.|XX", ".X|XX",
    "XXX|X..|X..", "XXX|..X|..X", "X..|X..|XXX", "..X|..X|XXX",
    "XXX|.X.", ".X.|XXX", "X.|XX|X.", ".X|XX|.X",
    "XX.|.XX", ".XX|XX.", "X.|XX|.X", ".X|XX|X.",
)

fun parseShape(art: String): List<Pair<Int, Int>> =
    art.split('|').flatMapIndexed { r, row -> row.mapIndexedNotNull { c, ch -> if (ch == 'X') r to c else null } }

val SHAPES = SHAPE_ART.map(::parseShape)

/** [color] is 1..6; 0 on the board means empty. */
data class Piece(val cells: List<Pair<Int, Int>>, val color: Int)

fun randomPieces(rng: Random) = List(3) { Piece(SHAPES.random(rng), 1 + rng.nextInt(6)) }

/** Board is a flat array of SIZE*SIZE cells (row-major). */
fun canPlace(board: IntArray, p: Piece, r: Int, c: Int) = p.cells.all { (dr, dc) ->
    val rr = r + dr
    val cc = c + dc
    rr in 0 until SIZE && cc in 0 until SIZE && board[rr * SIZE + cc] == 0
}

fun fitsAnywhere(board: IntArray, p: Piece) =
    (0 until SIZE).any { r -> (0 until SIZE).any { c -> canPlace(board, p, r, c) } }

/** [cleared] holds the cell indexes emptied by full lines (for the flash animation). */
class PlaceResult(val board: IntArray, val linesCleared: Int, val points: Int, val cleared: Set<Int>)

/** Places [p] with its top-left at (r, c), clears full rows and columns. Caller checks [canPlace] first. */
fun place(board: IntArray, p: Piece, r: Int, c: Int): PlaceResult {
    val b = board.copyOf()
    for ((dr, dc) in p.cells) b[(r + dr) * SIZE + c + dc] = p.color
    val fullRows = (0 until SIZE).filter { row -> (0 until SIZE).all { b[row * SIZE + it] != 0 } }
    val fullCols = (0 until SIZE).filter { col -> (0 until SIZE).all { b[it * SIZE + col] != 0 } }
    val cleared = fullRows.flatMap { row -> (0 until SIZE).map { row * SIZE + it } }.toSet() +
        fullCols.flatMap { col -> (0 until SIZE).map { it * SIZE + col } }
    for (i in cleared) b[i] = 0
    val lines = fullRows.size + fullCols.size
    return PlaceResult(b, lines, p.cells.size + 10 * lines * lines, cleared) // multi-line clears score more
}

/** Saved game as text: "board;score;piece|piece|piece" where a piece is "color:r.c,r.c" or "-" (used). */
fun encodeGame(board: IntArray, pieces: List<Piece?>, score: Int): String =
    board.joinToString(",") + ";" + score + ";" +
        pieces.joinToString("|") { p -> p?.let { it.color.toString() + ":" + it.cells.joinToString(",") { (r, c) -> "$r.$c" } } ?: "-" }

class SavedGame(val board: IntArray, val pieces: List<Piece?>, val score: Int)

/** Null when the text is missing or damaged (then a new game starts). */
fun decodeGame(text: String?): SavedGame? = runCatching {
    val (b, s, p) = text!!.split(";")
    val board = b.split(",").map { it.toInt() }.toIntArray()
    require(board.size == SIZE * SIZE)
    val pieces = p.split("|").map { part ->
        if (part == "-") null else {
            val (color, cells) = part.split(":")
            Piece(cells.split(",").map { rc -> rc.split(".").let { it[0].toInt() to it[1].toInt() } }, color.toInt())
        }
    }
    require(pieces.size == 3)
    SavedGame(board, pieces, s.toInt())
}.getOrNull()
