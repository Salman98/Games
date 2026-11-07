package com.techeliteclub.game.crownlogic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.Bezel
import com.techeliteclub.game.core.GameActivity
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.GameStore
import com.techeliteclub.game.core.GoldIcon
import com.techeliteclub.game.core.HomeInfo
import com.techeliteclub.game.core.HowToScreen
import com.techeliteclub.game.core.Themes
import com.techeliteclub.game.core.displayStyle

private val SAMPLE_REGIONS = listOf("0011", "0211", "2233", "2333")
private val SAMPLE_CROWNS = setOf(0 to 1, 1 to 3, 2 to 0, 3 to 2)

class MainActivity : GameActivity() {
    override val gameTheme = Themes.Crown
    override val logoLines = listOf("CROWN", "LOGIC")
    override val tagline = "One crown per row, column & colour."
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
                RegionBoard(4, 170.dp, { r, c -> SAMPLE_REGIONS[r][c].digitToInt() }, { r, c -> if ((r to c) in SAMPLE_CROWNS) CROWN else EMPTY })
            }
            GoldIcon(GameIcons.CrownGold, 110.dp, Modifier.offset(90.dp, (-80).dp).rotate(14f))
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        val solved = { r: Int, c: Int -> if ((r to c) in SAMPLE_CROWNS) CROWN else EMPTY }
        HowToScreen(
            title = "CROWN LOGIC",
            rules = listOf(
                "One crown in every row & column" to "Each line across and down gets one.",
                "One crown in every colour" to "Each coloured area gets one.",
                "Crowns never touch" to "Not side by side, not even at corners.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { RegionBoard(4, 80.dp, { _, _ -> 1 }, solved) },
                { RegionBoard(4, 80.dp, { r, c -> SAMPLE_REGIONS[r][c].digitToInt() }, solved) },
                { RegionBoard(3, 80.dp, { _, _ -> 1 }, { r, c -> if (r == 1 && c == 1) CROWN else MARK }) },
            ),
            footer = { TapSteps() },
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = CrownLogicGame(ads)
}

/** "Tap a square: empty → ✕ → crown". */
@Composable
private fun TapSteps() {
    Column(
        Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(22.dp)).padding(16.dp),
    ) {
        Text("Tap a square", style = displayStyle(18.sp), color = Color(0xFFFFD34D))
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Step("empty", EMPTY)
            Icon(GameIcons.Forward, null, tint = Color(0xFFFFD34D), modifier = Modifier.size(20.dp))
            Step("✕ = not here", MARK)
            Icon(GameIcons.Forward, null, tint = Color(0xFFFFD34D), modifier = Modifier.size(20.dp))
            Step("crown", CROWN)
        }
    }
}

@Composable
private fun Step(label: String, state: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        RegionBoard(1, 44.dp, { _, _ -> 1 }, { _, _ -> state })
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 4.dp))
    }
}
