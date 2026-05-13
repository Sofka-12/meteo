package com.example.meteopipli.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF358D6B),
    secondary = Color(0xFF397E91),
    tertiary = Color(0xFF56A458),
    background = Color(0xFFE9ECF1),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF695C51),
    secondary = Color(0xFF546775),
    tertiary = Color(0xFF375647),
    background = Color(0xFF1E1E24),
    surface = Color(0xFFB6B6BB),
    onPrimary = Color.Black,
    onBackground = Color(0xFFECE9E9),
)

@Composable
fun MeteopipliTheme(
    darkTheme: Boolean,  // ← ТЕПЕРЬ ПРИНИМАЕМ ПАРАМЕТР
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    SideEffect {
        val window = (view.context as androidx.activity.ComponentActivity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}