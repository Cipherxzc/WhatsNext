package com.cipherxzc.whatsnext.ui.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun WhatsNextTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val useDynamic = true
    val colors =
        if (useDynamic)
            if (useDarkTheme) dynamicDarkColorScheme(LocalContext.current)
            else dynamicLightColorScheme(LocalContext.current)
        else
            if (useDarkTheme) WhatsNextDarkScheme
            else WhatsNextLightScheme

    MaterialTheme(
        colorScheme = colors,
        shapes      = WhatsNextShapes,
        content     = content
    )
}