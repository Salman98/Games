package com.techeliteclub.game.core

import android.content.Context
import android.os.Bundle
import android.view.SoundEffectConstants
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Each game's MainActivity extends this and only implements [Game]. */
abstract class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val ads = Ads(this).also { it.start() }
        setContent {
            // Each game's own colour (res/values/game.xml) becomes the accent colour.
            val accent = colorResource(R.color.game_color)
            val scheme = if (isSystemInDarkTheme()) darkColorScheme(primary = accent, secondary = accent)
            else lightColorScheme(primary = accent, secondary = accent)
            MaterialTheme(colorScheme = scheme) { Game(ads) }
        }
    }

    @Composable
    abstract fun Game(ads: Ads)
}

/** Top bar (title, status, hint, restart), game content, banner at the bottom. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScaffold(
    title: String,
    status: String,
    ads: Ads,
    onRestart: () -> Unit,
    hintLabel: String = "Hint",
    onHint: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(status, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    }
                },
                actions = {
                    // Rewarded ad: the player chooses to watch a video for the hint.
                    if (onHint != null) {
                        TextButton(onClick = { ads.showRewarded(onHint) }, enabled = ads.rewardedReady) {
                            Text("▶ $hintLabel", maxLines = 1)
                        }
                    }
                    TextButton(onClick = onRestart, modifier = Modifier.semantics { contentDescription = "Restart" }) {
                        Text("↻", fontSize = 22.sp)
                    }
                    if (ads.privacyOptionsRequired) {
                        TextButton(onClick = { ads.showPrivacyOptions() }) { Text("Privacy") }
                    }
                },
            )
        },
        bottomBar = { ads.Banner(Modifier.navigationBarsPadding()) },
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}

/** End-of-level dialog. [emoji] is shown big above the title. */
@Composable
fun ResultDialog(
    title: String,
    text: String,
    button: String,
    onClick: () -> Unit,
    secondButton: String? = null,
    onSecond: () -> Unit = {},
    emoji: String = "🎉",
) {
    AlertDialog(
        onDismissRequest = {},
        icon = { Text(emoji, fontSize = 40.sp) },
        confirmButton = { Button(onClick = onClick) { Text(button) } },
        dismissButton = if (secondButton != null) {
            { TextButton(onClick = onSecond) { Text(secondButton) } }
        } else null,
        title = { Text(title, textAlign = TextAlign.Center) },
        text = { Text(text, textAlign = TextAlign.Center) },
    )
}

/** Small click sound (follows the phone's touch-sound setting) and vibration. */
class Feedback(private val view: View, private val haptic: HapticFeedback) {
    fun tap() = view.playSoundEffect(SoundEffectConstants.CLICK)
    fun good() {
        view.playSoundEffect(SoundEffectConstants.CLICK)
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    fun bad() = haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    fun win() = haptic.performHapticFeedback(HapticFeedbackType.LongPress)
}

@Composable
fun rememberFeedback(): Feedback {
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    return remember(view, haptic) { Feedback(view, haptic) }
}

/** Distinct colours shared by the games (tiles, balls, regions). */
val PALETTE = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFDD835), Color(0xFF8E24AA),
    Color(0xFFFB8C00), Color(0xFF00ACC1), Color(0xFFD81B60), Color(0xFF6D4C41), Color(0xFF7CB342),
)

/** Saved numbers (level, best score) that survive app restarts. */
class GameStore(context: Context) {
    private val prefs = context.getSharedPreferences("game", Context.MODE_PRIVATE)
    fun get(key: String, default: Int = 0) = prefs.getInt(key, default)
    fun put(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
}

@Composable
fun rememberStore(): GameStore {
    val context = LocalContext.current
    return remember { GameStore(context) }
}
