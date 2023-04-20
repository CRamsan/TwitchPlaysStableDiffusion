package ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
    content: @Composable() () -> Unit,
) {
    MaterialTheme(
        colors = colorPalette,
        typography = typography,
        shapes = shapes,
        content = content
    )
}