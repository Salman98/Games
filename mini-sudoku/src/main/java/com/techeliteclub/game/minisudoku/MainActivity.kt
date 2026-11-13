package com.techeliteclub.game.minisudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameIcons
import com.techeliteclub.game.core.HowToIcon
import com.techeliteclub.game.core.HowToScreen
import com.techeliteclub.game.core.Bezel
import com.techeliteclub.game.core.GameActivity
import com.techeliteclub.game.core.GameStore
import com.techeliteclub.game.core.HomeInfo
import com.techeliteclub.game.core.Themes
import com.techeliteclub.game.core.displayStyle

class MainActivity : GameActivity() {
    override val gameTheme = Themes.MiniSudoku
    override val logoLines = listOf("MINI", "SUDOKU")
    override val tagline = "Six by six. Solved in minutes."
    override val hasHowTo = true
    override val hasLevels = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        val digits = listOf("5", "3", "", "6", "", "1", "", "4", "2")
        Box(Modifier.size(300.dp, 230.dp), contentAlignment = Alignment.Center) {
            Bezel(Modifier.size(200.dp).rotate(-5f), padding = 5.dp, radius = 24.dp) {
                Column(Modifier.clip(RoundedCornerShape(12.dp))) {
                    for (r in 0 until 3) Row {
                        for (c in 0 until 3) {
                            val i = r * 3 + c
                            Box(
                                Modifier.size(59.dp).background(if (i == 4) Color(0xFFDDF5EF) else Color.White).border(0.5.dp, Color(0xFFD3E9E3)),
                                contentAlignment = Alignment.Center,
                            ) { Text(digits[i], style = displayStyle(32.sp), color = if (c == 1) Color(0xFF0FA58F) else Color(0xFF0F3B3A)) }
                        }
                    }
                }
            }
            Box(
                Modifier.offset(100.dp, (-80).dp).rotate(12f).size(64.dp).shadow(10.dp, RoundedCornerShape(20.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFB86B), Color(0xFFF2761B))), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("6", style = displayStyle(40.sp), color = Color.White) }
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "MINI SUDOKU",
            rules = listOf(
                "Fill every square with 1 to 6" to "Tap an empty square, then tap a number in the row below the board.",
                "No repeats" to "Each number once in every row, column and outlined box of six.",
                "Notes button: small pencil marks" to "While it is gold, a number is only written small as a reminder. Tap Notes again to fill squares normally.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { HowToIcon(GameIcons.Grid) },
                { HowToIcon(GameIcons.Close) },
                { HowToIcon(GameIcons.Pencil) },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = MiniSudokuGame(ads)
}
