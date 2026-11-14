package com.techeliteclub.game.rectangles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

private val ANSWER = listOf(Rect(0, 0, 2, 2), Rect(0, 2, 1, 2), Rect(1, 2, 3, 1), Rect(1, 3, 3, 1), Rect(2, 0, 2, 2))
private val SAMPLE = Puzzle(4, mapOf(0 to 4, 3 to 2, 10 to 3, 7 to 3, 13 to 4), ANSWER)

class MainActivity : GameActivity() {
    override val gameTheme = Themes.Rectangles
    override val logoLines = listOf("RECT", "ANGLES")
    override val tagline = "Cut the grid. Match every number."
    override val hasLevels = true
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        Box(Modifier.size(300.dp, 230.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(250.dp).background(Brush.radialGradient(listOf(Color(0x73FFB45A), Color.Transparent)), CircleShape))
            Bezel(Modifier.size(190.dp).rotate(-6f), padding = 5.dp, radius = 24.dp) { RectBoard(SAMPLE, 166.dp, ANSWER.take(3), ANSWER[3]) }
            Box(
                Modifier.offset(96.dp, (-80).dp).rotate(12f).size(62.dp).shadow(10.dp, RoundedCornerShape(18.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFB35C), Color(0xFFE8650C))), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("4", style = displayStyle(38.sp), color = Color.White) }
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "RECTANGLES",
            rules = listOf(
                "Drag to draw" to "Drag from one corner to the other. Tap a rectangle to remove it.",
                "One number each" to "Every rectangle holds exactly one number, and covers that many squares.",
                "Cover the board" to "Rectangles never overlap and every square is used.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { RectBoard(SAMPLE, 84.dp, emptyList(), ANSWER[0]) },
                { RectBoard(SAMPLE, 84.dp, ANSWER.take(3)) },
                { RectBoard(SAMPLE, 84.dp, ANSWER) },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = RectanglesGame(ads)
}
