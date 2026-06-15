package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.screens.RootScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MZKViewModel
import com.example.ui.viewmodel.MZKViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: MZKViewModel by viewModels {
        MZKViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Firestore programmatically
        com.example.data.repository.FirestoreSyncManager.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    RootScreen(viewModel = viewModel)
                }
            }
        }
    }
}
