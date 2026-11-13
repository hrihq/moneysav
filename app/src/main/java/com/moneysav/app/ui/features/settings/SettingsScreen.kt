package com.moneysav.app.ui.features.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.moneysav.app.ui.components.LiquidCard
import com.moneysav.app.ui.components.MoneySavIcons
import com.moneysav.app.ui.components.SectionHeader
import com.moneysav.app.ui.features.transactions.FilterChip
import com.moneysav.app.ui.theme.LiquidAccentColor
import com.moneysav.app.ui.theme.LiquidMotion
import com.moneysav.app.ui.theme.LiquidSpacing
import com.moneysav.app.ui.theme.ThemeState

@Composable
fun SettingsScreen(
    themeState: ThemeState,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = LiquidSpacing.page,
                end = LiquidSpacing.page,
                top = LiquidSpacing.inline,
                bottom = 40.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(LiquidSpacing.item),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
            }
            Text(
                text = "Pengaturan",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
        }

        LiquidCard {
            SectionHeader(icon = MoneySavIcons.palette, title = "Warna Aksen")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline)) {
                items(LiquidAccentColor.entries) { item ->
                    AccentSwatch(
                        item = item,
                        selected = themeState.accent == item,
                        onClick = { themeState.selectAccent(item) },
                    )
                }
            }
            Text(
                text = "Monet mengambil warna dari wallpaper (Android 12+).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LiquidCard {
            SectionHeader(icon = MoneySavIcons.settings, title = "Mode Gelap")
            Row(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline)) {
                FilterChip(
                    text = "Ikut Sistem",
                    selected = themeState.dark == null,
                    onClick = { themeState.selectDark(null) },
                )
                FilterChip(
                    text = "Terang",
                    selected = themeState.dark == false,
                    onClick = { themeState.selectDark(false) },
                )
                FilterChip(
                    text = "Gelap",
                    selected = themeState.dark == true,
                    onClick = { themeState.selectDark(true) },
                )
            }
        }
    }
}

@Composable
private fun AccentSwatch(
    item: LiquidAccentColor,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = LiquidMotion.bouncy(),
        label = "swatchScale",
    )
    val seed = item.seedColor()
    val brush = if (item == LiquidAccentColor.Dynamic) {
        Brush.linearGradient(
            listOf(Color(0xFF3482FF), Color(0xFF7050D5), Color(0xFFE91E63)),
        )
    } else {
        Brush.linearGradient(listOf(seed ?: Color.Gray, seed ?: Color.Gray))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(brush)
                .then(
                    if (selected) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    imageVector = MoneySavIcons.check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White,
                )
            }
        }
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
