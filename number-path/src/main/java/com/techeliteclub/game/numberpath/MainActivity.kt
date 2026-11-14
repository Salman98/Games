package com.techeliteclub.game.numberpath

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

private val SAMPLE_NUMBERS = intArrayOf(1, 0, 0, 0, 2, 0, 0, 0, 3)
private val SAMPLE_ROUTE = listOf(0, 1, 2, 5, 4, 3, 6, 7, 8)

class MainActivity : GameActivity() {
    override val gameTheme = Themes.Path
    override val logoLines = listOf("NUMBER", "PATH")
    override val tagline = "One line. Every square."
    override val hasLevels = true
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        Box(Modifier.size(300.dp, 230.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(250.dp).background(Brush.radialGradient(listOf(Color(0x73FF9AD0), Color.Transparent)), CircleShape))
            Bezel(Modifier.size(190.dp).rotate(-6f), padding = 4.dp, radius = 24.dp) {
                PathBoard(3, 170.dp, SAMPLE_NUMBERS, SAMPLE_ROUTE.take(6))
            }
            Box(
                Modifier.offset(96.dp, (-80).dp).rotate(12f).size(64.dp).shadow(10.dp, CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFE08A), Color(0xFFE89A00))), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text("1", style = displayStyle(40.sp), color = Color(0xFF3A2200)) }
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "NUMBER PATH",
            rules = listOf(
                "Start at 1 and drag" to "Slide your finger from square to square to draw the line.",
                "Pass the numbers in order" to "1, then 2, then 3, and finish on the last number.",
                "Fill every square" to "The line visits each square once. Tap the line to cut it back.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { PathBoard(3, 80.dp, SAMPLE_NUMBERS, SAMPLE_ROUTE.take(3)) },
                { PathBoard(3, 80.dp, SAMPLE_NUMBERS, SAMPLE_ROUTE.take(5)) },
                { PathBoard(3, 80.dp, SAMPLE_NUMBERS, SAMPLE_ROUTE) },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = NumberPathGame(ads)
}
