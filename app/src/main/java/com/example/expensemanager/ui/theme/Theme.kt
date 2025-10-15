package com.example.expensemanager.ui.theme

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.MaterialTheme
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.darkColors
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColors(
    primary = Color(0xFF0F9D58),
    surface = Color.White
)

private val DarkColors = darkColors(
    primary = Color(0xFF0F9D58),
    surface = Color(0xFF121212)
)

@Composable
fun ComposeExpenseTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colors = colors) {
        content()
    }
}
