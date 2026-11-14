package com.techeliteclub.game.cryptogram

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

private val SAMPLE = Puzzle("THE BEST", mapOf('T' to 5, 'H' to 3, 'E' to 6, 'B' to 11, 'S' to 10), setOf('T', 'E'))
private val WORD = Puzzle("WAY", mapOf('W' to 7, 'A' to 8, 'Y' to 9), setOf('A'))

class MainActivity : GameActivity() {
    override val gameTheme = Themes.Cryptogram
    override val logoLines = listOf("CRYPTO", "GRAM")
    override val tagline = "Crack the code. Read the quote."
    override val hasLevels = true
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        Box(Modifier.size(300.dp, 230.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(250.dp).background(Brush.radialGradient(listOf(Color(0x66FFE296), Color.Transparent)), CircleShape))
            Bezel(Modifier.size(190.dp).rotate(-6f), padding = 8.dp, radius = 24.dp) {
                Box(Modifier.size(162.dp), contentAlignment = Alignment.Center) { QuoteBoard(SAMPLE, 162.dp, 4, SAMPLE.given, 'S') }
            }
            Box(
                Modifier.offset(96.dp, (-80).dp).rotate(12f).size(62.dp).shadow(10.dp, RoundedCornerShape(18.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFF6DB), Color(0xFFE7C98A))), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("A", style = displayStyle(38.sp), color = Color(0xFF133D2C)) }
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "CRYPTOGRAM",
            rules = listOf(
                "Every number is a letter" to "The same number is always the same letter, all through the quote.",
                "Tap a tile, pick a letter" to "A right letter fills every tile with that number.",
                "Three mistakes" to "A wrong letter costs a mistake. Three and the quote starts again.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { QuoteBoard(WORD, 96.dp, 3, WORD.given, null) },
                { QuoteBoard(WORD, 96.dp, 3, WORD.given, 'W') },
                { QuoteBoard(WORD, 96.dp, 3, setOf('W', 'A', 'Y'), null) },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = CryptogramGame(ads)
}
