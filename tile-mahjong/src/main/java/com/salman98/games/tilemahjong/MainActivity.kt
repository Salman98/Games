package com.salman98.games.tilemahjong

import androidx.compose.runtime.Composable
import com.salman98.games.core.Ads
import com.salman98.games.core.GameActivity

class MainActivity : GameActivity() {
    @Composable
    override fun Game(ads: Ads) = TileMahjongGame(ads)
}
