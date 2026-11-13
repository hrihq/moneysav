package com.moneysav.app.ui.features.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moneysav.app.domain.FinanceCalculations
import com.moneysav.app.domain.TransactionType
import com.moneysav.app.ui.components.ChartLegend
import com.moneysav.app.ui.components.DonutChart
import com.moneysav.app.ui.components.IncomeExpenseBarChart
import com.moneysav.app.ui.components.LiquidCard
import com.moneysav.app.ui.components.LiquidEmptyState
import com.moneysav.app.ui.components.LiquidListItem
import com.moneysav.app.ui.components.MoneySavIcons
import com.moneysav.app.ui.components.SectionTitle
import com.moneysav.app.ui.features.transactions.FilterChip
import com.moneysav.app.ui.components.staggeredEntry
import com.moneysav.app.ui.theme.LiquidSpacing
import com.moneysav.app.util.Format
import kotlin.math.abs

@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onPeriodChange: (StatPeriod) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = LiquidSpacing.page,
            end = LiquidSpacing.page,
            top = LiquidSpacing.inline,
            bottom = 140.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(LiquidSpacing.item),
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline)) {
                items(StatPeriod.entries, key = { it.name }) { period ->
                    FilterChip(
                        text = period.label,
                        selected = state.period == period,
                        onClick = { onPeriodChange(period) },
                    )
                }
            }
        }

        item {
            LiquidCard {
                Text(
                    text = state.rangeLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(LiquidSpacing.tight))
                Row(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.item)) {
                    Summary(
                        title = "Pemasukan",
                        value = Format.currency(state.income),
                        color = Color(0xFF356A35),
                        change = state.incomeChange,
                        modifier = Modifier.weight(1f),
                    )
                    Summary(
                        title = "Pengeluaran",
                        value = Format.currency(state.expense),
                        color = Color(0xFFC62828),
                        change = state.expenseChange,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(LiquidSpacing.tight))
                Text(
                    text = "Selisih ${Format.currency(state.income - state.expense)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        item {
            LiquidCard {
                SectionTitle("Pemasukan vs Pengeluaran")
                IncomeExpenseBarChart(entries = state.bars)
            }
        }

        item {
            LiquidCard {
                SectionTitle("Pengeluaran per Kategori")
                if (state.donut.isEmpty()) {
                    LiquidEmptyState(text = "Belum ada pengeluaran pada periode ini")
                } else {
                    DonutChart(
                        slices = state.donut,
                        centerTitle = Format.currency(state.expense),
                        centerSubtitle = "Total Keluar",
                    )
                }
            }
        }

        item {
            LiquidCard {
                SectionTitle("Kategori Terbesar")
                if (state.topCategories.isEmpty()) {
                    LiquidEmptyState(text = "Belum ada data")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(LiquidSpacing.inline)) {
                        val max = state.topCategories.maxOf { it.second }.coerceAtLeast(1L)
                        state.topCategories.forEachIndexed { index, (category, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().staggeredEntry(index),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.leading),
                            ) {
                                Icon(
                                    imageVector = MoneySavIcons.forKey(category.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color(category.colorArgb),
                                )
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = Format.currency(value),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Summary(
    title: String,
    value: String,
    color: Color,
    change: Double?,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (change != null) {
            val up = change >= 0
            Text(
                text = "${if (up) "+" else ""}${String.format(java.util.Locale.US, "%.1f", change)}%",
                style = MaterialTheme.typography.labelMedium,
                color = if (up) Color(0xFF356A35) else Color(0xFFC62828),
            )
        }
    }
}
