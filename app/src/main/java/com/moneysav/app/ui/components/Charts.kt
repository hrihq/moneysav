package com.moneysav.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.moneysav.app.ui.theme.LiquidSpacing
import kotlin.math.max

data class BarEntry(
    val label: String,
    val income: Long,
    val expense: Long,
)

/**
 * Grafik batang pemasukan vs pengeluaran.
 * Dua batang membulat per titik data, tanpa sumbu / grid agar tetap bersih.
 */
@Composable
fun IncomeExpenseBarChart(
    entries: List<BarEntry>,
    modifier: Modifier = Modifier,
    incomeColor: Color = Color(0xFF356A35),
    expenseColor: Color = Color(0xFFC62828),
) {
    val maxValue = entries.maxOfOrNull { max(it.income, it.expense) } ?: 0L

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        ) {
            if (entries.isEmpty() || maxValue <= 0L) return@Canvas
            val gap = 12.dp.toPx()
            val groupWidth = (size.width - gap * (entries.size - 1)) / entries.size
            val barWidth = (groupWidth - gap) / 2f
            val corner = CornerRadius(6.dp.toPx(), 6.dp.toPx())

            entries.forEachIndexed { index, entry ->
                val groupLeft = index * (groupWidth + gap)
                val incomeHeight = entry.income.toFloat() / maxValue * size.height
                val expenseHeight = entry.expense.toFloat() / maxValue * size.height

                drawRoundRect(
                    color = incomeColor,
                    topLeft = Offset(groupLeft, size.height - incomeHeight),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = corner,
                )
                drawRoundRect(
                    color = expenseColor,
                    topLeft = Offset(groupLeft + barWidth + gap, size.height - expenseHeight),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = corner,
                )
            }
        }
        Spacer(Modifier.height(LiquidSpacing.inline))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            entries.forEach {
                Text(
                    text = it.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.height(LiquidSpacing.inline))
        ChartLegend(
            items = listOf(
                LegendItem("Pemasukan", incomeColor),
                LegendItem("Pengeluaran", expenseColor),
            ),
        )
    }
}

data class DonutSlice(
    val label: String,
    val value: Long,
    val color: Color,
)

/** Diagram donat untuk komposisi pengeluaran per kategori. */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    centerTitle: String = "",
    centerSubtitle: String = "",
) {
    val total = slices.sumOf { it.value }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(140.dp)) {
                if (total <= 0L) {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 22.dp.toPx()),
                    )
                } else {
                    val stroke = 22.dp.toPx()
                    var start = -90f
                    slices.forEach { slice ->
                        val sweep = slice.value.toFloat() / total.toFloat() * 360f
                        drawArc(
                            color = slice.color,
                            startAngle = start,
                            sweepAngle = (sweep - 2f).coerceAtLeast(0.5f),
                            useCenter = false,
                            style = Stroke(width = stroke),
                        )
                        start += sweep
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = centerTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = centerSubtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(LiquidSpacing.item))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LiquidSpacing.inline),
        ) {
            slices.take(5).forEach { slice ->
                ChartLegend(items = listOf(LegendItem(slice.label, slice.color)))
            }
        }
    }
}

data class LegendItem(val label: String, val color: Color)

@Composable
fun ChartLegend(items: List<LegendItem>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.item),
    ) {
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(item.color),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
