package com.techeliteclub.game.blockpuzzle

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GEMS
import com.techeliteclub.game.core.GameActivity
import com.techeliteclub.game.core.GameStore
import com.techeliteclub.game.core.HomeInfo
import com.techeliteclub.game.core.Themes
import com.techeliteclub.game.core.gem

class MainActivity : GameActivity() {
    override val gameTheme = Themes.Block
    override val logoLines = listOf("BLOCK", "GRID")
    override val tagline = "Drag. Drop. Blast lines."

    override fun homeInfo(store: GameStore) =
        HomeInfo("%,d".format(store.get("best")), pillIsTrophy = true, label = "BEST SCORE ${"%,d".format(store.get("best"))}", progress = null)

    @Composable
    override fun Hero() {
        // Floating gem blocks, like the home mockup.
        Box(Modifier.size(300.dp, 220.dp)) {
            for ((x, y, gemIndex, angle) in listOf(
                listOf(0, 40, 1, -12), listOf(80, 0, 0, 8), listOf(160, 30, 3, -4),
                listOf(40, 110, 2, 6), listOf(120, 100, 4, -10), listOf(200, 120, 5, 14),
            )) {
                val g = GEMS[gemIndex]
                Box(Modifier.offset(x.dp + 10.dp, y.dp + 10.dp).rotate(angle.toFloat()).size(64.dp).gem(g.first, g.second, 14.dp))
            }
        }
    }

    @Composable
    override fun Game(ads: Ads) = BlockPuzzleGame(ads)
}
