package com.techeliteclub.game.carescape

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
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.GoldIcon
import com.techeliteclub.game.core.HowToScreen
import com.techeliteclub.game.core.GameActivity
import com.techeliteclub.game.core.GameStore
import com.techeliteclub.game.core.HomeInfo
import com.techeliteclub.game.core.Themes

class MainActivity : GameActivity() {
    override val gameTheme = Themes.Car
    override val logoLines = listOf("CAR", "ESCAPE")
    override val tagline = "Slide the cars. Free the red one."
    override val hasLevels = true
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        Box(Modifier.size(330.dp, 220.dp)) {
            // road markings and red motion trails
            Box(Modifier.offset(10.dp, 150.dp).size(310.dp, 6.dp).background(Brush.horizontalGradient(listOf(Color(0x00FFFFFF), Color(0x80FFFFFF), Color(0x00FFFFFF)))))
            Box(Modifier.offset(10.dp, 92.dp).size(120.dp, 4.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0xCCFF2D55))), RoundedCornerShape(2.dp)))
            Box(Modifier.offset(0.dp, 116.dp).size(90.dp, 4.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0x99FF2D55))), RoundedCornerShape(2.dp)))
            HeroCar(true, 170.dp, 76.dp, hero = true, modifier = Modifier.offset(110.dp, 66.dp).rotate(-4f))
            HeroCar(false, 60.dp, 120.dp, hero = false, modifier = Modifier.offset(270.dp, 10.dp).rotate(6f))
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "CAR ESCAPE",
            rules = listOf(
                "Get the red car to the exit" to "The green gate on the right edge.",
                "Drag a car to slide it" to "Cars only move along their length.",
                "Fewer moves, more stars" to "Match the PAR for 3 stars.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { HeroCar(true, 70.dp, 34.dp, hero = true) },
                { HeroCar(false, 30.dp, 62.dp, hero = false) },
                { GoldIcon(GameIcons.StarGold, 40.dp) },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = CarEscapeGame(ads)
}
