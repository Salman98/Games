package com.techeliteclub.game.colorsort

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.HowToScreen
import com.techeliteclub.game.core.GameActivity
import com.techeliteclub.game.core.GameStore
import com.techeliteclub.game.core.HomeInfo
import com.techeliteclub.game.core.Themes

class MainActivity : GameActivity() {
    override val gameTheme = Themes.Sort
    override val logoLines = listOf("COLOR", "SORT")
    override val tagline = "Pour, match, relax."
    override val hasLevels = true
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.Bottom) {
            Row(Modifier.rotate(-8f)) { Tube(listOf(0, 0, 1, 0), selected = false, ball = 34.dp) }
            Tube(listOf(4, 4, 4, 4), selected = false, ball = 38.dp)
            Row(Modifier.rotate(8f)) { Tube(listOf(3, 2, 3, 3), selected = false, ball = 34.dp) }
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "COLOR SORT",
            rules = listOf(
                "Tap a tube, then another" to "The top ball moves across.",
                "Same colour or empty only" to "A ball lands on its own colour or in an empty tube.",
                "One colour in every tube" to "Sort them all to win. Fewer moves, more stars.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { Tube(listOf(0, 1, 1), selected = false, ball = 14.dp) },
                { Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { Tube(listOf(0, 1), selected = false, ball = 14.dp); Tube(emptyList(), selected = false, ball = 14.dp) } },
                { Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { repeat(CAP) { Ball(1, 16.dp) } } },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = ColorSortGame(ads)
}
