package com.techeliteclub.game.tilemahjong

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import com.techeliteclub.game.core.HowToScreen
import com.techeliteclub.game.core.GameActivity
import com.techeliteclub.game.core.GameStore
import com.techeliteclub.game.core.HomeInfo
import com.techeliteclub.game.core.Themes
import kotlin.math.abs

class MainActivity : GameActivity() {
    override val gameTheme = Themes.Mahjong
    override val logoLines = listOf("TILE", "MAHJONG")
    override val tagline = "Match the free tiles. Clear the table."
    override val hasLevels = true
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        // A fan of five tiles, the middle one glowing.
        Box(Modifier.size(320.dp, 220.dp)) {
            listOf(3, 0, 1, 2, 5).forEachIndexed { i, sym ->
                MahjongTile(
                    sym, 66.dp, 86.dp,
                    Modifier.offset((30 + i * 46).dp, (50 + abs(i - 2) * 14).dp).rotate((i - 2) * 9f),
                    glow = i == 2,
                )
            }
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "TILE MAHJONG",
            rules = listOf(
                "Match two tiles with the same picture" to "Tap one, then its twin.",
                "Only free tiles can be picked" to "Free: nothing on top, and the left or right side open.",
                "Dim tiles are blocked" to "Clear the table to win. Stuck? Shuffle.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { MahjongTile(0, 34.dp, 44.dp, glow = true); MahjongTile(0, 34.dp, 44.dp, glow = true) } },
                { MahjongTile(3, 34.dp, 44.dp) },
                { MahjongTile(5, 34.dp, 44.dp, dim = true) },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = TileMahjongGame(ads)
}
