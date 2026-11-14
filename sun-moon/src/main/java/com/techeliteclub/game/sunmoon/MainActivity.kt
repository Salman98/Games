package com.techeliteclub.game.sunmoon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.Bezel
import com.techeliteclub.game.core.GameActivity
import com.techeliteclub.game.core.GameStore
import com.techeliteclub.game.core.HomeInfo
import com.techeliteclub.game.core.HowToScreen
import com.techeliteclub.game.core.Themes

/** A small fixed board for the home and how-to screens: one row of [cells] per string, s = sun, m = moon, . = empty. */
@Composable
private fun Sample(rows: List<String>, cell: Dp) {
    Column(Modifier.clip(RoundedCornerShape(10.dp))) {
        for (row in rows) Row {
            for (ch in row) {
                Box(Modifier.size(cell).background(CELL).border(0.5.dp, Color.White.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Symbol(when (ch) { 's' -> SUN; 'm' -> MOON; else -> EMPTY }, cell * 0.7f)
                }
            }
        }
    }
}

class MainActivity : GameActivity() {
    override val gameTheme = Themes.SunMoon
    override val logoLines = listOf("SUN &", "MOON")
    override val tagline = "Balance every row and column."
    override val hasLevels = true
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        Box(Modifier.size(300.dp, 230.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(250.dp).background(Brush.radialGradient(listOf(Color(0x73FFD65A), Color.Transparent)), CircleShape))
            Bezel(Modifier.size(190.dp).rotate(-6f), padding = 4.dp, radius = 24.dp) {
                Sample(listOf("smsm", "msms", "mssm", "smms"), 43.dp)
            }
            Symbol(SUN, 100.dp, Modifier.offset(96.dp, (-78).dp).rotate(14f))
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "SUN & MOON",
            rules = listOf(
                "Tap a square" to "One tap is a sun, two taps a moon, three taps empty again.",
                "Same number of each" to "Every row and every column has as many suns as moons.",
                "Never three in a row" to "Two suns or two moons may touch, three may not.",
                "= and × signs" to "= joins two squares that are the same. × joins two that are different.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { Sample(listOf("s"), 56.dp) },
                { Sample(listOf("smms"), 20.dp) },
                { Sample(listOf("ssm"), 26.dp) },
                { Sample(listOf("sm"), 40.dp) },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = SunMoonGame(ads)
}
