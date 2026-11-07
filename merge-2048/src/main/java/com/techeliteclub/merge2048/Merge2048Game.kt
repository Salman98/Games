package com.techeliteclub.merge2048

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.core.Ads
import com.techeliteclub.core.GameScaffold
import com.techeliteclub.core.ResultDialog
import com.techeliteclub.core.rememberStore
import kotlin.math.abs
import kotlin.random.Random

private val TILE_COLORS = listOf(
    0xFFEEE4DA, 0xFFEDE0C8, 0xFFF2B179, 0xFFF59563, 0xFFF67C5F, 0xFFF65E3B,
    0xFFEDCF72, 0xFFEDCC61, 0xFFEDC850, 0xFFEDC53F, 0xFFEDC22E, 0xFF3C3A32,
).map { Color(it) }

@Composable
fun Merge2048Game(ads: Ads) {
    val store = rememberStore()
    val rng = remember { Random(System.nanoTime()) }
    var board by remember { mutableStateOf(newBoard(rng)) }
    var score by remember { mutableIntStateOf(0) }
    var best by remember { mutableIntStateOf(store.get("best")) }
    var undo by remember { mutableStateOf<Pair<IntArray, Int>?>(null) }
    val over = !canMove(board)

    fun swipe(dir: Dir) {
        val r = slide(board, dir)
        if (!r.moved) return
        undo = board to score
        board = spawn(r.board, rng)
        score += r.gained
        if (score > best) { best = score; store.put("best", best) }
    }

    fun restart() { board = newBoard(rng); score = 0; undo = null }
    fun undoMove() { undo?.let { (b, s) -> board = b; score = s; undo = null } }

    GameScaffold("2048 Merge", "Score $score · Best $best", ads, ::restart, "Undo",
        onHint = if (undo != null) ::undoMove else null) {
        Text("Swipe to slide. Equal tiles merge. Reach 2048!", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        BoxWithConstraints(
            Modifier.fillMaxWidth().aspectRatio(1f)
                .background(Color(0xFFBBADA0), RoundedCornerShape(8.dp)).padding(4.dp)
                .pointerInput(Unit) {
                    var total = Offset.Zero
                    detectDragGestures(
                        onDragStart = { total = Offset.Zero },
                        onDragEnd = {
                            if (maxOf(abs(total.x), abs(total.y)) > 40f) {
                                swipe(
                                    if (abs(total.x) > abs(total.y)) { if (total.x > 0) Dir.RIGHT else Dir.LEFT }
                                    else if (total.y > 0) Dir.DOWN else Dir.UP
                                )
                            }
                        },
                    ) { change, amount -> change.consume(); total += amount }
                }
        ) {
            val cell = maxWidth / N
            Column {
                for (r in 0 until N) Row {
                    for (c in 0 until N) {
                        val v = board[r * N + c]
                        val tier = if (v == 0) -1 else Integer.numberOfTrailingZeros(v) - 1
                        Box(
                            Modifier.size(cell).padding(4.dp).background(
                                if (v == 0) Color(0xFFCDC1B4) else TILE_COLORS[minOf(tier, TILE_COLORS.size - 1)],
                                RoundedCornerShape(6.dp),
                            ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (v != 0) Text(
                                "$v", fontWeight = FontWeight.Bold,
                                fontSize = (cell.value * if (v < 1000) 0.35f else 0.25f).sp,
                                color = if (v <= 4) Color(0xFF776E65) else Color.White,
                            )
                        }
                    }
                }
            }
        }
    }

    if (over) {
        ResultDialog("No moves left", "Score: $score", "Play again", { ads.levelDone(); restart() },
            secondButton = if (ads.rewardedReady && undo != null) "▶ Undo" else null,
            onSecond = { ads.showRewarded(::undoMove) })
    }
}
