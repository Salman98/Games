package com.techeliteclub.game.colorsort

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.techeliteclub.game.core.Ads
import com.techeliteclub.game.core.GameScaffold
import com.techeliteclub.game.core.PALETTE
import com.techeliteclub.game.core.ResultDialog
import com.techeliteclub.game.core.rememberStore
import kotlin.random.Random

@Composable
fun ColorSortGame(ads: Ads) {
    val store = rememberStore()
    var level by remember { mutableIntStateOf(store.get("level", 1)) }
    var tubes by remember(level) { mutableStateOf(newLevel(level, Random(level.toLong()))) }
    var history by remember(level) { mutableStateOf(listOf<Tubes>()) }
    var selected by remember(level) { mutableStateOf<Int?>(null) }
    val won = isSolved(tubes)

    fun restart() {
        tubes = newLevel(level, Random(level.toLong())); history = emptyList(); selected = null
    }

    fun tap(i: Int) {
        val from = selected
        if (from == null) {
            if (tubes[i].isNotEmpty()) selected = i
            return
        }
        if (canMove(tubes, from, i)) {
            history = history + listOf(tubes)
            tubes = move(tubes, from, i)
        }
        selected = null
    }

    val canAddTube = tubes.size < colorsFor(level) + 4
    GameScaffold(
        "Color Sort", "Level $level", ads, ::restart, "Extra tube",
        onHint = if (canAddTube) ({ tubes = tubes + listOf(emptyList()) }) else null,
    ) {
        Text("Tap a tube, then tap where to pour", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))
        // Up to 6 tubes per row.
        for (rowStart in tubes.indices step 6) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (i in rowStart until minOf(rowStart + 6, tubes.size)) {
                    Tube(tubes[i], selected == i) { tap(i) }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        OutlinedButton(onClick = {
            history.lastOrNull()?.let { tubes = it; history = history.dropLast(1) }
            selected = null
        }, enabled = history.isNotEmpty()) { Text("Undo") }
        if (!won && !hasMoves(tubes)) Text("No moves left. Undo, restart or add a tube.")
    }

    if (won) {
        ResultDialog("Level $level done!", "Nice sorting.", "Next level", {
            ads.levelDone()
            level++
            store.put("level", level)
        })
    }
}

@Composable
private fun Tube(balls: List<Int>, selected: Boolean, onClick: () -> Unit) {
    val ball = 34.dp
    Column(
        Modifier
            .offset(y = if (selected) (-12).dp else 0.dp)
            .width(ball + 12.dp)
            .height(ball * CAP + 20.dp)
            .border(3.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .clickable(onClick = onClick)
            .padding(bottom = 6.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (c in balls.asReversed()) {
            Box(Modifier.padding(1.dp).size(ball - 2.dp).background(PALETTE[c], CircleShape))
        }
    }
}
