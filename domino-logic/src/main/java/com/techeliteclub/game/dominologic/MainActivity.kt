package com.techeliteclub.game.dominologic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.Bezel
import com.techeliteclub.game.core.GameActivity
import com.techeliteclub.game.core.GameStore
import com.techeliteclub.game.core.HomeInfo
import com.techeliteclub.game.core.HowToScreen
import com.techeliteclub.game.core.Themes

/** 2 rows of 3: tiles 3-3, 1-5, 2-4 with a sum, an equal and a less-than region. */
private val ANSWER = listOf(Placed(0, 0, 1), Placed(1, 2, 5), Placed(2, 3, 4))
private val SAMPLE = Puzzle(
    2, 3, listOf(3 to 3, 1 to 5, 2 to 4),
    listOf(Region(listOf(0, 1), Rule.EQUAL), Region(listOf(2), Rule.LESS, 3), Region(listOf(3, 4), Rule.SUM, 6), Region(listOf(5), Rule.NONE)),
    ANSWER,
)

class MainActivity : GameActivity() {
    override val gameTheme = Themes.Domino
    override val logoLines = listOf("DOMINO", "LOGIC")
    override val tagline = "Every region has a rule."
    override val hasLevels = true
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        Box(Modifier.size(300.dp, 230.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(250.dp).background(Brush.radialGradient(listOf(Color(0x66FFC88C), Color.Transparent)), CircleShape))
            Bezel(Modifier.rotate(-6f), padding = 6.dp, radius = 24.dp) { DominoBoard(SAMPLE, 186.dp, ANSWER.take(2)) }
            Domino(5, 5, 40.dp, modifier = Modifier.offset(84.dp, (-84).dp).rotate(12f))
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "DOMINO LOGIC",
            rules = listOf(
                "Place every domino" to "Tap a domino, then two squares side by side. Rotate swaps its halves. Tap a placed domino to lift it.",
                "Obey each colour" to "A number is the total of the pips in that colour. = means all the same, ≠ all different.",
                "< and >" to "The pips in that colour add up to less than, or more than, the number.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { DominoBoard(SAMPLE, 96.dp, ANSWER.take(1)) },
                { DominoBoard(SAMPLE, 96.dp, ANSWER.take(2)) },
                { DominoBoard(SAMPLE, 96.dp, ANSWER) },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = DominoLogicGame(ads)
}
