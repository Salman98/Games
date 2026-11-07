package com.techeliteclub.game.tilemahjong

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
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
    override fun Game(ads: Ads) = TileMahjongGame(ads)
}
