package com.example.composeexpense.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColors = lightColors(
    primary = androidx.compose.ui.graphics.Color(0xFF0F9D58),
    surface = androidx.compose.ui.graphics.Color.White
)

private val DarkColors = darkColors(
    primary = androidx.compose.ui.graphics.Color(0xFF0F9D58),
    surface = androidx.compose.ui.graphics.Color(0xFF121212)
)

@Composable
fun ComposeExpenseTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colors = colors) {
        content()
    }
}
