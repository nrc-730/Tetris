package com.example.fallingblocks.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFFB71C1C),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFF101010),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF202020),
    onSurface = Color(0xFFFFFFFF),
    secondary = Color(0xFF9E9E9E),
    onSecondary = Color(0xFF000000),
)

@Composable
fun FallingBlocksTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
