package com.gameexplorer.app.userinterface

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Background = Color(0xFF0B0B14)
val Surface = Color(0xFF1A1A2E)
val Primary = Color(0xFF8B5CF6)
val Accent = Color(0xFFA78BFA)
val Cyan = Color(0xFF22D3EE)
val Pale = Color(0xFFF8FAFC)

@Composable fun GameTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary = Primary, secondary = Accent,
        background = Background, surface = Surface, onPrimary = Pale, onBackground = Pale,
        onSurface = Pale, error = Color(0xFFFF647C)), content = content)
}
