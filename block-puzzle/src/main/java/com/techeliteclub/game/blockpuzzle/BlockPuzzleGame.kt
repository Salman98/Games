package com.techeliteclub.game.blockpuzzle

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.Bezel
import com.techeliteclub.game.core.GEMS
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.GradientText
import com.techeliteclub.game.core.Hint
import com.techeliteclub.game.core.Pill
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.Tool
import com.techeliteclub.game.core.gem
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

private val TILE = RoundedCornerShape(7.dp)

private class Snapshot(val board: IntArray, val pieces: List<Piece?>, val score: Int)

@Composable
fun BlockPuzzleGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val density = LocalDensity.current
    val rng = remember { Random(System.nanoTime()) }
    // Resume the game from last time (it is saved after every move).
    val saved = remember { decodeGame(store.getString("save")) }
    var board by remember { mutableStateOf(saved?.board ?: IntArray(SIZE * SIZE)) }
    var pieces by remember { mutableStateOf(saved?.pieces ?: randomPieces(rng)) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(saved?.score ?: 0) }
    var best by remember { mutableIntStateOf(store.get("best")) }
    // Saved game counter: each new game (and each app launch) gets its own free hint.
    var gameNo by remember { mutableIntStateOf(if (saved != null) store.get("game_no") else (store.get("game_no") + 1).also { store.put("game_no", it) }) }
    var undo by remember { mutableStateOf<Snapshot?>(null) }
    var flash by remember { mutableStateOf(setOf<Int>()) }
    var popup by remember { mutableStateOf<Pair<String, String>?>(null) }
    var popupId by remember { mutableIntStateOf(0) }

    // Drag state, all in root (screen) pixels.
    var dragIndex by remember { mutableStateOf<Int?>(null) }
    var finger by remember { mutableStateOf(Offset.Zero) }
    var rootOrigin by remember { mutableStateOf(Offset.Zero) }
    var boardOrigin by remember { mutableStateOf(Offset.Zero) }
    var cellPx by remember { mutableFloatStateOf(1f) }
    val trayOrigins = remember { Array(3) { Offset.Zero } }
    val liftPx = with(density) { 70.dp.toPx() } // dragged piece floats above the finger so it stays visible

    val gameOver = pieces.filterNotNull().none { fitsAnywhere(board, it) }

    LaunchedEffect(board, pieces, score) { store.putString("save", encodeGame(board, pieces, score)) }
    LaunchedEffect(flash) { if (flash.isNotEmpty()) { delay(300); flash = emptySet() } }
    LaunchedEffect(popupId) { if (popup != null) { delay(900); popup = null } }

    fun restart() {
        board = IntArray(SIZE * SIZE); pieces = randomPieces(rng); selected = null; score = 0; undo = null; gameNo++; store.put("game_no", gameNo)
    }

    fun newPieces() { pieces = randomPieces(rng); selected = null }

    /** Top-left board cell the dragged piece would land on. */
    fun dropCell(p: Piece): Pair<Int, Int> {
        val w = (p.cells.maxOf { it.second } + 1) * cellPx
        val h = (p.cells.maxOf { it.first } + 1) * cellPx
        val x = finger.x - w / 2 - boardOrigin.x
        val y = finger.y - h - liftPx - boardOrigin.y
        return (y / cellPx).roundToInt() to (x / cellPx).roundToInt()
    }

    fun placeAt(i: Int, r: Int, c: Int) {
        val p = pieces[i] ?: return
        if (!canPlace(board, p, r, c)) { feedback.bad(); return }
        undo = Snapshot(board, pieces, score)
        val res = place(board, p, r, c)
        board = res.board
        score += res.points
        if (score > best) { best = score; store.put("best", best) }
        if (res.linesCleared > 0) {
            flash = res.cleared
            popup = (if (res.linesCleared > 1) "COMBO ×${res.linesCleared}" else "NICE!") to "+${res.points}"
            popupId++
            feedback.clear()
        } else feedback.good()
        val left = pieces.toMutableList().also { it[i] = null }
        pieces = if (left.all { it == null }) randomPieces(rng) else left
        selected = null
    }

    // Preview while dragging: where the piece lands and which lines it would clear.
    val dragged = dragIndex?.let { pieces[it] }
    val drop = dragged?.let { dropCell(it) }
    val dropOk = dragged != null && drop != null && canPlace(board, dragged, drop.first, drop.second)
    val ghost = if (dropOk) dragged!!.cells.map { (dr, dc) -> (drop!!.first + dr) * SIZE + drop.second + dc }.toSet() else emptySet()
    val willClear = if (dropOk) place(board, dragged!!, drop!!.first, drop.second).cleared else emptySet()

    Box(Modifier.fillMaxSize().onGloballyPositioned { rootOrigin = it.positionInRoot() }) {
        GameScaffold(
            ads, "BLOCK GRID",
            pills = listOf(Pill("BEST ${"%,d".format(best)}", GameIcons.Trophy), Pill("$score")),
            tools = listOf(Tool("Undo", GameIcons.Undo, enabled = undo != null) {
                undo?.let { board = it.board; pieces = it.pieces; score = it.score; undo = null }
            }),
            hint = Hint("New pieces", GameIcons.Restart, levelKey = gameNo, onHint = ::newPieces),
        ) {
            Bezel(Modifier.fillMaxWidth().aspectRatio(1f), padding = 7.dp) {
                Box(
                    Modifier.fillMaxSize().onGloballyPositioned {
                        boardOrigin = it.positionInRoot()
                        cellPx = it.size.width / SIZE.toFloat()
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    val cell = with(density) { cellPx.toDp() }
                    Column {
                        for (r in 0 until SIZE) Row {
                            for (c in 0 until SIZE) {
                                val i = r * SIZE + c
                                val v = board[i]
                                val tap = Modifier.clickable {
                                    val s = selected ?: return@clickable
                                    val p = pieces[s] ?: return@clickable
                                    val (ar, ac) = p.cells[0] // tap mode: the tapped cell gets the piece's first block
                                    placeAt(s, r - ar, c - ac)
                                }
                                when {
                                    i in flash -> Box(tap.size(cell).padding(2.dp).background(Color.White, TILE))
                                    i in ghost -> Box(
                                        tap.size(cell).padding(2.dp).background(Color(0x59D6A8FF), TILE).border(2.dp, Color(0xFFE6C9FF), TILE)
                                    )
                                    v != 0 -> Box(
                                        tap.size(cell).padding(2.dp).gem(GEMS[v - 1].first, GEMS[v - 1].second, 7.dp)
                                            .then(if (i in willClear) Modifier.border(2.dp, Color(0xFFFFF2A0), TILE) else Modifier)
                                    )
                                    else -> Box(tap.size(cell).padding(2.dp).background(Color.White.copy(alpha = 0.05f), TILE))
                                }
                            }
                        }
                    }
                    androidx.compose.animation.AnimatedVisibility(popup != null, enter = fadeIn() + scaleIn(), exit = fadeOut()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val (big, small) = popup ?: ("" to "")
                            GradientText(big, listOf(Color(0xFFFFF7C2), Color(0xFFFFD34D), Color(0xFFFF8A00)), 44.sp, stroke = Color(0xFF4A1E00))
                            GradientText(small, listOf(Color.White, Color(0xFFE6DBFF)), 30.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                pieces.forEachIndexed { i, p ->
                    PieceView(
                        p, selected == i, p != null && fitsAnywhere(board, p), dragIndex == i,
                        Modifier
                            .onGloballyPositioned { trayOrigins[i] = it.positionInRoot() }
                            .pointerInput(i, p) {
                                if (p == null) return@pointerInput
                                detectDragGestures(
                                    onDragStart = { pos ->
                                        dragIndex = i; selected = null; finger = trayOrigins[i] + pos; feedback.tap()
                                    },
                                    onDragEnd = {
                                        val idx = dragIndex
                                        val piece = idx?.let { pieces[it] }
                                        dragIndex = null
                                        if (idx != null && piece != null) {
                                            val (r, c) = dropCell(piece)
                                            placeAt(idx, r, c)
                                        }
                                    },
                                    onDragCancel = { dragIndex = null },
                                ) { change, amount -> change.consume(); finger += amount }
                            },
                    ) { if (p != null) { selected = if (selected == i) null else i; feedback.tap() } }
                }
            }
        }

        // The piece under the finger, drawn above everything.
        if (dragged != null) {
            val cell = with(density) { cellPx.toDp() }
            val w = (dragged.cells.maxOf { it.second } + 1) * cellPx
            val h = (dragged.cells.maxOf { it.first } + 1) * cellPx
            val topLeft = finger - rootOrigin - Offset(w / 2, h + liftPx)
            Box(Modifier.offset { IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()) }) {
                for ((r, c) in dragged.cells) {
                    Box(Modifier.offset(cell * c, cell * r).size(cell).padding(2.dp).gem(GEMS[dragged.color - 1].first, GEMS[dragged.color - 1].second, 7.dp))
                }
            }
        }

        if (gameOver && dragIndex == null) {
            ResultDialog(
                if (score >= best && score > 0) "New best!" else "No moves left",
                "Score: $score\nBest: $best", "Play again", { ads.levelDone(); restart() },
                secondButton = if (ads.rewardedReady) "▶ New pieces" else null,
                onSecond = { ads.showRewarded(::newPieces) },
                win = score >= best && score > 0,
            )
        }
    }
}

@Composable
private fun PieceView(
    p: Piece?, selected: Boolean, fits: Boolean, dragging: Boolean, modifier: Modifier, onClick: () -> Unit,
) {
    val mini: Dp = 22.dp
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier.size(104.dp)
            .background(Color.White.copy(alpha = 0.06f), shape)
            .border(if (selected) 2.dp else 1.dp, if (selected) Color(0xFFFFD34D) else Color.White.copy(alpha = 0.1f), shape)
            .clickable(enabled = p != null, onClick = onClick)
            .alpha(if (dragging) 0.15f else if (fits) 1f else 0.35f),
        contentAlignment = Alignment.Center,
    ) {
        if (p != null) {
            val h = p.cells.maxOf { it.first } + 1
            val w = p.cells.maxOf { it.second } + 1
            Box(Modifier.size(mini * w, mini * h)) {
                for ((r, c) in p.cells) {
                    Box(Modifier.offset(mini * c, mini * r).size(mini).padding(1.5.dp).gem(GEMS[p.color - 1].first, GEMS[p.color - 1].second, 5.dp))
                }
            }
        }
    }
}
