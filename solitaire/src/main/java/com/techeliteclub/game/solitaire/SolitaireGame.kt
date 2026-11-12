package com.techeliteclub.game.solitaire

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.Hint
import com.techeliteclub.game.core.Pill
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.Tool
import com.techeliteclub.game.core.displayStyle
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlin.random.Random

private val CARD_SHAPE = RoundedCornerShape(7.dp)
private val RED_INK = Color(0xFFC8102E)
private val BLACK_INK = Color(0xFF1A1A2E)
private val GOLD_LINE = Color(0x73FFE696)

/** One card (face up, face down, or an empty spot showing [emptyLabel]). */
@Composable
fun PlayingCard(card: Card?, w: Dp, h: Dp, modifier: Modifier = Modifier, glow: Boolean = false, emptyLabel: String = "") {
    when {
        card == null -> Box(
            modifier.size(w, h).padding(2.dp).border(2.dp, GOLD_LINE, CARD_SHAPE),
            contentAlignment = Alignment.Center,
        ) { Text(emptyLabel, color = GOLD_LINE, fontSize = (w.value * 0.45f).sp) }
        !card.up -> Box(
            modifier.size(w, h).padding(2.dp).shadow(3.dp, CARD_SHAPE)
                .background(Brush.linearGradient(listOf(Color(0xFF9A1834), Color(0xFF7A1028), Color(0xFF9A1834))), CARD_SHAPE)
                .border(2.5.dp, Color(0xFFFFF7E6), CARD_SHAPE),
            contentAlignment = Alignment.Center,
        ) { Box(Modifier.size(w * 0.32f).background(Color(0x80FFD75A), RoundedCornerShape(50))) }
        else -> {
            val ink = if (card.red) RED_INK else BLACK_INK
            val suit = "♠♥♦♣"[card.suit].toString()
            Box(
                modifier.size(w, h).padding(2.dp)
                    .shadow(if (glow) 12.dp else 3.dp, CARD_SHAPE, ambientColor = if (glow) Color(0xFFFFD84A) else Color.Black, spotColor = if (glow) Color(0xFFFFD84A) else Color.Black)
                    .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF7F3EA))), CARD_SHAPE)
                    .border(if (glow) 3.dp else 1.dp, if (glow) Color(0xFFFFD84A) else Color(0xFFE8E1CF), CARD_SHAPE)
                    .semantics { contentDescription = card.label },
            ) {
                Text(card.label.dropLast(1), Modifier.padding(start = 4.dp, top = 1.dp), style = displayStyle((w.value * 0.3f).sp), color = ink)
                Text(suit, Modifier.padding(start = 4.dp, top = (w.value * 0.32f).dp), fontSize = (w.value * 0.26f).sp, color = ink)
                Text(suit, Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 2.dp), fontSize = (w.value * 0.52f).sp, color = ink)
            }
        }
    }
}

@Composable
fun SolitaireGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val rng = remember { Random(System.nanoTime()) }
    // Resume the deal from last time (it is saved after every move).
    val saved = remember { decodeBoard(store.getString("save")) }
    var board by remember { mutableStateOf(saved ?: deal(rng)) }
    var history by remember { mutableStateOf(listOf<Board>()) }
    var hinted by remember { mutableStateOf<Source?>(null) }
    var wins by remember { mutableIntStateOf(store.get("wins")) }
    // Saved game counter: each new game (and each app launch) gets its own free hint.
    var dealNo by remember { mutableIntStateOf(if (saved != null) store.get("game_no") else (store.get("game_no") + 1).also { store.put("game_no", it) }) }
    var seconds by remember { mutableIntStateOf(0) }
    val won = isWon(board)
    LaunchedEffect(board) { store.putString("save", encodeBoard(board)) }
    LaunchedEffect(dealNo, won) { while (!won) { delay(1000); seconds++ } }

    fun commit(next: Board) { history = (history + listOf(board)).takeLast(200); board = next; hinted = null }
    fun tap(src: Source) {
        val next = autoMove(board, src)
        if (next == null) { feedback.bad(); return }
        val homeBefore = board.foundations.sumOf { it.size }
        commit(next)
        if (next.foundations.sumOf { it.size } > homeBefore) feedback.clear() else feedback.good()
    }
    fun newDeal() { ads.levelDone(); board = deal(rng); history = emptyList(); hinted = null; dealNo++; store.put("game_no", dealNo); seconds = 0 }

    GameScaffold(
        ads, "SOLITAIRE",
        pills = listOf(Pill("MOVES ${history.size}"), Pill("%d:%02d".format(seconds / 60, seconds % 60), GameIcons.Clock)),
        tools = listOf(
            Tool("Undo", GameIcons.Undo, enabled = history.isNotEmpty()) { board = history.last(); history = history.dropLast(1); hinted = null },
            Tool("New deal", GameIcons.Restart, onClick = ::newDeal),
            Tool("Auto", GameIcons.Wand, enabled = canAutoFinish(board)) { commit(autoFinish(board)) },
        ),
        // Off while a hint is still showing: another one would point at the same thing and waste the player's ad.
        hint = Hint("Hint", GameIcons.Bulb, levelKey = dealNo, enabled = hinted == null) { hinted = hint(board) },
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val w = maxWidth / 7
            val h = w * 1.4f
            val top = h + 14.dp
            // Top row: stock, waste, gap, four foundations.
            PlayingCard(board.stock.lastOrNull(), w, h, Modifier.clickable { commit(draw(board)); feedback.tap() }, emptyLabel = "↻")
            PlayingCard(board.waste.lastOrNull(), w, h, Modifier.offset(w, 0.dp).clickable { tap(Source.Waste) }, glow = hinted == Source.Waste)
            for (s in 0 until 4) {
                PlayingCard(
                    board.foundations[s].lastOrNull(), w, h,
                    Modifier.offset(w * (3 + s), 0.dp).clickable { tap(Source.Foundation(s)) },
                    emptyLabel = "♠♥♦♣"[s].toString(),
                )
            }
            // Tableau: hidden cards overlap tightly, face-up cards show their corner.
            for ((col, pile) in board.tableau.withIndex()) {
                if (pile.isEmpty()) PlayingCard(null, w, h, Modifier.offset(w * col, top))
                val step = min(h * 0.3f, (maxHeight - top - h) / maxOf(pile.size, 1)).coerceAtLeast(6.dp)
                pile.forEachIndexed { i, card ->
                    val src = Source.Tableau(col, i)
                    PlayingCard(card, w, h, Modifier.offset(w * col, top + step * i).clickable { tap(src) }, glow = hinted == src)
                }
            }
        }
    }

    if (won) {
        val stars = when { history.size < 120 -> 3; history.size < 180 -> 2; else -> 1 }
        ResultDialog("You won!", "Moves: ${history.size}\nTime: %d:%02d".format(seconds / 60, seconds % 60), "New deal", {
            wins++
            store.put("wins", wins)
            newDeal()
        }, stars = stars)
    }
}
