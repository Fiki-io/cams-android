package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presentation.CameraScreen
import com.example.presentation.CameraViewModel
import com.example.ui.theme.CamsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CamsTheme {
                val viewModel: CameraViewModel = viewModel {
                    CameraViewModel(applicationContext)
                }
                CameraScreen(viewModel = viewModel)
            }
        }
    }
}
