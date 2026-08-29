package com.amansharma.jewelryinventory.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.amansharma.jewelryinventory.navigation.JewelryNavHost
import com.amansharma.jewelryinventory.ui.theme.JewelryInventoryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JewelryInventoryTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    JewelryNavHost()
                }
            }
        }
    }
}
