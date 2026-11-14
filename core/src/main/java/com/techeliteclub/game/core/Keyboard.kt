package com.techeliteclub.game.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

const val KEY_ENTER = '\n'
const val KEY_DELETE = '\b'
private val KEY_ROWS = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM")

/**
 * On-screen QWERTY for the word games (no system keyboard, so no IME quirks or data collection).
 * [colors] tints single letters (found / wrong); [dim] letters are greyed and not clickable.
 * [onKey] gets the letter, [KEY_ENTER] or [KEY_DELETE]; the last two show only when [actions] is true.
 */
@Composable
fun LetterKeyboard(
    modifier: Modifier = Modifier,
    colors: Map<Char, Color> = emptyMap(),
    dim: Set<Char> = emptySet(),
    actions: Boolean = true,
    onKey: (Char) -> Unit,
) {
    val t = LocalGameTheme.current
    val base = if (t.light) Color.White else Color.White.copy(alpha = 0.16f)
    @Composable
    fun RowScope.Key(label: String, key: Char, weight: Float) {
        val off = key in dim
        Box(
            Modifier.weight(weight).height(46.dp)
                .background((colors[key] ?: base).copy(alpha = if (off) 0.25f else (colors[key] ?: base).alpha), RoundedCornerShape(8.dp))
                .pressable(enabled = !off, label = label) { onKey(key) },
            contentAlignment = Alignment.Center,
        ) {
            val ink = if (key in colors) Color.White else t.glassInk
            Text(label, style = displayStyle(if (label.length == 1) 18.sp else 12.sp), color = ink.copy(alpha = if (off) 0.4f else 1f))
        }
    }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        KEY_ROWS.forEachIndexed { r, row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (r == 1) Spacer(Modifier.weight(0.5f))
                if (r == 2) { if (actions) Key("ENTER", KEY_ENTER, 1.5f) else Spacer(Modifier.weight(1.5f)) }
                for (ch in row) Key("$ch", ch, 1f)
                if (r == 1) Spacer(Modifier.weight(0.5f))
                if (r == 2) { if (actions) Key("DEL", KEY_DELETE, 1.5f) else Spacer(Modifier.weight(1.5f)) }
            }
        }
    }
}
