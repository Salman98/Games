package com.techeliteclub.crownlogic

import androidx.compose.runtime.Composable
import com.techeliteclub.core.Ads
import com.techeliteclub.core.GameActivity

class MainActivity : GameActivity() {
    @Composable
    override fun Game(ads: Ads) = CrownLogicGame(ads)
}
