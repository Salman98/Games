package com.techeliteclub.game.core

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Back button + title, used by the feature screens. */
@Composable
private fun ScreenTop(title: String) {
    val t = LocalGameTheme.current
    val nav = LocalNav.current
    val feedback = rememberFeedback()
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        GlassIconButton(GameIcons.Back, "Home") { feedback.tap(); nav.home() }
        GradientText(title, t.logo1, 28.sp)
        Spacer(Modifier.size(46.dp))
    }
}

@Composable
private fun FeatureCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val t = LocalGameTheme.current
    Box(modifier.fillMaxWidth().glass(t, RoundedCornerShape(22.dp)).padding(16.dp)) { content() }
}

@Composable
private fun FeatureLabel(text: String, size: Int = 15, alpha: Float = 1f) =
    Text(text, color = LocalGameTheme.current.glassInk.copy(alpha = alpha), fontSize = size.sp, fontWeight = FontWeight.ExtraBold)

private val WEEK = listOf("M", "T", "W", "T", "F", "S", "S")

/** Daily hub: streak flame, this week, today's puzzle and streak rewards. */
@Composable
fun DailyScreen(onPlay: () -> Unit) {
    val t = LocalGameTheme.current
    val store = rememberStore()
    val feedback = rememberFeedback()
    val today = Reminder.today()
    val streak = Streak.current(store)
    val last = Streak.lastDay(store)
    val done = Daily.doneToday(store)
    GameBackground {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ScreenTop("DAILY")
            GoldIcon(GameIcons.Flame, 84.dp)
            GradientText("$streak DAY STREAK", t.logo2, 34.sp)
            Text("Best: ${Streak.best(store)} days", color = t.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            FeatureCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val monday = StreakMath.weekStart(today)
                    WEEK.forEachIndexed { i, d ->
                        val day = monday + i
                        val lit = streak > 0 && StreakMath.inStreak(day, last, streak)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FeatureLabel(d, 12, 0.7f)
                            Box(
                                Modifier.padding(top = 4.dp).size(34.dp)
                                    .background(if (lit) Brush.verticalGradient(t.primary) else Brush.verticalGradient(listOf(Color.Black.copy(0.25f), Color.Black.copy(0.25f))), CircleShape)
                                    .then(if (day == today) Modifier.border(2.dp, Color(0xFFFFD34D), CircleShape) else Modifier),
                                contentAlignment = Alignment.Center,
                            ) { if (lit) Icon(GameIcons.Check, null, tint = t.primaryInk, modifier = Modifier.size(18.dp)) }
                        }
                    }
                }
            }
            FeatureCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    FeatureLabel("TODAY'S PUZZLE", 18)
                    FeatureLabel(if (done) "Solved! A new one arrives tomorrow." else "Same puzzle for every player today.", 13, 0.75f)
                    Spacer(Modifier.height(12.dp))
                    if (done) GlassPill { Icon(GameIcons.Check, null, tint = Color(0xFF63E47E), modifier = Modifier.size(18.dp)); PillText("DONE") }
                    else PrimaryButton("PLAY TODAY", Modifier.fillMaxWidth(), height = 64.dp, fontSize = 26.sp, icon = GameIcons.Play) { feedback.good(); onPlay() }
                }
            }
            FeatureCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureLabel("STREAK REWARDS", 16)
                    for (r in Streak.REWARDS) {
                        val got = Streak.best(store) >= r.day
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(if (got) GameIcons.Check else GameIcons.Lock, null, tint = if (got) Color(0xFF63E47E) else t.glassInk.copy(0.6f), modifier = Modifier.size(20.dp))
                            FeatureLabel("Day ${r.day}", 14)
                            FeatureLabel(r.title, 14, 0.75f)
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

/** Shown when the streak grows. Mentions the reward when this day earned one. */
@Composable
fun StreakPopup(streak: Int, onClose: () -> Unit) {
    val t = LocalGameTheme.current
    val feedback = rememberFeedback()
    LaunchedEffect(Unit) { feedback.win() }
    val reward = Streak.REWARDS.find { it.day == streak }
    val pulse by rememberInfiniteTransition(label = "flame").animateFloat(0.92f, 1.08f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "s")
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).pressable(label = "Close") { onClose() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(32.dp).fillMaxWidth().background(Brush.verticalGradient(t.bg), RoundedCornerShape(28.dp))
                .border(2.dp, Color(0xFFFFD34D), RoundedCornerShape(28.dp)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GoldIcon(GameIcons.Flame, 96.dp, Modifier.scale(pulse))
            GradientText("$streak DAY STREAK!", t.logo2, 32.sp)
            Text("Come back tomorrow to keep it going.", color = t.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            if (reward != null) GlassPill { Icon(GameIcons.Gift, null, tint = Color(0xFFFFD34D), modifier = Modifier.size(18.dp)); PillText("Unlocked: ${reward.title}") }
            PrimaryButton("NICE!", Modifier.fillMaxWidth(), height = 60.dp, fontSize = 26.sp) { feedback.tap(); onClose() }
        }
    }
}

/** Current chapter of 10 levels as a winding path. Only the current level is playable from here. */
@Composable
fun LevelMapScreen(onPlay: () -> Unit) {
    val t = LocalGameTheme.current
    val store = rememberStore()
    val feedback = rememberFeedback()
    val level = store.get("level", 1)
    val chapter = (level - 1) / 10
    val pulse by rememberInfiniteTransition(label = "map").animateFloat(1f, 1.12f, infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "p")
    GameBackground {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            ScreenTop("CHAPTER ${chapter + 1}")
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 8.dp)) {
                for (i in 9 downTo 0) { // level 1 of the chapter at the bottom
                    val n = chapter * 10 + i + 1
                    val stars = Progress.starsFor(store, n)
                    val current = n == level
                    val passed = n < level
                    val align = when (i % 4) { 0 -> Alignment.Start; 2 -> Alignment.End; else -> Alignment.CenterHorizontally }
                    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = align) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.size(66.dp).scale(if (current) pulse else 1f)
                                    .background(
                                        when {
                                            current -> Brush.verticalGradient(t.primary)
                                            passed -> Brush.verticalGradient(t.rim)
                                            else -> Brush.verticalGradient(listOf(Color.Black.copy(0.35f), Color.Black.copy(0.35f)))
                                        },
                                        CircleShape,
                                    )
                                    .border(3.dp, if (current) Color.White else Color.White.copy(0.3f), CircleShape)
                                    .then(if (current) Modifier.pressable(label = "Play level $n") { feedback.good(); onPlay() } else Modifier),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (n > level) Icon(GameIcons.Lock, "Locked level $n", tint = Color.White.copy(0.6f), modifier = Modifier.size(24.dp))
                                else Text("$n", color = if (current) t.primaryInk else Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            }
                            if (passed) Row { repeat(3) { GoldIcon(if (it < stars) GameIcons.StarGold else GameIcons.StarEmpty, 16.dp) } }
                        }
                    }
                }
            }
            PrimaryButton("PLAY LEVEL $level", Modifier.padding(20.dp).fillMaxWidth(), height = 66.dp, fontSize = 26.sp, icon = GameIcons.Play) { feedback.good(); onPlay() }
        }
    }
}

