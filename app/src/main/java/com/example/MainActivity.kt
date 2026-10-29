package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AuthState
import com.example.ui.screens.AuthLockScreen
import com.example.ui.screens.CyberDashboard
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CyberViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CyberAppRoot()
            }
        }
    }
}

@Composable
fun CyberAppRoot(viewModel: CyberViewModel = viewModel()) {
    val authState by viewModel.authState.collectAsState()

    // Handle back button when unlocked: lock the terminal
    if (authState == AuthState.GRANTED) {
        BackHandler {
            viewModel.lockTerminal()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) { innerPadding ->
        Crossfade(
            targetState = authState,
            animationSpec = tween(400),
            label = "auth_crossfade"
        ) { state ->
            when (state) {
                AuthState.GRANTED -> {
                    CyberDashboard(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    AuthLockScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
