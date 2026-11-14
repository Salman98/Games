package com.techeliteclub.game.numbermatch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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

/** A few squares in a row for the how-to cards; [picked] glow orange. */
@Composable
private fun Strip(values: List<Int>, picked: Set<Int>) {
    Row { values.forEachIndexed { i, v -> NumberCell(v, 28.dp, i in picked, false, null) } }
}

class MainActivity : GameActivity() {
    override val gameTheme = Themes.NumberMatch
    override val logoLines = listOf("NUMBER", "MATCH")
    override val tagline = "Pair equal numbers or tens."
    override val hasLevels = true
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        Box(Modifier.size(300.dp, 230.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(250.dp).background(Brush.radialGradient(listOf(Color(0x7378A0FF), Color.Transparent)), CircleShape))
            Bezel(Modifier.size(190.dp).rotate(-6f), padding = 6.dp, radius = 24.dp) {
                Box(Modifier.size(162.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.layout.Column {
                        listOf(listOf(5, 1, 9, 7), listOf(2, 4, 6, 8), listOf(3, 8, 1, 5), listOf(7, 3, 5, 2)).forEachIndexed { r, row ->
                            Row { row.forEachIndexed { c, v -> NumberCell(v, 40.dp, (r == 0 && c == 1) || (r == 0 && c == 2), false, null) } }
                        }
                    }
                }
            }
            Box(
                Modifier.offset(96.dp, (-80).dp).rotate(12f).size(62.dp).shadow(10.dp, RoundedCornerShape(18.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFF8DB8), Color(0xFFE0407A))), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("10", style = displayStyle(30.sp), color = Color.White) }
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "NUMBER MATCH",
            rules = listOf(
                "Pair two numbers" to "Tap two that are equal, or two that add up to 10.",
                "Nothing in between" to "Pairs sit side by side, in a column, on a diagonal, or across the end of a row, with only crossed-out squares between them.",
                "Clear the board" to "Stuck? Add numbers copies what is left to the end. You get 4 per level.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { Strip(listOf(3, 7, 4), setOf(0, 1)) },
                { Strip(listOf(6, 0, 6), setOf(0, 2)) },
                { Strip(listOf(0, 0, 0), emptySet()) },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = NumberMatchGame(ads)
}
