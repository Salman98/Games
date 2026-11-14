package com.techeliteclub.game.wordgroups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

/** Two tiles side by side for the how-to cards. */
@Composable
private fun Pair2(a: String, b: String, pickA: Boolean, pickB: Boolean) {
    Row(Modifier.width(100.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        WordTile(a, pickA, Modifier.weight(1f), 34.dp)
        WordTile(b, pickB, Modifier.weight(1f), 34.dp)
    }
}

class MainActivity : GameActivity() {
    override val gameTheme = Themes.WordGroups
    override val logoLines = listOf("WORD", "GROUPS")
    override val tagline = "Sixteen words. Four secret groups."
    override val hasLevels = true
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        Box(Modifier.size(300.dp, 230.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(250.dp).background(Brush.radialGradient(listOf(Color(0x73AA82FF), Color.Transparent)), CircleShape))
            Bezel(Modifier.size(190.dp).rotate(-6f), padding = 8.dp, radius = 24.dp) {
                Column(Modifier.size(162.dp), verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)) {
                    for ((row, pick) in listOf(listOf("LIME", "FOOT") to 1, listOf("FIG", "DATE") to -1, listOf("PLUM", "HOUR") to -1)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEachIndexed { i, w -> WordTile(w, i == pick, Modifier.weight(1f), 46.dp) }
                        }
                    }
                }
            }
            Box(
                Modifier.offset(96.dp, (-80).dp).rotate(12f).size(62.dp).shadow(10.dp, RoundedCornerShape(18.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFC55C), Color(0xFFF08A00))), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("4", style = displayStyle(38.sp), color = Color.White) }
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "WORD GROUPS",
            rules = listOf(
                "Find four that fit" to "Tap four words that share something, then press Submit.",
                "Four groups in all" to "A right group moves to the top and shows what links it.",
                "Four mistakes" to "A wrong guess costs one. \"One away\" means three of your four were right.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { Pair2("LIME", "HOUR", true, false) },
                { Box(Modifier.width(100.dp)) { GroupBar(Category("FRUITS", listOf("LIME", "FIG")), 0, 40.dp) } },
                { Pair2("FIG", "FOOT", true, true) },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = WordGroupsGame(ads)
}
