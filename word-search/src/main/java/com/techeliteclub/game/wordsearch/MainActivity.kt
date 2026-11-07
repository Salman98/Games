package com.techeliteclub.game.wordsearch

import androidx.compose.runtime.Composable
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameActivity

class MainActivity : GameActivity() {
    @Composable
    override fun Game(ads: Ads) = WordSearchGame(ads)
}
