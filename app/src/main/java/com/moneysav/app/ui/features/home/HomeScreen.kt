package com.moneysav.app.ui.features.home

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneysav.app.domain.FinanceCalculations
import com.moneysav.app.ui.components.AnimatedCurrency
import com.moneysav.app.ui.components.IncomeExpenseBarChart
import com.moneysav.app.ui.components.LiquidCard
import com.moneysav.app.ui.components.LiquidEmptyState
import com.moneysav.app.ui.components.LiquidListItem
import com.moneysav.app.ui.components.LiquidProgressBar
import com.moneysav.app.ui.components.MoneySavIcons
import com.moneysav.app.ui.components.SectionTitle
import com.moneysav.app.ui.theme.LiquidSpacing
import com.moneysav.app.util.Format

@Composable
fun HomeScreen(
    state: HomeUiState,
    onAddTransaction: () -> Unit,
    onOpenTransaction: (Long) -> Unit,
    onOpenAllTransactions: () -> Unit,
    onOpenBudget: () -> Unit,
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
            LiquidCard(onClick = onOpenBudget) {
                Text(
                    text = "Saldo Total",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AnimatedCurrency(
                    amount = state.balance,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (state.balance >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                )
                if (state.budgetTotal > 0) {
                    Text(
                        text = "Sisa anggaran ${Format.currency(state.budgetRemaining)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.item)) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Pemasukan",
                    amount = state.incomeMonth,
                    color = Color(0xFF356A35),
                    icon = MoneySavIcons.income,
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Pengeluaran",
                    amount = state.expenseMonth,
                    color = Color(0xFFC62828),
                    icon = MoneySavIcons.expense,
                )
            }
        }

        item {
            LiquidCard(onClick = onAddTransaction) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.leading),
                ) {
                    Icon(
                        imageVector = MoneySavIcons.income,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Tambah Transaksi",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Catat pemasukan atau pengeluaran",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item {
            LiquidCard {
                SectionTitle("Arus Kas 6 Bulan")
                IncomeExpenseBarChart(entries = state.monthBars)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Transaksi Terbaru",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Lihat Semua",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable(onClick = onOpenAllTransactions)
                        .padding(8.dp),
                )
            }
        }

        if (state.recent.isEmpty()) {
            item {
                LiquidCard {
                    LiquidEmptyState(
                        text = "Belum ada transaksi",
                        hint = "Tekan tombol Tambah Transaksi untuk mulai mencatat",
                    )
                }
            }
        } else {
            item {
                LiquidCard(contentPadding = 0.dp) {
                    Column {
                        state.recent.forEachIndexed { index, item ->
                            LiquidListItem(
                                modifier = Modifier.staggeredEntry(index),
                                title = item.category?.name ?: "Lainnya",
                                subtitle = listOfNotNull(
                                    Format.date(item.transaction.date),
                                    item.transaction.note.takeIf { it.isNotBlank() },
                                ).joinToString(" • "),
                                leading = MoneySavIcons.forKey(item.category?.icon ?: "category"),
                                leadingTint = Color(item.category?.colorArgb ?: 0xFF90A4AE),
                                trailing = {
                                    Text(
                                        text = Format.currencySigned(
                                            item.transaction.amount,
                                            item.transaction.type == com.moneysav.app.domain.TransactionType.INCOME,
                                        ),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (item.transaction.type == com.moneysav.app.domain.TransactionType.INCOME) {
                                            Color(0xFF356A35)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                    )
                                },
                                onClick = { onOpenTransaction(item.transaction.id) },
                                showDivider = index != state.recent.lastIndex,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Long,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    LiquidCard(modifier = modifier, onClick = null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline),
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = color)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AnimatedCurrency(
                    amount = amount,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                )
            }
        }
    }
}
