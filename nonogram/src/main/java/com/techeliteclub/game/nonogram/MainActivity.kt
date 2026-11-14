package com.techeliteclub.game.nonogram

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
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
import com.techeliteclub.game.core.gem

/** A 5×5 heart for the home and how-to screens. */
private val HEART = Puzzle(5, ".X.X.XXXXXXXXXX.XXX...X..".map { it == 'X' }.toBooleanArray())
private fun filled(rows: Int) = IntArray(25) { if (it < rows * 5 && HEART.solution[it]) FILL else if (it < rows * 5) CROSS else EMPTY }

class MainActivity : GameActivity() {
    override val gameTheme = Themes.Nonogram
    override val logoLines = listOf("NONO", "GRAM")
    override val tagline = "Follow the numbers. Reveal the picture."
    override val hasLevels = true
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        Box(Modifier.size(300.dp, 230.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(250.dp).background(Brush.radialGradient(listOf(Color(0x5978FFE6), Color.Transparent)), CircleShape))
            Bezel(Modifier.size(190.dp).rotate(-6f), padding = 6.dp, radius = 24.dp) { NonoBoard(HEART, 160.dp, filled(3), clueFrac = 0.26f) }
            Box(
                Modifier.offset(96.dp, (-80).dp).rotate(12f).size(62.dp).shadow(10.dp, RoundedCornerShape(18.dp))
                    .gem(Color(0xFFFF8AA8), Color(0xFFE2365F), 18.dp),
            )
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "NONOGRAM",
            rules = listOf(
                "Read the numbers" to "Each number is a run of filled squares in that row or column, in order, with gaps between runs.",
                "Fill and mark" to "Tap or drag to fill. Switch to Mark empty to put an ✕ where nothing goes.",
                "Reveal the picture" to "When every row and column matches its numbers, the picture is done.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { NonoBoard(HEART, 96.dp, IntArray(25), clueFrac = 0.34f) },
                { NonoBoard(HEART, 96.dp, filled(2), clueFrac = 0.34f) },
                { NonoBoard(HEART, 96.dp, filled(5), clueFrac = 0.34f) },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = NonogramGame(ads)
}
