package com.example.todoapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors(
    surface = White,
    onSurface = Black,
    background = Blue200,
    onBackground = Black
)

private val DarkColorPalette = darkColors(
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
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}