/** Board skins: free, earned by streak, or unlocked with a rewarded ad. */
@Composable
fun ThemesScreen(ads: Ads, onSelect: (Skin) -> Unit) {
    val t = LocalGameTheme.current
    val store = rememberStore()
    val feedback = rememberFeedback()
    var refresh by remember { mutableIntStateOf(0) } // re-read owned state after an unlock
    val current = LocalSkin.current
    GameBackground {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            ScreenTop("THEMES")
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (row in SKINS.chunked(2)) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (skin in row) {
                        val owned = refresh >= 0 && Skins.owned(store, skin) // reads refresh so an unlock redraws
                        val selected = skin.id == current.id
                        val gems = skin.gems ?: GEMS
                        Column(
                            Modifier.weight(1f).glass(t, RoundedCornerShape(20.dp))
                                .then(if (selected) Modifier.border(3.dp, Color(0xFFFFD34D), RoundedCornerShape(20.dp)) else Modifier)
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(Modifier.fillMaxWidth().height(84.dp).background(Brush.verticalGradient(skin.tray ?: t.tray), RoundedCornerShape(14.dp)).border(3.dp, Brush.verticalGradient(skin.rim ?: t.rim), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    for (r in 0 until 2) Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        for (c in 0 until 3) { val g = gems[r * 3 + c]; Box(Modifier.size(22.dp).gem(g.first, g.second, 5.dp)) }
                                    }
                                }
                            }
                            FeatureLabel(skin.name, 16)
                            when {
                                selected -> FeatureLabel("IN USE", 12, 0.7f)
                                owned -> PrimaryButton("USE", Modifier.fillMaxWidth(), height = 40.dp, fontSize = 18.sp) { feedback.good(); Skins.select(store, skin); onSelect(skin) }
                                skin.byAd -> PrimaryButton("▶ WATCH AD", Modifier.fillMaxWidth(), height = 40.dp, fontSize = 15.sp) {
                                    if (ads.rewardedReady) ads.showRewarded { Skins.unlock(store, skin); refresh++ }
                                }
                                else -> Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(GameIcons.Lock, null, tint = t.glassInk.copy(0.7f), modifier = Modifier.size(16.dp))
                                    FeatureLabel(" ${skin.streakDay}-day streak", 12, 0.7f)
                                }
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/** Sister games: open if installed, else the Play Store page. */
@Composable
fun MoreGamesScreen() {
    val context = LocalContext.current
    val feedback = rememberFeedback()
    GameBackground {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            ScreenTop("MORE GAMES")
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (g in ALL_GAMES.filter { it.pkg != context.packageName }) {
                    val installed = remember(g.pkg) { isInstalled(context, g.pkg) }
                    Row(
                        Modifier.fillMaxWidth().glass(LocalGameTheme.current, RoundedCornerShape(20.dp))
                            .pressable(label = "Open ${g.name}") { feedback.tap(); openGame(context, g.pkg) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(Modifier.size(56.dp).background(Brush.verticalGradient(g.theme.bg), RoundedCornerShape(16.dp)).border(2.dp, Brush.verticalGradient(g.theme.rim), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                            Text(g.name.take(1), color = g.theme.logo1.last(), fontSize = 28.sp, fontWeight = FontWeight.Black)
                        }
                        Column(Modifier.weight(1f)) {
                            FeatureLabel(g.name, 17)
                            FeatureLabel(g.blurb, 13, 0.7f)
                        }
                        GlassPill { PillText(if (installed) "OPEN" else "GET") }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
