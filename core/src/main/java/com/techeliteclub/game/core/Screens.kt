package com.techeliteclub.game.core

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** A game's home screen: hero art, logo, progress, PLAY, sound / music / how-to. */
@Composable
fun HomeScreen(
    logoLines: List<String>,
    tagline: String,
    info: HomeInfo,
    hero: @Composable () -> Unit,
    onPlay: () -> Unit,
    onHowTo: (() -> Unit)?,
) {
    val t = LocalGameTheme.current
    val nav = LocalNav.current
    val settings = LocalSettings.current
    val feedback = rememberFeedback()
    GameBackground {
        Column(Modifier.fillMaxSize().statusBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                GlassPill {
                    if (info.pillIsTrophy) Icon(GameIcons.Trophy, null, tint = Color(0xFFFFD34D), modifier = Modifier.size(18.dp))
                    else GoldIcon(GameIcons.StarGold, 20.dp)
                    PillText(info.pill)
                }
                GlassIconButton(GameIcons.Gear, "Settings") { feedback.tap(); nav.settings() }
            }
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { hero() }
            logoLines.forEachIndexed { i, line ->
                GradientText(line, if (i == 0) t.logo1 else t.logo2, if (line.length > 8) 54.sp else 68.sp)
            }
            Text(tagline, color = t.ink, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
            Spacer(Modifier.height(20.dp))
            if (info.progress == null) {
                GlassPill { PillText(info.label) }
            } else {
                Column(Modifier.width(300.dp).glass(t, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        PillText(info.label)
                        Text("${(info.progress * 100).toInt()}% to next chapter", color = t.glassInk.copy(alpha = 0.75f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.padding(top = 8.dp).fillMaxWidth().height(10.dp).clip(RoundedCornerShape(6.dp)).background(Color.Black.copy(alpha = 0.25f))) {
                        Box(Modifier.fillMaxWidth(info.progress.coerceIn(0.03f, 1f)).height(10.dp).clip(RoundedCornerShape(6.dp)).background(Brush.horizontalGradient(t.primary)))
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            PrimaryButton("PLAY", Modifier.width(270.dp), height = 82.dp, fontSize = 40.sp, icon = GameIcons.Play) { feedback.good(); onPlay() }
            Row(Modifier.padding(top = 22.dp, bottom = 26.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                if (settings != null) {
                    GlassIconButton(if (settings.sound) GameIcons.Sound else GameIcons.SoundOff, if (settings.sound) "Sound on" else "Sound off", 54.dp) {
                        settings.switchSound(!settings.sound); feedback.tap()
                    }
                    GlassIconButton(if (settings.music) GameIcons.Music else GameIcons.MusicOff, if (settings.music) "Music on" else "Music off", 54.dp) {
                        settings.switchMusic(!settings.music); feedback.tap()
                    }
                }
                if (onHowTo != null) GlassIconButton(GameIcons.Help, "How to play", 54.dp) { feedback.tap(); onHowTo() }
            }
        }
    }
}

/**
 * Full-screen result. [win] adds light rays, falling confetti and [stars] gold stars (0–3) that pop in.
 * [text] lines become rows in the glass card ("Moves: 18" shows as label + value).
 */
@Composable
fun ResultDialog(
    title: String,
    text: String,
    button: String,
    onClick: () -> Unit,
    secondButton: String? = null,
    onSecond: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") emoji: String = "",
    win: Boolean = true,
    stars: Int = 3,
) {
    val t = LocalGameTheme.current
    val feedback = rememberFeedback()
    LaunchedEffect(Unit) { if (win) feedback.win() else feedback.bad() }
    Box(
        Modifier.fillMaxSize()
            .background(Color.Black.copy(alpha = if (win) 0.6f else 0.7f))
            .pointerInput(Unit) { detectTapGestures { } }, // block taps on the game below
        contentAlignment = Alignment.Center,
    ) {
        if (win) { LightRays(); Confetti() }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            if (win && stars > 0) {
                Row(verticalAlignment = Alignment.Bottom) {
                    for (i in 0 until 3) {
                        val pop = remember { Animatable(0f) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(250L + i * 220L)
                            pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 300f))
                        }
                        val big = i == 1
                        GoldIcon(
                            if (i < stars) GameIcons.StarGold else GameIcons.StarEmpty, if (big) 120.dp else 86.dp,
                            Modifier.scale(pop.value).rotate(if (i == 0) -14f else if (i == 2) 14f else 0f).offset(y = if (big) 0.dp else 8.dp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            Box(
                Modifier
                    .background(
                        Brush.verticalGradient(if (win) listOf(Color(0xFFFF6B7F), Color(0xFFD6143A)) else listOf(Color(0xFF6B7393), Color(0xFF3A3F5C))),
                        RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 30.dp, vertical = 10.dp),
            ) { GradientText(title.uppercase(), listOf(Color.White, Color(0xFFFFE1E6)), 30.sp, stroke = Color(0xFF5A0A1E)) }
            if (text.isNotBlank()) {
                Column(
                    Modifier.padding(top = 24.dp).width(300.dp).glass(t.copy(light = false), RoundedCornerShape(24.dp)).padding(horizontal = 22.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    for (line in text.split('\n')) {
                        val parts = line.split(":", limit = 2)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            if (parts.size == 2) {
                                Text(parts[0].trim(), color = Color.White.copy(alpha = 0.8f), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                Text(parts[1].trim(), color = Color.White, style = displayStyle(24.sp))
                            } else {
                                Text(line, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
            PrimaryButton(button, Modifier.padding(top = 28.dp).width(280.dp), height = 74.dp, fontSize = 30.sp, onClick = onClick)
            if (secondButton != null) {
                Box(Modifier.padding(top = 16.dp)) {
                    GlassPill(Modifier.pressable(onClick = onSecond)) {
                        if (secondButton.startsWith("▶")) Icon(GameIcons.Play, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text(secondButton.removePrefix("▶ "), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LightRays() {
    val spin = rememberInfiniteTransition(label = "rays")
    val angle by spin.animateFloat(0f, 360f, infiniteRepeatable(tween(24000, easing = LinearEasing), RepeatMode.Restart), label = "angle")
    Canvas(Modifier.fillMaxSize()) {
        val c = Offset(size.width / 2, size.height * 0.32f)
        val r = size.maxDimension
        rotate(angle, c) {
            for (i in 0 until 18) {
                val a0 = Math.toRadians(i * 20.0)
                val a1 = Math.toRadians(i * 20.0 + 9)
                val p = Path().apply {
                    moveTo(c.x, c.y)
                    lineTo(c.x + r * cos(a0).toFloat(), c.y + r * sin(a0).toFloat())
                    lineTo(c.x + r * cos(a1).toFloat(), c.y + r * sin(a1).toFloat())
                    close()
                }
                drawPath(p, Brush.radialGradient(listOf(Color(0x33FFE278), Color.Transparent), c, r * 0.6f))
            }
        }
    }
}

private class Piece(val x: Float, val speed: Float, val drift: Float, val spin: Float, val w: Float, val h: Float, val color: Color, val delay: Float)

@Composable
private fun Confetti() {
    val colors = listOf(Color(0xFFFF5A7A), Color(0xFFFFD84A), Color(0xFF4FE08A), Color(0xFF5AA8FF), Color(0xFFC47BFF), Color(0xFFFF9AD5), Color(0xFFFFA43A))
    val pieces = remember {
        val r = Random(11)
        List(70) { Piece(r.nextFloat(), 0.18f + r.nextFloat() * 0.25f, r.nextFloat() * 2f - 1f, r.nextFloat() * 360f, 6f + r.nextFloat() * 6f, 10f + r.nextFloat() * 8f, colors[r.nextInt(colors.size)], r.nextFloat() * 1.5f) }
    }
    var nanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) withFrameNanos { nanos = it - start }
    }
    Canvas(Modifier.fillMaxSize()) {
        val secs = nanos / 1e9f
        for (p in pieces) {
            val tt = secs - p.delay
            if (tt < 0) continue
            val y = (tt * p.speed * size.height) % (size.height + 40f) - 20f
            val x = p.x * size.width + sin(tt * 2f + p.drift * 3f) * 18f * p.drift
            rotate(p.spin + tt * 220f * p.drift, Offset(x, y)) {
                drawRect(p.color, Offset(x - p.w / 2, y - p.h / 2), Size(p.w.dp.toPx(), p.h.dp.toPx()))
            }
        }
    }
}

/** Sound, music, vibration and (in the EU/UK) privacy choices. */
@Composable
fun SettingsSheet(ads: Ads, settings: GameSettings, onClose: () -> Unit) {
    val t = LocalGameTheme.current
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)).pointerInput(Unit) { detectTapGestures { onClose() } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .width(320.dp)
                .background(Brush.verticalGradient(listOf(t.bg[0], t.bg[1])), RoundedCornerShape(28.dp))
                .glass(t, RoundedCornerShape(28.dp))
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                GradientText("SETTINGS", t.logo1, 28.sp)
                GlassIconButton(GameIcons.Close, "Close", 40.dp, onClose)
            }
            SettingRow(GameIcons.Sound, "Sound effects", settings.sound) { settings.switchSound(it) }
            SettingRow(GameIcons.Music, "Music", settings.music) { settings.switchMusic(it) }
            SettingRow(GameIcons.Vibrate, "Vibration", settings.vibration) { settings.switchVibration(it) }
            if (ads.privacyOptionsRequired) {
                GlassPill(Modifier.pressable { ads.showPrivacyOptions() }) { PillText("Privacy options") }
            }
        }
    }
}

@Composable
private fun SettingRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, on: Boolean, onChange: (Boolean) -> Unit) {
    val t = LocalGameTheme.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = t.glassInk, modifier = Modifier.size(24.dp))
        Text(label, color = t.glassInk, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        Switch(
            checked = on, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF22B455), checkedThumbColor = Color.White),
        )
    }
}

/** How-to-play layout: logo, numbered rule cards (each with its own picture), and a big button. */
@Composable
fun HowToScreen(title: String, rules: List<Pair<String, String>>, pictures: List<@Composable () -> Unit>, footer: @Composable () -> Unit, onDone: () -> Unit) {
    val t = LocalGameTheme.current
    GameBackground {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            GradientText(title, t.logo1, 38.sp)
            Text(
                "HOW TO PLAY", color = Color(0xFFFFD34D), fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp).background(Color(0x2EFFD34D), RoundedCornerShape(50)).padding(horizontal = 16.dp, vertical = 6.dp),
            )
            Spacer(Modifier.height(16.dp))
            rules.forEachIndexed { i, (head, sub) ->
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 12.dp).glass(t, RoundedCornerShape(22.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier.size(34.dp).background(Brush.verticalGradient(listOf(Color(0xFFFFF1B0), Color(0xFFFFC533), Color(0xFFE08A00))), RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center,
                    ) { Text("${i + 1}", style = displayStyle(20.sp), color = Color(0xFF3A2200)) }
                    Column(Modifier.weight(1f)) {
                        Text(head, style = displayStyle(18.sp), color = t.glassInk)
                        Text(sub, color = t.glassInk.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    pictures.getOrNull(i)?.invoke()
                }
            }
            footer()
            Spacer(Modifier.weight(1f))
            PrimaryButton("GOT IT · LET'S PLAY", Modifier.fillMaxWidth(), height = 68.dp, fontSize = 24.sp, onClick = onDone)
        }
    }
}
