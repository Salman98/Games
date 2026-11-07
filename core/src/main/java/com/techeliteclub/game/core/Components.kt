package com.techeliteclub.game.core

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

/** Deep lit background: gradient, soft light bloom at the top, dark vignette, faint sparkles. */
@Composable
fun GameBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val t = LocalGameTheme.current
    val sparkles = remember { Random(7).let { r -> List(16) { Triple(r.nextFloat(), r.nextFloat(), 1f + r.nextFloat() * 1.5f) } } }
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(t.bg))
            .drawBehind {
                drawRect(Brush.radialGradient(listOf(t.bloom, Color.Transparent), Offset(size.width / 2, 0f), size.width * 0.85f))
                drawRect(
                    Brush.radialGradient(
                        0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = if (t.light) 0.1f else 0.45f),
                        center = Offset(size.width / 2, size.height / 2), radius = size.maxDimension * 0.7f,
                    )
                )
                if (!t.light) for ((x, y, r) in sparkles) {
                    drawCircle(Color.White.copy(alpha = 0.45f), r.dp.toPx(), Offset(x * size.width, y * size.height))
                }
            },
        content = content,
    )
}

/** Frosted glass surface (white glass on light themes). */
fun Modifier.glass(t: GameTheme, shape: Shape): Modifier =
    if (t.light) shadow(6.dp, shape).background(Color.White.copy(alpha = 0.78f), shape).border(1.dp, Color.White, shape)
    else background(Color.White.copy(alpha = 0.12f), shape).border(1.dp, Color.White.copy(alpha = 0.24f), shape)

/** Click with a little squash, like the mockups' "pressable" buttons. */
@Composable
fun Modifier.pressable(enabled: Boolean = true, label: String? = null, onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, label = "press")
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = source, indication = null, enabled = enabled, onClickLabel = label, role = Role.Button, onClick = onClick)
}

@Composable
fun GlassIconButton(icon: ImageVector, description: String, size: Dp = 46.dp, onClick: () -> Unit) {
    val t = LocalGameTheme.current
    Box(
        Modifier.size(size).glass(t, CircleShape).clip(CircleShape).pressable(onClick = onClick).semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = t.glassInk, modifier = Modifier.size(size * 0.48f)) }
}

@Composable
fun GlassPill(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val t = LocalGameTheme.current
    Row(
        modifier.glass(t, RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        content = content,
    )
}

@Composable
fun PillText(text: String, color: Color = LocalGameTheme.current.glassInk) =
    Text(text, color = color, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)

/** Big chunky gradient button with a dark lower lip and a glow (PLAY, NEXT LEVEL). */
@Composable
fun PrimaryButton(
    text: String, modifier: Modifier = Modifier, height: Dp = 76.dp, fontSize: TextUnit = 32.sp,
    icon: ImageVector? = null, colors: List<Color>? = null, ink: Color? = null, onClick: () -> Unit,
) {
    val t = LocalGameTheme.current
    val shape = RoundedCornerShape(28.dp)
    val fill = colors ?: t.primary
    Box(
        modifier
            .height(height)
            .pressable(onClick = onClick)
            .shadow(16.dp, shape, ambientColor = t.glow, spotColor = t.glow)
            .background(Brush.verticalGradient(fill), shape)
            .drawBehind {
                val r = CornerRadius(28.dp.toPx())
                drawRoundRect(Color.Black.copy(alpha = 0.22f), Offset(0f, size.height - 7.dp.toPx()), Size(size.width, 7.dp.toPx()), r)
                drawRoundRect(
                    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.4f), Color.Transparent), endY = size.height * 0.45f),
                    size = Size(size.width, size.height * 0.5f), cornerRadius = r,
                )
            }
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (icon != null) Icon(icon, null, tint = ink ?: t.primaryInk, modifier = Modifier.size(fontSize.value.dp * 0.8f))
            Text(text, style = displayStyle(fontSize), color = ink ?: t.primaryInk, letterSpacing = 2.sp, maxLines = 1)
        }
    }
}

