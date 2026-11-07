package com.techeliteclub.game.wordsearch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import com.techeliteclub.game.core.GameActivity
import com.techeliteclub.game.core.GameStore
import com.techeliteclub.game.core.HomeInfo
import com.techeliteclub.game.core.Themes
import com.techeliteclub.game.core.displayStyle

class MainActivity : GameActivity() {
    override val gameTheme = Themes.Word
    override val logoLines = listOf("WORD", "SEARCH")
    override val tagline = "Find every hidden word."
    override val hasLevels = true

    override fun homeInfo(store: GameStore): HomeInfo {
        val level = store.get("level", 1)
        return HomeInfo("${store.get("stars")}", pillIsTrophy = false, label = "LEVEL $level", progress = ((level - 1) % 10) / 10f)
    }

    @Composable
    override fun Hero() {
        Box(Modifier.size(320.dp, 220.dp)) {
            // golden "found" capsule behind the letters
            Box(
                Modifier.offset(20.dp, 112.dp).rotate(3f).size(280.dp, 16.dp).shadow(12.dp, RoundedCornerShape(8.dp), ambientColor = Color(0xFFFFEC96), spotColor = Color(0xFFFFEC96))
                    .background(Color(0xCCFFEC96), RoundedCornerShape(8.dp))
            )
            listOf("W" to -12f, "O" to -4f, "R" to 6f, "D" to 14f).forEachIndexed { i, (ch, angle) ->
                Box(
                    Modifier.offset((22 + i * 70).dp, (60 + listOf(0, 18, 4, 22)[i]).dp).rotate(angle).size(66.dp)
                        .shadow(10.dp, RoundedCornerShape(16.dp))
                        .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFFFE9EF))), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text(ch, style = displayStyle(40.sp), color = Color(0xFF6B1F4E)) }
            }
        }
    }

    @Composable
    override fun Game(ads: Ads) = WordSearchGame(ads)
}
