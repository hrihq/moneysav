package com.moneysav.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.moneysav.app.ui.theme.LiquidMotion
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

/**
 * Sheet setengah layar di bagian bawah.
 *
 * - Masuk dengan animasi naik (spring lembut);
 * - Strip pegangan di atas bisa ditarik ke bawah untuk menutup
 *   (dilepas sebelum ambang → memantul kembali);
 * - Latar gelap di belahnya bisa diklik untuk menutup.
 *
 * Konten dipasang di dalam [content]; scroll internal tetap berfungsi
 * karena gesture drag hanya dipasang pada strip pegangan.
 */
@Composable
fun ScreenBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val sheetHeightPx = maxHeightPx * 0.64f
        val scope = rememberCoroutineScope()
        val offsetY = remember { Animatable(0f) }

        LaunchedEffect(sheetHeightPx) {
            offsetY.snapTo(sheetHeightPx)
            offsetY.animateTo(0f, animationSpec = LiquidMotion.gentle())
        }

        // Scrim
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(with(density) { sheetHeightPx.toDp() })
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MiuixTheme.colorScheme.surface),
        ) {
            // Strip pegangan = area drag-to-dismiss.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(52.dp)
                    .pointerInput(sheetHeightPx) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (offsetY.value > sheetHeightPx * 0.26f) {
                                        offsetY.animateTo(sheetHeightPx, animationSpec = tween(180))
                                        onDismiss()
                                    } else {
                                        offsetY.animateTo(0f, animationSpec = LiquidMotion.gentle())
                                    }
                                }
                            },
                        ) { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetY.snapTo((offsetY.value + dragAmount).coerceIn(0f, sheetHeightPx))
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(width = 44.dp, height = 5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MiuixTheme.colorScheme.dividerLine),
                )
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = 52.dp),
            ) {
                content()
            }
        }
    }
}
