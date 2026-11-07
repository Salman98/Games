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
import com.techeliteclub.game.core.GameActivity
import com.techeliteclub.game.core.GameStore
import com.techeliteclub.game.core.HomeInfo
import com.techeliteclub.game.core.Themes

class MainActivity : GameActivity() {
    override val gameTheme = Themes.M2048
    override val logoLines = listOf("2048")
    override val tagline = "Swipe. Merge. Go higher."

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
    override fun Game(ads: Ads) = Merge2048Game(ads)
}
