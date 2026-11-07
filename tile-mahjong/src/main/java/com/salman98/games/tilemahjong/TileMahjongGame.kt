package com.salman98.games.tilemahjong

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salman98.games.core.Ads
import com.salman98.games.core.GameScaffold
import com.salman98.games.core.ResultDialog
import com.salman98.games.core.rememberStore
import kotlin.random.Random

@Composable
fun TileMahjongGame(ads: Ads) {
    val store = rememberStore()
    val rng = remember { Random(System.nanoTime()) }
    var level by remember { mutableIntStateOf(store.get("level", 1)) }
    val start = remember(level) { newLevel(level, Random(level.toLong())).tiles }
    var tiles by remember(level) { mutableStateOf(start) }
    var selected by remember(level) { mutableStateOf<Pos?>(null) }
    var hint by remember(level) { mutableStateOf<Pair<Pos, Pos>?>(null) }
    val stuck = tiles.isNotEmpty() && freePairs(tiles).isEmpty()

    fun restart() { tiles = start; selected = null; hint = null }

    fun tap(p: Pos) {
        if (!isFree(tiles, p)) return
        val s = selected
        when {
            s == null || s == p -> selected = if (s == p) null else p
            tiles[s] == tiles[p] -> { tiles = remove(tiles, s, p); selected = null; hint = null }
            else -> selected = p
        }
    }

    GameScaffold("Tile Mahjong", "Level $level · ${tiles.size} tiles", ads, ::restart, "Hint",
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
                    for ((p, sym) in tiles) {
                        if (p.layer != layer) continue
                        val free = isFree(tiles, p)
                        val highlighted = p == selected || (hint?.let { p == it.first || p == it.second } == true)
                        Box(
                            Modifier.offset(w * p.c - lift * layer, h * p.r - lift * layer)
                                .size(w, h).padding(2.dp)
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
        })
        stuck -> ResultDialog("No matches left", "Shuffle the tiles or start the level again.", "Restart",
            { ads.levelDone(); restart() },
            secondButton = if (ads.rewardedReady) "▶ Shuffle" else null,
            onSecond = { ads.showRewarded { tiles = shuffle(tiles, rng); selected = null; hint = null } })
    }
}
