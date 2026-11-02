package com.range3.warikan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.range3.warikan.data.DataStoreAppStore
import com.range3.warikan.ui.SettingsScreen
import com.range3.warikan.ui.WarikanScreen
import com.range3.warikan.ui.WarikanViewModel
import com.range3.warikan.ui.theme.WarikanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WarikanTheme {
                val vm = viewModel { WarikanViewModel(DataStoreAppStore(applicationContext)) }
                WarikanApp(vm)
            }
        }
    }
}

@Composable
private fun WarikanApp(vm: WarikanViewModel) {
    if (!vm.loaded) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
        return
    }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    if (showSettings) {
        BackHandler { showSettings = false }
        SettingsScreen(vm, onBack = { showSettings = false })
    } else {
        WarikanScreen(vm, onOpenSettings = { showSettings = true })
    }
}
