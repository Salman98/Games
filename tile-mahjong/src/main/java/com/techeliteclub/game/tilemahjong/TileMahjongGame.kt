package com.techeliteclub.game.tilemahjong

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.Bezel
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.Hint
import com.techeliteclub.game.core.Pill
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.Tool
import com.techeliteclub.game.core.rememberFeedback
import com.techeliteclub.game.core.rememberStore
import kotlin.random.Random

private val IVORY = Brush.linearGradient(listOf(Color.White, Color(0xFFF6EED8), Color(0xFFEADFC1)))

/** An ivory tile with thickness, a drawn picture, optional gold glow. */
@Composable
fun MahjongTile(symbol: Int, w: Dp, h: Dp, modifier: Modifier = Modifier, glow: Boolean = false, dim: Boolean = false) {
    val shape = RoundedCornerShape(9.dp)
    Box(modifier.size(w, h + 5.dp)) {
        Box(Modifier.offset(y = 5.dp).size(w, h).background(Color(0xFFC9B78A), shape)) // tile edge
        Box(
            Modifier.size(w, h)
                .then(if (glow) Modifier.shadow(14.dp, shape, ambientColor = Color(0xFFFFD84A), spotColor = Color(0xFFFFD84A)) else Modifier)
                .background(IVORY, shape)
                .border(if (glow) 3.dp else 1.dp, if (glow) Color(0xFFFFD84A) else Color(0xFFFFFAF0), shape)
                .graphicsLayer { alpha = if (dim) 0.72f else 1f },
            contentAlignment = Alignment.Center,
        ) { TileSymbol(symbol, Modifier.size(w * 0.66f)) }
    }
}

@Composable
fun TileMahjongGame(ads: Ads) {
    val store = rememberStore()
    val feedback = rememberFeedback()
    val rng = remember { Random(System.nanoTime()) }
    var level by remember { mutableIntStateOf(store.get("level", 1)) }
    val start = remember(level) { newLevel(level, Random(level.toLong())).tiles }
    var tiles by remember(level) { mutableStateOf(start) }
    var history by remember(level) { mutableStateOf(listOf<Tiles>()) }
    var selected by remember(level) { mutableStateOf<Pos?>(null) }
    var hint by remember(level) { mutableStateOf<Pair<Pos, Pos>?>(null) }
    val available = freePairs(tiles).size
    val stuck = tiles.isNotEmpty() && available == 0

    fun restart() { tiles = start; history = emptyList(); selected = null; hint = null }

    fun tap(p: Pos) {
        if (!isFree(tiles, p)) { feedback.bad(); return }
        val s = selected
        when {
            s == null || s == p -> { selected = if (s == p) null else p; feedback.tap() }
            tiles[s] == tiles[p] -> {
                history = history + listOf(tiles)
                tiles = remove(tiles, s, p); selected = null; hint = null; feedback.good()
            }
            else -> { selected = p; feedback.bad() }
        }
    }

    GameScaffold(
        ads, "TILE MAHJONG",
        pills = listOf(Pill("LEVEL $level"), Pill("${tiles.size} TILES · $available MATCHES")),
        tools = listOf(
            Tool("Undo", GameIcons.Undo, enabled = history.isNotEmpty()) { tiles = history.last(); history = history.dropLast(1); selected = null },
            Tool("Shuffle", GameIcons.Shuffle, enabled = tiles.isNotEmpty()) { tiles = shuffle(tiles, rng); selected = null; hint = null },
        ),
        hint = Hint("Hint", GameIcons.Bulb, levelKey = level) { hint = freePairs(tiles).firstOrNull() },
    ) {
        Bezel(Modifier.fillMaxWidth().height(400.dp), padding = 10.dp) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val w = maxWidth / COLS
                val h = minOf(w * 1.28f, (maxHeight - 10.dp) / ROWS)
                val lift = 6.dp // top layer is drawn slightly up-left to look stacked
                for (layer in 0..1) {
                    // Row by row so each tile's thickness is covered by the tile drawn after it.
                    val onLayer = tiles.entries.filter { it.key.layer == layer }.sortedWith(compareBy({ it.key.r }, { it.key.c }))
                    for ((p, sym) in onLayer) key(p) {
                        val free = isFree(tiles, p)
                        val glow = p == selected || (hint?.let { p == it.first || p == it.second } == true)
                        val scale by animateFloatAsState(if (p == selected) 1.08f else 1f, label = "pop")
                        MahjongTile(
                            sym, w - 3.dp, h - 8.dp,
                            Modifier.offset(w * p.c - lift * layer, h * p.r - lift * layer)
                                .scale(scale)
                                .clickable { tap(p) }
                                .semantics { contentDescription = "Tile ${sym + 1}" + if (free) ", free" else ", blocked" },
                            glow = glow, dim = !free,
                        )
                    }
                }
            }
        }
    }

    when {
        tiles.isEmpty() -> ResultDialog("Table cleared", "Level: $level", "Next level", {
            ads.levelDone()
            store.put("stars", store.get("stars") + 3)
            level++
            store.put("level", level)
        })
        stuck -> ResultDialog(
            "No matches left", "Shuffle the tiles or start the level again.", "Restart", { ads.levelDone(); restart() },
            secondButton = "Shuffle",
            onSecond = { tiles = shuffle(tiles, rng); selected = null; hint = null },
            win = false,
        )
    }
}
