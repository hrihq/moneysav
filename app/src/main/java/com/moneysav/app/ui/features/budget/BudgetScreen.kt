package com.moneysav.app.ui.features.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moneysav.app.domain.Category
import com.moneysav.app.domain.FinanceCalculations
import com.moneysav.app.domain.TransactionType
import com.moneysav.app.ui.BudgetWithProgress
import com.moneysav.app.ui.TargetWithProgress
import com.moneysav.app.ui.components.AmountVisualTransformation
import com.moneysav.app.ui.components.LiquidCard
import com.moneysav.app.ui.components.LiquidEmptyState
import com.moneysav.app.ui.components.LiquidProgressBar
import com.moneysav.app.ui.components.MoneySavIcons
import com.moneysav.app.ui.components.ProgressRing
import com.moneysav.app.ui.components.SectionHeader
import com.moneysav.app.ui.components.StatusBanner
import com.moneysav.app.ui.components.staggeredEntry
import com.moneysav.app.ui.features.transactions.FilterChip
import com.moneysav.app.ui.theme.LiquidRadii
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
        item { MonthSelector(month = state.month, onMonthChange = onMonthChange) }

        item {
            BudgetSummaryCard(
                total = state.totalBudget,
                spent = state.totalSpent,
                month = state.month,
            )
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
                SectionHeader(
                    icon = MoneySavIcons.budget,
                    title = "Anggaran per Kategori",
                    action = {
                        IconButton(onClick = { showBudgetDialog = true }) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = "Tambah anggaran",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
                if (state.budgets.isEmpty()) {
                    LiquidEmptyState(
                        text = "Belum ada anggaran bulan ini",
                        hint = "Tekan + untuk menambah anggaran per kategori",
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(LiquidSpacing.item)) {
                        state.budgets.forEachIndexed { index, item ->
                            BudgetRow(item = item, index = index, onDelete = onDeleteBudget)
                        }
                    }
                }
            }
        }

        item {
            LiquidCard {
                SectionHeader(
                    icon = Icons.Rounded.Savings,
                    title = "Target Tabungan",
                    action = {
                        IconButton(onClick = { showTargetDialog = true }) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = "Tambah target",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
                if (state.targets.isEmpty()) {
                    LiquidEmptyState(
                        text = "Belum ada target tabungan",
                        hint = "Tekan + untuk menabung menuju satu tujuan",
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(LiquidSpacing.item)) {
                        state.targets.forEachIndexed { index, item ->
                            TargetRow(item = item, index = index, onDelete = onDeleteTarget)
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

/** Selektor bulan berbentuk pil dengan permukaan tonal. */
@Composable
private fun MonthSelector(month: String, onMonthChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onMonthChange(FinanceCalculations.previousMonth(month)) }) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Bulan sebelumnya")
        }
        Text(
            text = FinanceCalculations.monthLabel(month),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = { onMonthChange(FinanceCalculations.addMonths(month, 1)) }) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = "Bulan berikutnya")
        }
    }
}

/** Ringkasan penggunaan anggaran dengan ring progres. */
@Composable
private fun BudgetSummaryCard(total: Long, spent: Long, month: String) {
    val progress = if (total <= 0L) 0f else spent.toFloat() / total.toFloat()
    val over = total > 0L && spent > total
    val remaining = total - spent

    LiquidCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.leading),
        ) {
            ProgressRing(
                progress = progress,
                modifier = Modifier.size(96.dp),
                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "terpakai",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(LiquidSpacing.tight),
            ) {
                Text(
                    text = FinanceCalculations.monthLabel(month),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = Format.currency(total),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Terpakai ${Format.currency(spent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Sisa ${Format.currency(remaining)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun BudgetRow(item: BudgetWithProgress, index: Int, onDelete: (Long) -> Unit) {
    val color = item.category?.let { Color(it.colorArgb) } ?: MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier.staggeredEntry(index),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.leading),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MoneySavIcons.forKey(item.category?.icon ?: "category"),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = color,
                )
            }
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
            IconButton(onClick = { onDelete(item.budget.id) }) {
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
            color = if (item.progress > 1f) MaterialTheme.colorScheme.error else color,
        )
    }
}

@Composable
private fun TargetRow(item: TargetWithProgress, index: Int, onDelete: (Long) -> Unit) {
    val color = Color(item.target.colorArgb)
    Column(
        modifier = Modifier.staggeredEntry(index),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.leading),
        ) {
            ProgressRing(
                progress = item.progress,
                modifier = Modifier.size(44.dp),
                color = color,
                strokeWidth = 5.dp,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Savings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = color,
                )
            }
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
            IconButton(onClick = { onDelete(item.target.id) }) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Hapus target",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        LiquidProgressBar(progress = item.progress, color = color)
    }
}

@Composable
private fun AddBudgetDialog(
    categories: List<Category>,
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
                    items(categories, key = { it.id }) { category ->
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
