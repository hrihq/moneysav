package com.moneysav.app.ui.features.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneysav.app.domain.TransactionType
import com.moneysav.app.ui.components.AnimatedCurrency
import com.moneysav.app.ui.components.IncomeExpenseBarChart
import com.moneysav.app.ui.components.LiquidCard
import com.moneysav.app.ui.components.LiquidEmptyState
import com.moneysav.app.ui.components.LiquidListItem
import com.moneysav.app.ui.components.MoneySavIcons
import com.moneysav.app.ui.components.SectionHeader
import com.moneysav.app.ui.components.staggeredEntry
import com.moneysav.app.ui.theme.LiquidRadii
import com.moneysav.app.ui.theme.LiquidSpacing
import com.moneysav.app.ui.theme.LocalMoneyColors
import com.moneysav.app.util.Format

@Composable
fun HomeScreen(
    state: HomeUiState,
    onAddTransaction: () -> Unit,
    onOpenTransaction: (Long) -> Unit,
    onOpenAllTransactions: () -> Unit,
    onOpenBudget: () -> Unit,
) {
    val money = LocalMoneyColors.current

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
            HeroBalanceCard(
                balance = state.balance,
                income = state.incomeMonth,
                expense = state.expenseMonth,
                budgetRemaining = state.budgetRemaining,
                budgetTotal = state.budgetTotal,
                onClick = onOpenBudget,
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.item)) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Pemasukan",
                    amount = state.incomeMonth,
                    color = money.income,
                    icon = MoneySavIcons.income,
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Pengeluaran",
                    amount = state.expenseMonth,
                    color = money.expense,
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
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = MoneySavIcons.income,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
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
                SectionHeader(icon = MoneySavIcons.statistics, title = "Arus Kas 6 Bulan")
                IncomeExpenseBarChart(
                    entries = state.monthBars,
                    incomeColor = money.income,
                    expenseColor = money.expense,
                )
            }
        }

        item {
            SectionHeader(
                icon = MoneySavIcons.transactions,
                title = "Transaksi Terbaru",
                action = {
                    Text(
                        text = "Lihat Semua",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable(onClick = onOpenAllTransactions)
                            .padding(8.dp),
                    )
                },
            )
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
                                            item.transaction.type == TransactionType.INCOME,
                                        ),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (item.transaction.type == TransactionType.INCOME) {
                                            money.income
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

/** Kartu hero saldo: gradasi + bentuk dekoratif, jadi titik fokus Beranda. */
@Composable
private fun HeroBalanceCard(
    balance: Long,
    income: Long,
    expense: Long,
    budgetRemaining: Long,
    budgetTotal: Long,
    onClick: () -> Unit,
) {
    val money = LocalMoneyColors.current
    val start = MaterialTheme.colorScheme.primaryContainer
    val end = MaterialTheme.colorScheme.secondaryContainer
    val content = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LiquidRadii.large))
            .background(Brush.linearGradient(listOf(start, end)))
            .clickable(onClick = onClick),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
        ) {
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = size.width * 0.22f,
                center = Offset(size.width * 0.92f, -size.height * 0.25f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = size.width * 0.15f,
                center = Offset(size.width * 0.06f, size.height * 1.15f),
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.06f),
                radius = size.width * 0.30f,
                center = Offset(size.width * 0.55f, size.height * 1.25f),
            )
        }
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Saldo Total",
                style = MaterialTheme.typography.labelLarge,
                color = content.copy(alpha = 0.85f),
            )
            AnimatedCurrency(
                amount = balance,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = content,
            )
            if (budgetTotal > 0) {
                Text(
                    text = "Sisa anggaran ${Format.currency(budgetRemaining)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = content.copy(alpha = 0.8f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline)) {
                HeroPill(label = "Masuk", value = Format.currency(income), color = money.income)
                HeroPill(label = "Keluar", value = Format.currency(expense), color = money.expense)
            }
        }
    }
}

@Composable
private fun HeroPill(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.92f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Long,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    LiquidCard(modifier = modifier, onClick = null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = color)
            }
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
