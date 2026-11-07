package com.salman98.games.wordsearch

import androidx.compose.runtime.Composable
import com.salman98.games.core.Ads
import com.salman98.games.core.GameActivity

class MainActivity : GameActivity() {
    @Composable
    override fun Game(ads: Ads) = WordSearchGame(ads)
}
