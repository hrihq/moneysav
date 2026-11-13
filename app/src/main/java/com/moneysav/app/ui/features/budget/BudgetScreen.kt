package com.moneysav.app.ui.features.budget

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moneysav.app.domain.FinanceCalculations
import com.moneysav.app.domain.TransactionType
import com.moneysav.app.ui.components.AmountVisualTransformation
import com.moneysav.app.ui.components.LiquidCard
import com.moneysav.app.ui.components.LiquidProgressBar
import com.moneysav.app.ui.components.MoneySavIcons
import com.moneysav.app.ui.components.staggeredEntry
import com.moneysav.app.ui.components.SectionTitle
import com.moneysav.app.ui.components.StatusBanner
import com.moneysav.app.ui.features.transactions.FilterChip
import com.moneysav.app.ui.theme.LiquidSpacing
import com.moneysav.app.util.Format
import com.moneysav.app.util.Notifications

@Composable
fun BudgetScreen(
    state: BudgetUiState,
    onMonthChange: (String) -> Unit,
    onAddBudget: (Long, Long) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    onAddTarget: (String, Long) -> Unit,
    onDeleteTarget: (Long) -> Unit,
) {
    val context = LocalContext.current
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }

    // Beri notifikasi sekali untuk setiap peringatan baru.
    var lastWarned by remember { mutableStateOf(setOf<String>()) }
    LaunchedEffect(state.warnings) {
        val newWarnings = state.warnings.filter { it !in lastWarned }
        if (newWarnings.isNotEmpty()) {
            lastWarned = state.warnings.toSet()
            if (Notifications.canNotify(context)) {
                Notifications.notifyBudgetWarning(
                    context,
                    title = "Anggaran ${FinanceCalculations.monthLabel(state.month)}",
                    message = newWarnings.joinToString("\n"),
                )
            }
        }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onMonthChange(FinanceCalculations.previousMonth(state.month)) }) {
                    Icon(Icons.Rounded.ChevronLeft, contentDescription = "Bulan sebelumnya")
                }
                Text(
                    text = FinanceCalculations.monthLabel(state.month),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                IconButton(onClick = { onMonthChange(FinanceCalculations.addMonths(state.month, 1)) }) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = "Bulan berikutnya")
                }
            }
        }

        item {
            LiquidCard {
                Text(
                    text = "Total Anggaran",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = Format.currency(state.totalBudget),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(LiquidSpacing.tight))
                Text(
                    text = "Terpakai ${Format.currency(state.totalSpent)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.totalBudget > 0) {
                    Spacer(Modifier.height(LiquidSpacing.inline))
                    LiquidProgressBar(
                        progress = state.totalSpent.toFloat() / state.totalBudget.toFloat(),
                        color = if (state.totalSpent > state.totalBudget) MaterialTheme.colorScheme.error else Color.Unspecified,
                    )
                }
            }
        }

        state.warnings.forEach { warning ->
            item {
                StatusBanner(
                    text = warning,
                    icon = Icons.Rounded.Flag,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        item {
            LiquidCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Anggaran per Kategori",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showBudgetDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Tambah anggaran")
                    }
                }
                if (state.budgets.isEmpty()) {
                    Text(
                        text = "Belum ada anggaran bulan ini. Tekan + untuk menambah.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(LiquidSpacing.item)) {
                        state.budgets.forEachIndexed { index, item ->
                            Column(modifier = Modifier.staggeredEntry(index)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = item.category?.name ?: "Semua Kategori",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = "${Format.currency(item.spent)} / ${Format.currency(item.budget.amount)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Text(
                                        text = Format.currency(item.remaining),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (item.remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                    )
                                    IconButton(onClick = { onDeleteBudget(item.budget.id) }) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = "Hapus anggaran",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                LiquidProgressBar(
                                    progress = item.progress,
                                    color = if (item.progress > 1f) MaterialTheme.colorScheme.error else Color.Unspecified,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            LiquidCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Target Tabungan",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showTargetDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Tambah target")
                    }
                }
                if (state.targets.isEmpty()) {
                    Text(
                        text = "Belum ada target. Tekan + untuk menambah.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(LiquidSpacing.item)) {
                        state.targets.forEachIndexed { index, item ->
                            Column(modifier = Modifier.staggeredEntry(index)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Rounded.Savings,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(item.target.colorArgb),
                                    )
                                    Column(
                                        Modifier
                                            .weight(1f)
                                            .padding(horizontal = LiquidSpacing.leading),
                                    ) {
                                        Text(
                                            text = item.target.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = "${Format.currency(item.saved)} / ${Format.currency(item.target.targetAmount)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    IconButton(onClick = { onDeleteTarget(item.target.id) }) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = "Hapus target",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                LiquidProgressBar(
                                    progress = item.progress,
                                    color = Color(item.target.colorArgb),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBudgetDialog) {
        AddBudgetDialog(
            categories = state.categories.filter { it.type == TransactionType.EXPENSE },
            onDismiss = { showBudgetDialog = false },
            onConfirm = { categoryId, amount ->
                onAddBudget(categoryId, amount)
                showBudgetDialog = false
            },
        )
    }

    if (showTargetDialog) {
        AddTargetDialog(
            onDismiss = { showTargetDialog = false },
            onConfirm = { name, amount ->
                onAddTarget(name, amount)
                showTargetDialog = false
            },
        )
    }
}

@Composable
private fun AddBudgetDialog(
    categories: List<com.moneysav.app.domain.Category>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit,
) {
    var amountText by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf(categories.firstOrNull()?.id) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Anggaran") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LiquidSpacing.item)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline)) {
                    items(categories) { category ->
                        FilterChip(
                            text = category.name,
                            selected = categoryId == category.id,
                            onClick = { categoryId = category.id },
                        )
                    }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Jumlah anggaran (Rp)") },
                    visualTransformation = AmountVisualTransformation(),
                )
                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.toLongOrNull() ?: 0L
                val catId = categoryId
                when {
                    amount <= 0L -> error = "Jumlah harus lebih dari nol"
                    catId == null -> error = "Pilih kategori"
                    else -> onConfirm(catId, amount)
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}

@Composable
private fun AddTargetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Target Tabungan Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LiquidSpacing.item)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Nama target") },
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Target (Rp)") },
                    visualTransformation = AmountVisualTransformation(),
                )
                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.toLongOrNull() ?: 0L
                when {
                    name.isBlank() -> error = "Nama target wajib diisi"
                    amount <= 0L -> error = "Target harus lebih dari nol"
                    else -> onConfirm(name.trim(), amount)
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
