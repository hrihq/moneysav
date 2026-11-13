package com.moneysav.app.ui.features.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moneysav.app.domain.FinanceCalculations
import com.moneysav.app.domain.TransactionType
import com.moneysav.app.ui.components.LiquidCard
import com.moneysav.app.ui.components.LiquidEmptyState
import com.moneysav.app.ui.components.LiquidListItem
import com.moneysav.app.ui.components.MoneySavIcons
import com.moneysav.app.ui.theme.LiquidRadii
import com.moneysav.app.ui.theme.LiquidSpacing
import com.moneysav.app.util.Format

@Composable
fun TransactionsScreen(
    state: TransactionsUiState,
    onQueryChange: (String) -> Unit,
    onTypeChange: (String?) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onMonthChange: (String?) -> Unit,
    onOpenTransaction: (Long) -> Unit,
) {
    val months = remember {
        val base = state.filters.month ?: FinanceCalculations.currentMonth()
        (5 downTo 0).map { FinanceCalculations.addMonths(base, -it) }
    }

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
            OutlinedTextField(
                value = state.filters.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Cari transaksi") },
                leadingIcon = { Icon(MoneySavIcons.transactions, contentDescription = null) },
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline)) {
                item {
                    FilterChip(
                        text = "Semua",
                        selected = state.filters.type == null,
                        onClick = { onTypeChange(null) },
                    )
                }
                item {
                    FilterChip(
                        text = "Pemasukan",
                        selected = state.filters.type == TransactionType.INCOME.name,
                        onClick = { onTypeChange(TransactionType.INCOME.name) },
                    )
                }
                item {
                    FilterChip(
                        text = "Pengeluaran",
                        selected = state.filters.type == TransactionType.EXPENSE.name,
                        onClick = { onTypeChange(TransactionType.EXPENSE.name) },
                    )
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline)) {
                item {
                    FilterChip(
                        text = "Semua Kategori",
                        selected = state.filters.categoryId == null,
                        onClick = { onCategoryChange(null) },
                    )
                }
                items(state.categories, key = { it.id }) { category ->
                    FilterChip(
                        text = category.name,
                        selected = state.filters.categoryId == category.id,
                        onClick = { onCategoryChange(category.id) },
                    )
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline)) {
                item {
                    FilterChip(
                        text = "Semua Bulan",
                        selected = state.filters.month == null,
                        onClick = { onMonthChange(null) },
                    )
                }
                items(months, key = { it }) { month ->
                    FilterChip(
                        text = FinanceCalculations.monthLabel(month),
                        selected = state.filters.month == month,
                        onClick = { onMonthChange(month) },
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.item)) {
                Text(
                    text = "Masuk ${Format.currency(state.totalIncome)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF356A35),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Keluar ${Format.currency(state.totalExpense)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFC62828),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (state.items.isEmpty()) {
            item {
                LiquidCard {
                    LiquidEmptyState(
                        text = "Tidak ada transaksi",
                        hint = "Ubah filter atau tambah transaksi baru",
                    )
                }
            }
        } else {
            item {
                LiquidCard(contentPadding = 0.dp) {
                    Column {
                        state.items.forEachIndexed { index, item ->
                            LiquidListItem(
                                modifier = Modifier.staggeredEntry(index),
                                title = item.category?.name ?: "Lainnya",
                                subtitle = listOfNotNull(
                                    Format.dateTime(item.transaction.date),
                                    item.transaction.paymentMethod,
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
                                            Color(0xFF356A35)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                onClick = { onOpenTransaction(item.transaction.id) },
                                showDivider = index != state.items.lastIndex,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val content = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = content,
        modifier = Modifier
            .clip(RoundedCornerShape(LiquidRadii.small))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}
