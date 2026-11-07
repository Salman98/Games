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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.PALETTE
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

private val TILE_SHAPE = RoundedCornerShape(5.dp)

@Composable
fun BlockPuzzleGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val density = LocalDensity.current
    val rng = remember { Random(System.nanoTime()) }
    var board by remember { mutableStateOf(IntArray(SIZE * SIZE)) }
    var pieces by remember { mutableStateOf<List<Piece?>>(randomPieces(rng)) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var best by remember { mutableIntStateOf(store.get("best")) }
    var flash by remember { mutableStateOf(setOf<Int>()) }
    var popup by remember { mutableStateOf<String?>(null) }
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

    LaunchedEffect(flash) { if (flash.isNotEmpty()) { delay(300); flash = emptySet() } }
    LaunchedEffect(popupId) { if (popup != null) { delay(900); popup = null } }

    fun restart() {
        board = IntArray(SIZE * SIZE); pieces = randomPieces(rng); selected = null; score = 0
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
        val res = place(board, p, r, c)
        board = res.board
        score += res.points
        if (score > best) { best = score; store.put("best", best) }
        if (res.linesCleared > 0) {
            flash = res.cleared
            popup = if (res.linesCleared > 1) "Combo ×${res.linesCleared}  +${res.points}" else "+${res.points}"
            popupId++
            feedback.good()
        } else feedback.tap()
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
        GameScaffold("Block Grid", "Score $score · Best $best", ads, ::restart, "New pieces", ::newPieces) {
            Text("Drag a piece onto the board. Fill rows or columns to clear them.",
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.fillMaxWidth().aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(10.dp))
                    .padding(4.dp)
                    .onGloballyPositioned {
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
                            val color = when {
                                i in flash -> Color.White
                                i in ghost -> PALETTE[dragged!!.color - 1].copy(alpha = 0.45f)
                                v != 0 && i in willClear -> PALETTE[v - 1].copy(alpha = 0.6f)
                                v != 0 -> PALETTE[v - 1]
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            Tile(cell, color, v != 0 || i in ghost, Modifier.clickable {
                                val s = selected ?: return@clickable
                                val p = pieces[s] ?: return@clickable
                                val (ar, ac) = p.cells[0] // tap mode: the tapped cell gets the piece's first block
                                placeAt(s, r - ar, c - ac)
                            })
                        }
                    }
                }
                // Full name: inside a Column the ColumnScope version would be picked, which is not allowed in this Box.
                androidx.compose.animation.AnimatedVisibility(popup != null, enter = fadeIn() + scaleIn(), exit = fadeOut()) {
                    Text(
                        popup ?: "", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color.White,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
            }
            // Tray fills the space left, pieces centred.
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
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
                        ) { if (p != null) selected = if (selected == i) null else i }
                    }
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
                    Tile(cell, PALETTE[dragged.color - 1], true, Modifier.offset(cell * c, cell * r))
                }
            }
        }
    }

    if (gameOver && dragIndex == null) {
        ResultDialog(
            "No moves left", "Score: $score\nBest: $best", "Play again", { ads.levelDone(); restart() },
            secondButton = if (ads.rewardedReady) "▶ New pieces" else null,
            onSecond = { ads.showRewarded(::newPieces) },
            emoji = if (score >= best && score > 0) "🏆" else "🧱",
        )
    }
}

/** One square with a light top edge so filled blocks look raised. */
@Composable
private fun Tile(size: Dp, color: Color, raised: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier.size(size).padding(1.5.dp)
            .background(color, TILE_SHAPE)
            .then(if (raised) Modifier.border(1.5.dp, Color.White.copy(alpha = 0.25f), TILE_SHAPE) else Modifier)
    )
}

@Composable
private fun PieceView(
    p: Piece?, selected: Boolean, fits: Boolean, dragging: Boolean, modifier: Modifier, onClick: () -> Unit,
) {
    val mini = 22.dp
    Box(
        modifier.size(118.dp)
            .border(2.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable(enabled = p != null, onClick = onClick)
            .alpha(if (dragging) 0.15f else if (fits) 1f else 0.35f),
        contentAlignment = Alignment.Center,
    ) {
        if (p != null) {
            val h = p.cells.maxOf { it.first } + 1
            val w = p.cells.maxOf { it.second } + 1
            Box(Modifier.size(mini * w, mini * h)) {
                for ((r, c) in p.cells) Tile(mini, PALETTE[p.color - 1], true, Modifier.offset(mini * c, mini * r))
            }
        }
    }
}
