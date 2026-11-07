package com.techeliteclub.game.blockpuzzle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.PALETTE
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.rememberStore
import kotlin.random.Random

@Composable
fun BlockPuzzleGame(ads: Ads) {
    val store = rememberStore()
    val rng = remember { Random(System.nanoTime()) }
    var board by remember { mutableStateOf(IntArray(SIZE * SIZE)) }
    var pieces by remember { mutableStateOf<List<Piece?>>(randomPieces(rng)) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var best by remember { mutableIntStateOf(store.get("best")) }
    val gameOver = pieces.filterNotNull().none { fitsAnywhere(board, it) }

    fun restart() {
        board = IntArray(SIZE * SIZE); pieces = randomPieces(rng); selected = null; score = 0
    }

    fun newPieces() {
        pieces = randomPieces(rng); selected = null
    }

    fun tap(r: Int, c: Int) {
        val i = selected ?: return
        val p = pieces[i] ?: return
        val (ar, ac) = p.cells[0] // the tapped cell is where the piece's first block goes
        if (!canPlace(board, p, r - ar, c - ac)) return
        val res = place(board, p, r - ar, c - ac)
        board = res.board
        score += res.points
        if (score > best) { best = score; store.put("best", best) }
        val left = pieces.toMutableList().also { it[i] = null }
        pieces = if (left.all { it == null }) randomPieces(rng) else left
        selected = null
    }

    GameScaffold("Block Grid", "Score $score · Best $best", ads, ::restart, "New pieces", ::newPieces) {
        Text("Tap a piece, then tap the board", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val cell = maxWidth / SIZE
            Column {
                for (r in 0 until SIZE) Row {
                    for (c in 0 until SIZE) {
                        val v = board[r * SIZE + c]
                        Box(
                            Modifier.size(cell).padding(1.dp)
                                .background(
                                    if (v == 0) MaterialTheme.colorScheme.surfaceVariant else PALETTE[v - 1],
                                    RoundedCornerShape(4.dp),
                                )
                                .clickable { tap(r, c) }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            pieces.forEachIndexed { i, p ->
                PieceView(p, selected == i, p != null && fitsAnywhere(board, p)) { if (p != null) selected = i }
            }
        }
    }

    if (gameOver) {
        ResultDialog(
            "No moves left", "Score: $score", "Play again", { ads.levelDone(); restart() },
            secondButton = if (ads.rewardedReady) "▶ New pieces" else null,
            onSecond = { ads.showRewarded(::newPieces) },
        )
    }
}

@Composable
private fun PieceView(p: Piece?, selected: Boolean, fits: Boolean, onClick: () -> Unit) {
    val mini = 16.dp
    Box(
        Modifier.size(96.dp)
            .border(2.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(enabled = p != null, onClick = onClick)
            .alpha(if (fits) 1f else 0.35f),
        contentAlignment = Alignment.Center,
    ) {
        if (p != null) {
            val h = p.cells.maxOf { it.first } + 1
            val w = p.cells.maxOf { it.second } + 1
            Box(Modifier.size(mini * w, mini * h)) {
                for ((r, c) in p.cells) {
                    Box(
                        Modifier.offset(mini * c, mini * r).size(mini).padding(1.dp)
                            .background(PALETTE[p.color - 1], RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}
