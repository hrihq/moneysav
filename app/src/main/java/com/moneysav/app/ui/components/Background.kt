package com.moneysav.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.moneysav.app.ui.theme.LiquidMotion
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Latar berisi blob lembut yang bergeser saat pindah tab.
 *
 * Ditempatkan di dalam layer pengambilan backdrop, jadi pergerakannya ikut
 * terbias oleh nav bar kaca — latar terasa hidup, bukan gambar statis.
 */
@Composable
fun AnimatedBackdrop(
    tabIndex: Int,
    modifier: Modifier = Modifier,
) {
    val surface = MiuixTheme.colorScheme.surface
    val primaryContainer = MiuixTheme.colorScheme.primaryContainer
    val secondaryContainer = MiuixTheme.colorScheme.secondaryContainer

    val progress by animateFloatAsState(
        targetValue = tabIndex.toFloat(),
        animationSpec = LiquidMotion.gentle(),
        label = "backdropTab",
    )

    Box(modifier.fillMaxSize().background(surface)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            fun blob(index: Int, color: Color, radius: Float, baseX: Float, baseY: Float) {
                val t = (progress - index).coerceIn(-1f, 1f)
                drawCircle(
                    color = color,
                    radius = radius,
                    center = Offset(baseX + t * w * 0.14f, baseY + t * h * 0.07f),
                )
            }

            blob(0, primaryContainer.copy(alpha = 0.24f), w * 0.46f, w * 0.12f, h * 0.16f)
            blob(1, secondaryContainer.copy(alpha = 0.24f), w * 0.40f, w * 0.98f, h * 0.32f)
            blob(2, primaryContainer.copy(alpha = 0.14f), w * 0.56f, w * 0.48f, h * 0.98f)
        }
    }
}