/** Bottom tool button. Gold = the hint button. [badge] shows top-right ("1 FREE"). */
@Composable
fun ToolButton(
    label: String, icon: ImageVector, modifier: Modifier = Modifier, enabled: Boolean = true,
    gold: Boolean = false, badge: String? = null, onClick: () -> Unit,
) {
    val t = LocalGameTheme.current
    val shape = RoundedCornerShape(22.dp)
    val ink = if (gold) Color(0xFF5A3000) else t.glassInk
    Box(modifier.height(68.dp)) {
        Column(
            Modifier
                .fillMaxSize()
                .then(
                    if (gold) Modifier.shadow(10.dp, shape, ambientColor = Color(0xFFFFB400), spotColor = Color(0xFFFFB400))
                        .background(Brush.verticalGradient(listOf(Color(0xFFFFE58A), Color(0xFFFFC533), Color(0xFFF2A100))), shape)
                        .border(1.dp, Color(0xFFFFF0B3), shape)
                    else Modifier.glass(t, shape)
                )
                .clip(shape)
                .pressable(enabled = enabled, onClick = onClick)
                .graphicsLayer { alpha = if (enabled) 1f else 0.45f },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(icon, null, tint = ink, modifier = Modifier.size(24.dp))
            Text(label, color = ink, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        }
        if (badge != null) {
            Text(
                badge,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-10).dp)
                    .shadow(4.dp, RoundedCornerShape(50))
                    .background(Brush.verticalGradient(listOf(Color(0xFF63E47E), Color(0xFF1FA046))), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp,
            )
        }
    }
}

/** Board as a physical tray: metallic rim, recessed surface with an inner shadow. */
@Composable
fun Bezel(modifier: Modifier = Modifier, padding: Dp = 10.dp, radius: Dp = 28.dp, content: @Composable BoxScope.() -> Unit) {
    val t = LocalGameTheme.current
    val outer = RoundedCornerShape(radius)
    val inner = RoundedCornerShape(radius - 6.dp)
    // propagateMinConstraints: the tray fills a fixed-size bezel but still hugs content when the bezel is not sized.
    Box(
        modifier
            .shadow(18.dp, outer)
            .background(Brush.linearGradient(t.rim), outer)
            .padding(6.dp),
        propagateMinConstraints = true,
    ) {
        Box(
            Modifier
                .clip(inner)
                .background(Brush.verticalGradient(t.tray))
                .drawBehind {
                    drawRect(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent), endY = 16.dp.toPx()))
                }
                .padding(padding),
            content = content,
        )
    }
}

/** Glossy gem look for a piece: gradient body, top shine, specular spot, darker lower lip. */
fun Modifier.gem(top: Color, base: Color, radius: Dp = 8.dp): Modifier = this.drawBehind {
    val r = CornerRadius(radius.toPx())
    drawRoundRect(Brush.verticalGradient(listOf(top, base)), cornerRadius = r)
    drawRoundRect(
        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.26f)), startY = size.height * 0.78f),
        cornerRadius = r,
    )
    drawRoundRect(
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.5f), Color.Transparent), endY = size.height * 0.45f),
        size = Size(size.width, size.height * 0.5f), cornerRadius = r,
    )
    val spot = Offset(size.width * 0.28f, size.height * 0.24f)
    val sr = size.minDimension * 0.2f
    drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.8f), Color.Transparent), spot, sr), sr, spot)
}

/** Logo text: gradient fill, dark outline and a solid drop "depth" below it. */
@Composable
fun GradientText(text: String, colors: List<Color>, size: TextUnit, modifier: Modifier = Modifier, stroke: Color = LocalGameTheme.current.stroke) {
    val base = displayStyle(size).copy(textAlign = TextAlign.Center, letterSpacing = 1.5.sp)
    val outline = with(LocalDensity.current) { (size.value / 11f).dp.toPx() }
    val depth = (size.value / 14f).dp
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(text, style = base.copy(color = stroke, drawStyle = Stroke(width = outline, join = StrokeJoin.Round)), modifier = Modifier.offset(y = depth))
        Text(text, style = base.copy(color = stroke, drawStyle = Stroke(width = outline, join = StrokeJoin.Round)))
        Text(text, style = base.copy(brush = Brush.verticalGradient(colors)))
    }
}

/** Gold star / crown row, e.g. level progress. */
@Composable
fun GoldIcon(icon: ImageVector, size: Dp, modifier: Modifier = Modifier) =
    Icon(icon, null, tint = Color.Unspecified, modifier = modifier.size(size))
