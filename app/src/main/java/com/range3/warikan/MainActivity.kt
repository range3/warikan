package com.range3.warikan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.range3.warikan.ui.WarikanScreen
import com.range3.warikan.ui.theme.WarikanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WarikanTheme {
                WarikanScreen()
            }
        }
    }
}
