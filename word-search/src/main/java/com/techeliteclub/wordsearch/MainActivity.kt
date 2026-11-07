package com.techeliteclub.wordsearch

import androidx.compose.runtime.Composable
import com.techeliteclub.core.Ads
import com.techeliteclub.core.GameActivity

class MainActivity : GameActivity() {
    @Composable
    override fun Game(ads: Ads) = WordSearchGame(ads)
}
