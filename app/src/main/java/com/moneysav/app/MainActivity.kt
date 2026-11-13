package com.moneysav.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import com.moneysav.app.ui.navigation.AppShell
import com.moneysav.app.ui.theme.LiquidTheme
import com.moneysav.app.ui.theme.ThemeState

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val repository = (application as MoneySavApp).repository
        val themeState = ThemeState(
            getSharedPreferences("moneysav_settings", MODE_PRIVATE),
        )

        setContent {
            // Crossfade halus saat ganti aksen / mode gelap.
            AnimatedContent(
                targetState = themeState.accent to themeState.dark,
                transitionSpec = {
                    fadeIn(tween(350)) togetherWith fadeOut(tween(350))
                },
                label = "theme",
            ) { (accent, dark) ->
                LiquidTheme(accentColor = accent, darkTheme = dark) {
                    AppShell(repository = repository, themeState = themeState)
                }
            }
        }
    }
}
