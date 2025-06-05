package com.cipherxzc.whatsnext

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cipherxzc.whatsnext.ui.theme.WhatsNextTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WhatsNextTheme {
                WhatsNextApp()
            }
        }
    }
}