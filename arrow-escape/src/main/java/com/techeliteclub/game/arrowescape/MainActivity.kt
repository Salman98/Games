package com.techeliteclub.game.arrowescape

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameActivity
import com.techeliteclub.game.core.GameStore
import com.techeliteclub.game.core.HomeInfo
import com.techeliteclub.game.core.Themes

class MainActivity : GameActivity() {
    override val gameTheme = Themes.Arrow
    override val logoLines = listOf("ARROW", "ESCAPE")
    override val tagline = "Clear the board, one arrow at a time."
    override val hasLevels = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        Box(Modifier.size(320.dp, 220.dp)) {
            Box(
                Modifier.offset(190.dp, 132.dp).size(110.dp, 10.dp)
                    .background(Brush.horizontalGradient(listOf(Color(0xB3FF9A52), Color.Transparent)), RoundedCornerShape(5.dp))
            )
            ArrowTile(Dir.UP, 78.dp, Modifier.offset(30.dp, 60.dp).rotate(-10f))
            ArrowTile(Dir.LEFT, 64.dp, Modifier.offset(110.dp, 10.dp).rotate(8f))
            ArrowTile(Dir.RIGHT, 90.dp, Modifier.offset(115.dp, 95.dp))
            ArrowTile(Dir.DOWN, 70.dp, Modifier.offset(225.dp, 40.dp).rotate(12f))
        }
    }

    @Composable
    override fun Game(ads: Ads) = ArrowEscapeGame(ads)
}
