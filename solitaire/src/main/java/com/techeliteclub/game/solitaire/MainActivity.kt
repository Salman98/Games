package com.techeliteclub.game.solitaire

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import com.techeliteclub.game.core.HowToScreen
import com.techeliteclub.game.core.GameActivity
import com.techeliteclub.game.core.GameStore
import com.techeliteclub.game.core.HomeInfo
import com.techeliteclub.game.core.Themes
import kotlin.math.abs

class MainActivity : GameActivity() {
    override val gameTheme = Themes.Solitaire
    override val logoLines = listOf("SOLITAIRE")
    override val tagline = "The classic, beautifully dealt."
    override val hasHowTo = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val wins = store.get("wins")
        return HomeInfo("$wins", pillIsTrophy = true, label = "WINS $wins", progress = null)
    }

    @Composable
    override fun Hero() {
        // Fan of the four aces.
        Box(Modifier.size(320.dp, 220.dp)) {
            for (suit in 0 until 4) {
                val spread = suit - 1.5f
                PlayingCard(
                    Card(1, suit, up = true), 92.dp, 128.dp,
                    Modifier.offset((70 + suit * 46).dp, (40 + abs(spread) * 16).dp).rotate(spread * 12f),
                )
            }
        }
    }

    @Composable
    override fun HowTo(onDone: () -> Unit) {
        HowToScreen(
            title = "SOLITAIRE",
            rules = listOf(
                "Build each suit from Ace to King" to "Move all 52 cards to the four piles at the top.",
                "Stack down, red on black" to "In the columns a card goes on one that is one higher and the other colour.",
                "Tap a card to move it" to "Tap the deck for a new card. Only a King fills an empty column.",
            ),
            pictures = listOf<@Composable () -> Unit>(
                { PlayingCard(Card(1, 0, up = true), 34.dp, 48.dp) },
                { Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { PlayingCard(Card(7, 0, up = true), 34.dp, 48.dp); PlayingCard(Card(6, 1, up = true), 34.dp, 48.dp) } },
                { PlayingCard(Card(13, 1, up = true), 34.dp, 48.dp) },
            ),
            footer = {},
            onDone = onDone,
        )
    }

    @Composable
    override fun Game(ads: Ads) = SolitaireGame(ads)
}
