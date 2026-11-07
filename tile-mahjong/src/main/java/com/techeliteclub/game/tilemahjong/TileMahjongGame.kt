package com.techeliteclub.game.tilemahjong

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlin.random.Random

@Composable
fun TileMahjongGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val rng = remember { Random(System.nanoTime()) }
    var level by remember { mutableIntStateOf(store.get("level", 1)) }
    val start = remember(level) { newLevel(level, Random(level.toLong())).tiles }
    var tiles by remember(level) { mutableStateOf(start) }
    var selected by remember(level) { mutableStateOf<Pos?>(null) }
    var hint by remember(level) { mutableStateOf<Pair<Pos, Pos>?>(null) }
    val available = freePairs(tiles).size
    val stuck = tiles.isNotEmpty() && available == 0

    fun restart() { tiles = start; selected = null; hint = null }

    fun tap(p: Pos) {
        if (!isFree(tiles, p)) return
        val s = selected
        when {
            s == null || s == p -> { selected = if (s == p) null else p; feedback.tap() }
            tiles[s] == tiles[p] -> { tiles = remove(tiles, s, p); selected = null; hint = null; feedback.good() }
            else -> { selected = p; feedback.bad() }
        }
    }

    GameScaffold("Tile Mahjong", "Level $level · ${tiles.size} tiles · $available matches", ads, ::restart, "Hint",
        onHint = { hint = freePairs(tiles).firstOrNull() }) {
        Text("Match two free tiles: nothing on top, and an open left or right side.",
            style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val w = maxWidth / COLS
            val h = w * 1.2f
            val lift = 5.dp // top layer is drawn slightly up-left to look stacked
            Box(Modifier.size(maxWidth, h * ROWS)) {
                for (layer in 0..1) {
                    // Row by row so each tile's thickness is covered by the tile drawn after it.
                    val onLayer = tiles.entries.filter { it.key.layer == layer }.sortedWith(compareBy({ it.key.r }, { it.key.c }))
                    for ((p, sym) in onLayer) key(p) {
                        val free = isFree(tiles, p)
                        val highlighted = p == selected || (hint?.let { p == it.first || p == it.second } == true)
                        val scale by animateFloatAsState(if (p == selected) 1.08f else 1f, label = "pop")
                        // Darker block under each tile gives it thickness.
                        Box(
                            Modifier.offset(w * p.c - lift * layer + 3.dp, h * p.r - lift * layer + 3.dp)
                                .size(w, h).padding(2.dp)
                                .background(Color(0xFF8D6E63), RoundedCornerShape(6.dp))
                        )
                        Box(
                            Modifier.offset(w * p.c - lift * layer, h * p.r - lift * layer)
                                .size(w, h).padding(2.dp)
                                .scale(scale)
                                .shadow((2 + 3 * layer).dp, RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFF8E1), RoundedCornerShape(6.dp))
                                .border(
                                    if (highlighted) 3.dp else 1.dp,
                                    if (highlighted) MaterialTheme.colorScheme.primary else Color(0xFFBCAAA4),
                                    RoundedCornerShape(6.dp),
                                )
                                .alpha(if (free) 1f else 0.6f)
                                .clickable { tap(p) },
                            contentAlignment = Alignment.Center,
                        ) { Text(SYMBOLS[sym], fontSize = (w.value * 0.45f).sp) }
                    }
                }
            }
        }
    }

    when {
        tiles.isEmpty() -> ResultDialog("Cleared!", "Level $level complete.", "Next level", {
            ads.levelDone()
            level++
            store.put("level", level)
        }, emoji = "🀄")
        stuck -> ResultDialog("No matches left", "Shuffle the tiles or start the level again.", "Restart",
            { ads.levelDone(); restart() },
            secondButton = if (ads.rewardedReady) "▶ Shuffle" else null,
            onSecond = { ads.showRewarded { tiles = shuffle(tiles, rng); selected = null; hint = null } })
    }
}
