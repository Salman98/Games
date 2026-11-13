package com.techeliteclub.game.core

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

/** Colours of one game's world (matches the approved mockups). Lists are gradient stops, top to bottom. */
data class GameTheme(
    val bg: List<Color>,
    val bloom: Color,
    val ink: Color,
    val stroke: Color,
    val primary: List<Color>,
    val primaryInk: Color,
    val glow: Color,
    val logo1: List<Color>,
    val logo2: List<Color>,
    val rim: List<Color>,
    val tray: List<Color>,
    val light: Boolean = false,
) {
    /** Text/icon colour on frosted-glass controls. */
    val glassInk: Color get() = if (light) Color(0xFF2B2A4A) else Color.White
}

private fun c(hex: Long) = Color(hex)
private val GREEN = listOf(c(0xFF7DF59A), c(0xFF20C55A), c(0xFF109A42))
private val GOLD = listOf(c(0xFFFFE58A), c(0xFFFFC533), c(0xFFEF9A00))
private val GOLD_LOGO = listOf(c(0xFFFFF7CF), c(0xFFFFD34D), c(0xFFE08A00))
private val GOLD_RIM = listOf(c(0xFFFFF1B0), c(0xFFD9A020), c(0xFF8A5A00))

object Themes {
    val Block = GameTheme(
        bg = listOf(c(0xFF2A1470), c(0xFF170A45), c(0xFF0B0526)), bloom = c(0x8CA06EFF), ink = c(0xFFE8DDFF), stroke = c(0xFF1A0752),
        primary = GREEN, primaryInk = Color.White, glow = c(0x7328DC6E),
        logo1 = listOf(Color.White, c(0xFFD8C8FF)), logo2 = listOf(c(0xFFFFF3A0), c(0xFFFFC533), c(0xFFFF8A00)),
        rim = listOf(c(0xFFB9A6FF), c(0xFF5B3FC4), c(0xFF2A1A7A)), tray = listOf(c(0xFF160A3F), c(0xFF0E0630)),
    )
    val Crown = GameTheme(
        bg = listOf(c(0xFF1B2A6B), c(0xFF111A48), c(0xFF080D29)), bloom = c(0x61FFD25A), ink = c(0xFFF3E6C0), stroke = c(0xFF3A2200),
        primary = GOLD, primaryInk = c(0xFF3A2200), glow = c(0x73FFBE28),
        logo1 = GOLD_LOGO, logo2 = listOf(Color.White, c(0xFFCFD8FF)),
        rim = GOLD_RIM, tray = listOf(c(0xFF1A2252), c(0xFF1A2252)),
    )
    val Sort = GameTheme(
        bg = listOf(c(0xFF0B5A63), c(0xFF063A47), c(0xFF021C27)), bloom = c(0x595AF0E6), ink = c(0xFFC8FBFF), stroke = c(0xFF022B36),
        primary = listOf(c(0xFFFF9AD5), c(0xFFFF4FA3), c(0xFFD61F7A)), primaryInk = Color.White, glow = c(0x73FF50AA),
        logo1 = listOf(Color.White, c(0xFFA8F6FF)), logo2 = listOf(c(0xFFFFD0EF), c(0xFFFF6FB8), c(0xFFE0207E)),
        rim = listOf(c(0xFFC8FBFF), c(0xFF3FB7C4), c(0xFF0D5B66)), tray = listOf(c(0x8C00141E), c(0x8C00141E)),
    )
    val Arrow = GameTheme(
        bg = listOf(c(0xFF3A6FE0), c(0xFF2B49B8), c(0xFF1B2A78)), bloom = c(0x8CB4DCFF), ink = c(0xFFE3EEFF), stroke = c(0xFF14256E),
        primary = listOf(c(0xFFFFB27A), c(0xFFFF7A2E), c(0xFFE25300)), primaryInk = Color.White, glow = c(0x73FF7828),
        logo1 = listOf(Color.White, c(0xFFCFE2FF)), logo2 = listOf(c(0xFFFFE1C4), c(0xFFFF9A4A), c(0xFFFF6A00)),
        rim = listOf(Color.White, c(0xFFB9CDF5), c(0xFF6F87C9)), tray = listOf(c(0xFFF7F9FF), c(0xFFE6ECFA)),
    )
    val Mahjong = GameTheme(
        bg = listOf(c(0xFF0F6B47), c(0xFF0A4A31), c(0xFF04261A)), bloom = c(0x4D8CFFC8), ink = c(0xFFD6F7E6), stroke = c(0xFF3A0D06),
        primary = listOf(c(0xFFFF8A7A), c(0xFFE5383B), c(0xFFB0151E)), primaryInk = Color.White, glow = c(0x73E63C3C),
        logo1 = listOf(c(0xFFFFF7D6), c(0xFFFFD96A), c(0xFFE0A020)), logo2 = listOf(Color.White, c(0xFFFFE9C9)),
        rim = listOf(c(0xFFFF9B8A), c(0xFFC8212B), c(0xFF6E0B10)), tray = listOf(c(0xFF178A5C), c(0xFF0B5A3B)),
    )
    val Sudoku = GameTheme(
        bg = listOf(c(0xFFFBF6EC), c(0xFFF7EFE0), c(0xFFF3E9D4)), bloom = c(0xE6FFFFFF), ink = c(0xFF3B3F63), stroke = c(0xFF1C2357),
        primary = listOf(c(0xFF5D7CFF), c(0xFF3550E0), c(0xFF2338B5)), primaryInk = Color.White, glow = c(0x593550E0),
        logo1 = listOf(c(0xFF5D7CFF), c(0xFF2338B5)), logo2 = listOf(c(0xFFFFCF6B), c(0xFFF29A1B)),
        rim = listOf(Color.White, c(0xFFE5D6B5), c(0xFFBDA56F)), tray = listOf(Color.White, Color.White), light = true,
    )
    val Word = GameTheme(
        bg = listOf(c(0xFFFF8B6B), c(0xFFD9467A), c(0xFF6B1F6E)), bloom = c(0x8CFFDCA0), ink = c(0xFFFFE9EF), stroke = c(0xFF4A0F3A),
        primary = GREEN, primaryInk = Color.White, glow = c(0x6628C864),
        logo1 = listOf(Color.White, c(0xFFFFD9E6)), logo2 = listOf(c(0xFFFFF2A8), c(0xFFFFCF3A), c(0xFFFF9A00)),
        rim = listOf(c(0xFFFFF4E6), c(0xFFE8B892), c(0xFF9A5A3A)), tray = listOf(c(0xFFFFF8EF), c(0xFFFFF8EF)),
    )
    val Solitaire = GameTheme(
        bg = listOf(c(0xFF1F8A52), c(0xFF0F5A35), c(0xFF062D1A)), bloom = c(0x40AAFFC8), ink = c(0xFFD9F5E3), stroke = c(0xFF3A2400),
        primary = GOLD, primaryInk = c(0xFF3A2400), glow = c(0x73FFBE28),
        logo1 = GOLD_LOGO, logo2 = listOf(Color.White, c(0xFFD9F5E3)),
        rim = GOLD_RIM, tray = listOf(Color.Transparent, Color.Transparent),
    )
    val Car = GameTheme(
        bg = listOf(c(0xFF1B1F3A), c(0xFF11132A), c(0xFF07081A)), bloom = c(0x59FF4678), ink = c(0xFFD9DEFA), stroke = c(0xFF150622),
        primary = listOf(c(0xFFFF8A9A), c(0xFFFF2D55), c(0xFFC4002F)), primaryInk = Color.White, glow = c(0x80FF2D55),
        logo1 = listOf(Color.White, c(0xFFFFC2CF)), logo2 = listOf(c(0xFFB9FBFF), c(0xFF33D6FF), c(0xFF008CFF)),
        rim = listOf(c(0xFFC9D0E8), c(0xFF59607A), c(0xFF22263A)), tray = listOf(c(0xFF2A2F45), c(0xFF2A2F45)),
    )
    val M2048 = GameTheme(
        bg = listOf(c(0xFF4A2A1A), c(0xFF2E170C), c(0xFF170A04)), bloom = c(0x73FFAA50), ink = c(0xFFFFE6C8), stroke = c(0xFF3A1200),
        primary = listOf(c(0xFFFFB36B), c(0xFFFF7A1A), c(0xFFD65200)), primaryInk = Color.White, glow = c(0x73FF781E),
        logo1 = listOf(c(0xFFFFF3C4), c(0xFFFFC65A), c(0xFFFF7A1A)), logo2 = listOf(Color.White, c(0xFFFFE1C2)),
        rim = listOf(c(0xFFFFD9A8), c(0xFFC27A3A), c(0xFF6B3A12)), tray = listOf(c(0xFF5A3520), c(0xFF40240F)),
    )
    val MiniSudoku = GameTheme(
        bg = listOf(c(0xFFEFFBF6), c(0xFFE2F6EE), c(0xFFD3EFE3)), bloom = c(0xE6FFFFFF), ink = c(0xFF2F5A55), stroke = c(0xFF0F3B3A),
        primary = listOf(c(0xFF4FD8C2), c(0xFF0FA58F), c(0xFF0B7D6C)), primaryInk = Color.White, glow = c(0x590FA58F),
        logo1 = listOf(c(0xFF2CC4AD), c(0xFF0B7D6C)), logo2 = listOf(c(0xFFFFB86B), c(0xFFF2761B)),
        rim = listOf(Color.White, c(0xFFBFE6DA), c(0xFF7DBFAE)), tray = listOf(Color.White, Color.White), light = true,
    )
    val SunMoon = GameTheme(
        bg = listOf(c(0xFF3A2A8C), c(0xFF241A5E), c(0xFF100B33)), bloom = c(0x73FFC46B), ink = c(0xFFE9E2FF), stroke = c(0xFF1A0F45),
        primary = GOLD, primaryInk = c(0xFF3A2200), glow = c(0x73FFBE28),
        logo1 = GOLD_LOGO, logo2 = listOf(Color.White, c(0xFFCFD8FF)),
        rim = listOf(c(0xFFCFC2FF), c(0xFF6A55D0), c(0xFF2C1F7A)), tray = listOf(c(0xFF1C1450), c(0xFF140E3C)),
    )
    val Path = GameTheme(
        bg = listOf(c(0xFF8E2463), c(0xFF5C1447), c(0xFF2A0822)), bloom = c(0x73FF9AD0), ink = c(0xFFFFE3F1), stroke = c(0xFF3A0A28),
        primary = GOLD, primaryInk = c(0xFF3A2200), glow = c(0x73FFBE28),
        logo1 = listOf(Color.White, c(0xFFFFD3EA)), logo2 = GOLD_LOGO,
        rim = listOf(c(0xFFFFD0E8), c(0xFFC2478A), c(0xFF6A1646)), tray = listOf(c(0xFF3A0F2C), c(0xFF2A0920)),
    )
}

val LocalGameTheme = staticCompositionLocalOf { Themes.Block }

/** The logo font. Lilita One if its file is in core/src/main/assets/fonts, else a heavy system font. */
data class DisplayFont(val family: FontFamily, val weight: FontWeight)

val LocalDisplayFont = staticCompositionLocalOf { DisplayFont(FontFamily.Default, FontWeight.Black) }

const val DISPLAY_FONT_ASSET = "fonts/LilitaOne-Regular.ttf"

@Composable
fun rememberDisplayFont(): DisplayFont {
    val context = LocalContext.current
    return remember {
        try {
            DisplayFont(FontFamily(Typeface.createFromAsset(context.assets, DISPLAY_FONT_ASSET)), FontWeight.Normal)
        } catch (e: RuntimeException) { // file not added yet
            DisplayFont(FontFamily.Default, FontWeight.Black)
        }
    }
}

@Composable
fun displayStyle(size: TextUnit): TextStyle {
    val f = LocalDisplayFont.current
    return TextStyle(fontFamily = f.family, fontWeight = f.weight, fontSize = size)
}
