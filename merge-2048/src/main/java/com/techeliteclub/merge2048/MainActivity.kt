package com.techeliteclub.merge2048

import androidx.compose.runtime.Composable
import com.techeliteclub.core.Ads
import com.techeliteclub.core.GameActivity

class MainActivity : GameActivity() {
    @Composable
    override fun Game(ads: Ads) = Merge2048Game(ads)
}
