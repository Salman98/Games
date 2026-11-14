package com.techeliteclub.game.bridges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.Bezel
import com.techeliteclub.game.core.GameActivity
import com.techeliteclub.game.core.GameStore
import com.techeliteclub.game.core.HomeInfo
import com.techeliteclub.game.core.HowToScreen
import com.techeliteclub.game.core.Themes
import com.techeliteclub.game.core.displayStyle

/** 3×3 with islands in the four corners: top 1 bridge, right 2, left 1. */
private val ANSWER = mapOf(Link(0, 1) to 1, Link(1, 3) to 2, Link(0, 2) to 1)
private val SAMPLE = Puzzle(3, listOf(0, 2, 6, 8), listOf(2, 3, 1, 2), ANSWER)

class MainActivity : GameActivity() {
    override val gameTheme = Themes.Bridges
    override val logoLines = listOf("BRIDGES")
    override val tagline = "Link every island. No crossings."
    override val hasLevels = true
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        Box(Modifier.size(300.dp, 230.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(250.dp).background(Brush.radialGradient(listOf(Color(0xB3FFFFFF), Color.Transparent)), CircleShape))
            Bezel(Modifier.size(190.dp).rotate(-6f), padding = 5.dp, radius = 24.dp) { BridgeBoard(SAMPLE, 166.dp, ANSWER - Link(0, 2)) }
            Box(
                Modifier.offset(96.dp, (-80).dp).rotate(12f).size(62.dp).shadow(10.dp, CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFF2C9), Color(0xFFF2C46A))), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text("2", style = displayStyle(38.sp), color = Color(0xFF5A3300)) }
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "BRIDGES",
            rules = listOf(
                "Drag between islands" to "Bridges run straight, left-right or up-down. Drag again for a double bridge.",
                "Match the number" to "Each island needs exactly that many bridges. It turns green when it has them.",
                "Join them all" to "Bridges never cross, and you can walk from any island to any other.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { BridgeBoard(SAMPLE, 84.dp, mapOf(Link(0, 1) to 1)) },
                { BridgeBoard(SAMPLE, 84.dp, ANSWER - Link(0, 2)) },
                { BridgeBoard(SAMPLE, 84.dp, ANSWER) },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = BridgesGame(ads)
}
