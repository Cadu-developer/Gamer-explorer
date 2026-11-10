package com.gameexplorer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gameexplorer.app.userinterface.GameTheme
import com.gameexplorer.app.userinterface.GameExplorerApp
import com.gameexplorer.app.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { GameTheme { val vm: AppViewModel = viewModel(); GameExplorerApp(vm) } }
    }
}
