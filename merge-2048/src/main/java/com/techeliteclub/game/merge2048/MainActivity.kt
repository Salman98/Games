package com.techeliteclub.game.merge2048

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.HowToIcon
import com.techeliteclub.game.core.HowToScreen
import com.techeliteclub.game.core.GameActivity
import com.techeliteclub.game.core.GameStore
import com.techeliteclub.game.core.HomeInfo
import com.techeliteclub.game.core.Themes

class MainActivity : GameActivity() {
    override val gameTheme = Themes.M2048
    override val logoLines = listOf("2048")
    override val tagline = "Swipe. Merge. Go higher."
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val best = "%,d".format(store.get("best"))
        return HomeInfo(best, pillIsTrophy = true, label = "BEST SCORE $best", progress = null)
    }

    @Composable
    override fun Hero() {
        Box(Modifier.size(320.dp, 220.dp)) {
            Tile2048(512, 84.dp, Modifier.offset(30.dp, 100.dp).rotate(-10f))
            Tile2048(256, 84.dp, Modifier.offset(206.dp, 100.dp).rotate(10f))
            Tile2048(
                2048, 112.dp,
                Modifier.offset(104.dp, 20.dp).shadow(24.dp, RoundedCornerShape(22.dp), ambientColor = Color(0xFFFFC83C), spotColor = Color(0xFFFFC83C)),
            )
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "2048",
            rules = listOf(
                "Swipe to slide all tiles" to "Up, down, left or right.",
                "Same numbers merge" to "2 and 2 make 4, 4 and 4 make 8, and so on.",
                "Reach 2048" to "The game ends when the board is full and nothing can merge.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { HowToIcon(GameIcons.Forward) },
                { Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { Tile2048(2, 34.dp); Tile2048(2, 34.dp) } },
                { Tile2048(2048, 44.dp) },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = Merge2048Game(ads)
}
