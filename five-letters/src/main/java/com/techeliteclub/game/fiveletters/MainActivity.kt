package com.techeliteclub.game.fiveletters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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

/** A scored word for the how-to cards. */
@Composable
private fun Scored(guess: String, answer: String) {
    val marks = score(guess, answer)
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) { for (i in 0 until LEN) LetterBox(guess[i], marks[i], 20.dp) }
}

class MainActivity : GameActivity() {
    override val gameTheme = Themes.FiveLetters
    override val logoLines = listOf("FIVE", "LETTERS")
    override val tagline = "Six tries. One word."
    override val hasLevels = true
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "WORD $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        Box(Modifier.size(300.dp, 230.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(250.dp).background(Brush.radialGradient(listOf(Color(0x594FE0C0), Color.Transparent)), CircleShape))
            Bezel(Modifier.size(190.dp).rotate(-6f), padding = 10.dp, radius = 24.dp) {
                Box(Modifier.size(158.dp), contentAlignment = Alignment.Center) { GuessGrid(listOf("SLATE", "CLAMP"), "PLA", "PLANT", emptySet(), 27.dp, rows = 5) }
            }
            Box(Modifier.offset(96.dp, (-80).dp).rotate(12f)) { LetterBox('W', CORRECT, 62.dp) }
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "FIVE LETTERS",
            rules = listOf(
                "Guess the word" to "Type five letters and press Enter. You have six tries.",
                "Green: right place" to "The letter is in the word and in the right spot.",
                "Gold: wrong place" to "The letter is in the word, but somewhere else. Grey letters are not in it.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { Scored("SLATE", "QQQQQ") },
                { Scored("PLANT", "PLANT") },
                { Scored("TAPLN", "PLANT") },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = FiveLettersGame(ads)
}
