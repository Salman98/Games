package com.techeliteclub.game.merge2048

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.Bezel
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.GradientText
import com.techeliteclub.game.core.Hint
import com.techeliteclub.game.core.Pill
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.Tool
import com.techeliteclub.game.core.displayStyle
import com.techeliteclub.game.core.gem
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

/** Tile colours by value: light top, deep base, text colour. */
private fun tileColors(v: Int): Triple<Color, Color, Color> = when (v) {
    2 -> Triple(Color(0xFFFFF7E8), Color(0xFFF1E2C4), Color(0xFF7A5230))
    4 -> Triple(Color(0xFFFFF0D2), Color(0xFFF0D49A), Color(0xFF7A5230))
    8 -> Triple(Color(0xFFFFC58A), Color(0xFFFF9440), Color.White)
    16 -> Triple(Color(0xFFFFAD6E), Color(0xFFFF7A2A), Color.White)
    32 -> Triple(Color(0xFFFF9A7A), Color(0xFFFF5A3D), Color.White)
    64 -> Triple(Color(0xFFFF7A6A), Color(0xFFE5383B), Color.White)
    128 -> Triple(Color(0xFFFFF0A0), Color(0xFFFFC533), Color.White)
    256 -> Triple(Color(0xFFFFE680), Color(0xFFFFB300), Color.White)
    512 -> Triple(Color(0xFFFFD966), Color(0xFFFF9500), Color.White)
    1024 -> Triple(Color(0xFFFFE08A), Color(0xFFFF7A00), Color.White)
    else -> Triple(Color(0xFFFFF3B0), Color(0xFFFFC533), Color.White) // 2048 and beyond
}

@Composable
fun Tile2048(v: Int, size: Dp, modifier: Modifier = Modifier) {
    val (top, base, ink) = tileColors(v)
    val fontSize = (size.value * when { v < 100 -> 0.46f; v < 1000 -> 0.36f; else -> 0.28f }).sp
    Box(modifier.size(size).shadow(4.dp, RoundedCornerShape(size * 0.2f)).gem(top, base, size * 0.2f), contentAlignment = Alignment.Center) {
        Text(
            "$v",
            style = displayStyle(fontSize).copy(shadow = if (ink == Color.White) Shadow(Color(0x40000000), Offset(0f, 3f), 0f) else null),
            color = ink,
        )
    }
}

@Composable
fun Merge2048Game(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val rng = remember { Random(System.nanoTime()) }
    // Resume the game from last time (it is saved after every move).
    val saved = remember { decodeGame(store.getString("save")) }
    var board by remember { mutableStateOf(saved?.first ?: newBoard(rng)) }
    var score by remember { mutableIntStateOf(saved?.second ?: 0) }
    var best by remember { mutableIntStateOf(store.get("best")) }
    var undo by remember { mutableStateOf<Pair<IntArray, Int>?>(null) }
    var celebrated by remember { mutableStateOf(false) }
    // Saved game counter: each new game (and each app launch) gets its own free hint.
    var gameNo by remember { mutableIntStateOf(if (saved != null) store.get("game_no") else (store.get("game_no") + 1).also { store.put("game_no", it) }) }
    var popup by remember { mutableStateOf<String?>(null) }
    var popupId by remember { mutableIntStateOf(0) }
    val over = !canMove(board)

    LaunchedEffect(board, score) { store.putString("save", encodeGame(board, score)) }
    LaunchedEffect(popupId) { if (popup != null) { delay(800); popup = null } }

    fun swipe(dir: Dir) {
        val r = slide(board, dir)
        if (!r.moved) return
        undo = board to score
        board = spawn(r.board, rng)
        score += r.gained
        if (score > best) { best = score; store.put("best", best) }
        if (r.gained >= 64) { popup = "+${r.gained}!"; popupId++; feedback.clear() } else if (r.gained > 0) feedback.good() else feedback.tap()
    }

    fun restart() { board = newBoard(rng); score = 0; undo = null; celebrated = false; gameNo++; store.put("game_no", gameNo) }
    fun undoMove() { undo?.let { (b, s) -> board = b; score = s; undo = null } }

    GameScaffold(
        ads, "2048",
        pills = listOf(Pill("SCORE ${"%,d".format(score)}"), Pill("%,d".format(best), GameIcons.Trophy)),
        tools = listOf(Tool("Restart", GameIcons.Restart, onClick = ::restart)),
        hint = Hint("Undo", GameIcons.Undo, levelKey = gameNo, enabled = undo != null, onHint = ::undoMove),
    ) {
        Bezel(
            Modifier.fillMaxWidth().aspectRatio(1f)
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
                .semantics { contentDescription = "Board. Swipe to move tiles." },
            padding = 8.dp,
        ) {
            BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val cell = maxWidth / N
                Column {
                    for (r in 0 until N) Row {
                        for (c in 0 until N) {
                            val v = board[r * N + c]
                            Box(Modifier.size(cell).padding(5.dp).background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                // New or changed tiles pop in.
                                if (v != 0) key(r * N + c, v) {
                                    val pop = remember { Animatable(0.7f) }
                                    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 900f)) }
                                    Tile2048(v, cell - 10.dp, Modifier.scale(pop.value))
                                }
                            }
                        }
                    }
                }
                androidx.compose.animation.AnimatedVisibility(popup != null, enter = fadeIn() + scaleIn(), exit = fadeOut()) {
                    GradientText(popup ?: "", listOf(Color(0xFFFFF7C2), Color(0xFFFFD34D), Color(0xFFFF8A00)), 40.sp, stroke = Color(0xFF4A1E00))
                }
            }
        }
    }

    if (over) {
        ResultDialog(
            if (score >= best && score > 0) "New best!" else "No moves left",
            "Score: ${"%,d".format(score)}\nBest: ${"%,d".format(best)}", "Play again", { ads.levelDone(); restart() },
            secondButton = if (ads.rewardedReady && undo != null) "▶ Undo" else null,
            onSecond = { ads.showRewarded(::undoMove) },
            win = score >= best && score > 0,
        )
    } else if (!celebrated && board.any { it >= 2048 }) {
        ResultDialog("You made 2048!", "Keep going for a higher score.", "Keep going", { celebrated = true })
    }
}
