package com.techeliteclub.game.core

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Line icons (24×24, like the mockups). Tint them with Icon(tint = …). */
object GameIcons {
    private fun circle(cx: Float, cy: Float, r: Float) = "M${cx - r},${cy}a$r,$r 0 1,0 ${2 * r},0a$r,$r 0 1,0 ${-2 * r},0"

    private fun line(name: String, vararg paths: String, width: Float = 2.3f): ImageVector {
        val b = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        for (d in paths) {
            b.addPath(
                pathData = addPathNodes(d), fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            )
        }
        return b.build()
    }

    private fun filled(name: String, d: String, fill: Brush, outline: Color? = null, outlineWidth: Float = 1.2f): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).addPath(
            pathData = addPathNodes(d), fill = fill, stroke = outline?.let { SolidColor(it) }, strokeLineWidth = outlineWidth,
            strokeLineJoin = StrokeJoin.Round,
        ).build()

    val Back = line("back", "M15 5l-7 7 7 7", width = 2.6f)
    val Forward = line("forward", "M9 5l7 7-7 7", width = 2.8f)
    val Undo = line("undo", "M9 14L4 9l5-5", "M4 9h10.5a5.5 5.5 0 0 1 0 11H11")
    val Restart = line("restart", "M3 12a9 9 0 1 0 3-6.7L3 8", "M3 3v5h5")
    val Bulb = line("bulb", "M9 18h6M10 22h4M12 2a7 7 0 0 0-4 12.7V17h8v-2.3A7 7 0 0 0 12 2z")
    val Plus = line("plus", "M12 5v14M5 12h14", width = 2.6f)
    val Shuffle = line("shuffle", "M16 3h5v5M4 20 21 3M21 16v5h-5M15 15l6 6M4 4l5 5")
    val Erase = line("erase", "M20 20H9L4 15l9-9 7 7-5 5", "M6 13l5 5")
    val Pencil = line("pencil", "M12 20h9", "M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z")
    val Wand = line("wand", "M15 4V2M15 16v-2M8 9h2M20 9h2M17.8 11.8 19 13M17.8 6.2 19 5M3 21l9-9M12.2 6.2 11 5")
    val Gear = line(
        "gear", circle(12f, 12f, 3.2f),
        "M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z",
        width = 2.1f,
    )
    val Sound = line("sound", "M11 5 6 9H3v6h3l5 4V5z", "M15.5 8.5a5 5 0 0 1 0 7M18.5 5.5a9 9 0 0 1 0 13")
    val SoundOff = line("sound_off", "M11 5 6 9H3v6h3l5 4V5z", "M16 9l6 6M22 9l-6 6")
    val Music = line("music", "M9 18V5l11-2v13", circle(6f, 18f, 3f), circle(17f, 16f, 3f))
    val MusicOff = line("music_off", "M9 18V5l11-2v13", circle(6f, 18f, 3f), circle(17f, 16f, 3f), "M3 3l18 18")
    val Help = line("help", "M9.1 9a3 3 0 0 1 5.8 1c0 2-3 3-3 3", "M12 17h.01", width = 2.6f)
    val Clock = line("clock", circle(12f, 13f, 8f), "M12 9v4l2 2M9 2h6")
    val Trophy = line("trophy", "M8 21h8M12 17v4M7 4h10v5a5 5 0 0 1-10 0V4z", "M17 5h3v2a3 3 0 0 1-3 3M7 5H4v2a3 3 0 0 0 3 3")
    val Check = line("check", "M5 12l5 5L20 7", width = 3.2f)
    val Close = line("close", "M5 5l14 14M19 5L5 19", width = 3f)
    val Bell = line("bell", "M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9", "M13.7 21a2 2 0 0 1-3.4 0")
    val Vibrate = line("vibrate", "M8 4h8v16H8z", "M4 8v8M20 8v8")
    val Lock = line("lock", "M5 11h14v10H5z", "M8 11V7a4 4 0 0 1 8 0v4")
    val Gift = line("gift", "M4 9h16v4H4zM6 13h12v8H6z", "M12 9v12M12 9C10 5 6 5 7.5 7.5 8.5 9 12 9 12 9zM12 9c2-4 6-4 4.5-1.5C15.5 9 12 9 12 9z")
    val LevelMap = line("map", "M3 6l6-2 6 2 6-2v14l-6 2-6-2-6 2z", "M9 4v14M15 6v14")
    val Palette = line("palette", "M12 3a9 9 0 1 0 0 18c1.5 0 2-1 2-2s-1-1.5-1-2.5S14 15 15 15h2a4 4 0 0 0 4-4c0-4.4-4-8-9-8z", "M7.5 11.5h.01M9.5 7.5h.01M14.5 7.5h.01")
    val Grid = line("grid", "M4 4h7v7H4zM13 4h7v7h-7zM4 13h7v7H4zM13 13h7v7h-7z")
    val Calendar = line("calendar", "M4 6h16v15H4z", "M4 10h16M8 3v4M16 3v4")
    val Flame = filled(
        "flame",
        "M12 2c1 4 6 6 6 12a6 6 0 0 1-12 0c0-3 1.5-4.5 2.5-5.5.3 2 1.2 3 2.2 3.3C10 9 10.5 5 12 2z",
        Brush.verticalGradient(listOf(Color(0xFFFFE07A), Color(0xFFFF8A1E), Color(0xFFE5321E)), startY = 2f, endY = 22f),
    )

    val Play = filled("play", "M7 4.5v15a1 1 0 0 0 1.5.9l12-7.5a1 1 0 0 0 0-1.8l-12-7.5A1 1 0 0 0 7 4.5z", SolidColor(Color.White))
    val Heart = filled(
        "heart",
        "M12 21s-7.5-4.6-9.5-9.3C1.2 8.5 3.3 5 6.8 5c2 0 3.6 1.2 5.2 3 1.6-1.8 3.2-3 5.2-3 3.5 0 5.6 3.5 4.3 6.7C19.5 16.4 12 21 12 21z",
        SolidColor(Color(0xFFFF4D6D)),
    )

    private val goldBrush = Brush.verticalGradient(listOf(Color(0xFFFFF3B0), Color(0xFFFFD23F), Color(0xFFE38B00)), startY = 2f, endY = 22f)
    private const val STAR = "M12 2.6l2.9 5.9 6.5.9-4.7 4.6 1.1 6.5L12 17.4l-5.8 3.1 1.1-6.5L2.6 9.4l6.5-.9z"
    private const val CROWN = "M3 17.5h18l-1.6-10-4.6 4.2L12 4.5 9.2 11.7 4.6 7.5zM3 18.4h18v2.6H3z"

    /** Gold metallic star and crown (not tinted: use Image or Icon with tint = Color.Unspecified). */
    val StarGold = filled("star_gold", STAR, goldBrush, Color(0xFF8A4B00))
    val StarEmpty = filled("star_empty", STAR, SolidColor(Color.White.copy(alpha = 0.18f)), Color.White.copy(alpha = 0.35f))
    val CrownGold = filled("crown_gold", CROWN, goldBrush, Color(0xFF7A4200), 1.1f)
    val CrownEmpty = filled("crown_empty", CROWN, SolidColor(Color.White.copy(alpha = 0.18f)), Color.White.copy(alpha = 0.35f), 1.1f)
}
