package com.jeiel85.forestpetgarden

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeiel85.forestpetgarden.ui.GardenGameScreen
import com.jeiel85.forestpetgarden.ui.theme.MyApplicationTheme
import com.jeiel85.forestpetgarden.viewmodel.GardenViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: GardenViewModel = viewModel()
        GardenGameScreen(viewModel = viewModel)
      }
    }
  }
}
