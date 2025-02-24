package com.example.todoapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColorScheme(
    primary = Yellow,
    onPrimary = Black,
    secondary = Cream,
    onSecondary = Black,
    tertiary = Green,
    onTertiary = White,
    surface = White,
    onSurface = Black,
    background = Green,
    onBackground = White,
)

private val DarkColorPalette = darkColorScheme(
    surface = Blue700,
    onSurface = White,
    background = Blue500,
    onBackground = White
)

@Composable
fun ToDoAppTheme(content: @Composable () -> Unit) {

    val colors = if (isSystemInDarkTheme()) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}