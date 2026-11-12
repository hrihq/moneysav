package com.moneysav.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.moneysav.app.ui.navigation.AppShell
import com.moneysav.app.ui.theme.LiquidTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val repository = (application as MoneySavApp).repository

        setContent {
            LiquidTheme {
                AppShell(repository = repository)
            }
        }
    }
}
