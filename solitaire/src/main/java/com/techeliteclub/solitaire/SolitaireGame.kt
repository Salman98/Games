package com.techeliteclub.solitaire

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.techeliteclub.core.Ads
import com.techeliteclub.core.GameScaffold
import com.techeliteclub.core.ResultDialog
import com.techeliteclub.core.rememberStore
import kotlin.random.Random

@Composable
fun SolitaireGame(ads: Ads) {
    val store = rememberStore()
    val rng = remember { Random(System.nanoTime()) }
    var board by remember { mutableStateOf(deal(rng)) }
    var history by remember { mutableStateOf(listOf<Board>()) }
    var hinted by remember { mutableStateOf<Source?>(null) }
    var wins by remember { mutableIntStateOf(store.get("wins")) }
    val won = isWon(board)

    fun commit(next: Board) { history = (history + listOf(board)).takeLast(200); board = next; hinted = null }
    fun tap(src: Source) { autoMove(board, src)?.let(::commit) }
    fun newDeal() { ads.levelDone(); board = deal(rng); history = emptyList(); hinted = null }

    GameScaffold("Solitaire", "Wins: $wins", ads, ::newDeal, "Hint", { hinted = hint(board) }) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val w = maxWidth / 7
            val h = w * 1.4f
            val top = h + 12.dp
            // Top row: stock, waste, undo, foundations.
            CardSlot(w, h, 0.dp, 0.dp, board.stock.lastOrNull(), false, emptyLabel = "↻", onClick = { commit(draw(board)) })
            CardSlot(w, h, w, 0.dp, board.waste.lastOrNull(), hinted == Source.Waste) { tap(Source.Waste) }
            TextButton(onClick = {
                history.lastOrNull()?.let { board = it; history = history.dropLast(1) }
            }, enabled = history.isNotEmpty(), modifier = Modifier.offset(w * 2, 0.dp).size(w, h)) { Text("Undo") }
            for (s in 0 until 4) {
                CardSlot(w, h, w * (3 + s), 0.dp, board.foundations[s].lastOrNull(), false, "♠♥♦♣"[s].toString()) {
                    tap(Source.Foundation(s))
                }
            }
            // Tableau: hidden cards overlap tightly, face-up cards show their label.
            for ((col, pile) in board.tableau.withIndex()) {
                if (pile.isEmpty()) CardSlot(w, h, w * col, top, null, false) {}
                val step = min(h * 0.3f, (maxHeight - top - h) / maxOf(pile.size, 1)).coerceAtLeast(6.dp)
                pile.forEachIndexed { i, card ->
                    val src = Source.Tableau(col, i)
                    CardSlot(w, h, w * col, top + step * i, card, hinted == src) { tap(src) }
                }
            }
        }
    }

    if (won) {
        ResultDialog("You won!", "All cards home.", "New deal", {
            wins++
            store.put("wins", wins)
            newDeal()
        })
    }
}

@Composable
private fun CardSlot(
    w: Dp, h: Dp, x: Dp, y: Dp, card: Card?, highlighted: Boolean, emptyLabel: String = "", onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    val border = if (highlighted) MaterialTheme.colorScheme.primary else Color.Gray
    Box(
        Modifier.offset(x, y).size(w, h).padding(2.dp)
            .background(
                when {
                    card == null -> Color.Transparent
                    card.up -> Color.White
                    else -> Color(0xFF1565C0) // card back
                },
                shape,
            )
            .border(if (highlighted) 3.dp else 1.dp, border, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.TopStart,
    ) {
        when {
            card == null -> Text(emptyLabel, Modifier.align(Alignment.Center), color = Color.Gray)
            card.up -> Text(
                card.label, Modifier.padding(3.dp), fontSize = (w.value * 0.28f).sp,
                color = if (card.red) Color(0xFFC62828) else Color.Black,
            )
        }
    }
}
