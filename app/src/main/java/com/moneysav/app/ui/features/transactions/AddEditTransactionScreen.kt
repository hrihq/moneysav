package com.moneysav.app.ui.features.transactions

import android.app.DatePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.moneysav.app.domain.Category
import com.moneysav.app.domain.PaymentMethods
import com.moneysav.app.domain.TransactionType
import com.moneysav.app.ui.components.AmountVisualTransformation
import com.moneysav.app.ui.theme.LiquidRadii
import com.moneysav.app.ui.theme.LiquidSpacing
import com.moneysav.app.util.Format
import java.util.Calendar

@Composable
fun AddEditTransactionScreen(
    viewModel: AddEditTransactionViewModel,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    val categories by viewModel.categories.collectAsState()
    val targets by viewModel.targets.collectAsState()
    val initial by viewModel.initial.collectAsState()

    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var note by remember { mutableStateOf("") }
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var paymentMethod by remember { mutableStateOf(PaymentMethods.all.first()) }
    var targetId by remember { mutableStateOf<Long?>(null) }
    var restored by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Muat data transaksi yang sedang diedit (sekali).
    LaunchedEffect(initial) {
        val t = initial ?: return@LaunchedEffect
        if (!restored) {
            type = t.type
            amountText = t.amount.toString()
            categoryId = t.categoryId
            note = t.note
            dateMillis = t.date
            paymentMethod = t.paymentMethod
            targetId = t.savingsTargetId
            restored = true
        }
    }

    // Kategori awal untuk transaksi baru.
    LaunchedEffect(categories, type) {
        if (categoryId == null) {
            categoryId = categories.firstOrNull { it.type == type }?.id
        }
    }

    val filteredCategories = categories.filter { it.type == type }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
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
            IconButton(onClick = onDismiss) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
            }
            Text(
                text = if (initial == null) "Tambah Transaksi" else "Ubah Transaksi",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            if (initial != null) {
                IconButton(onClick = { viewModel.delete(onDismiss) }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline)) {
            TypeChip("Pemasukan", type == TransactionType.INCOME) { type = TransactionType.INCOME }
            TypeChip("Pengeluaran", type == TransactionType.EXPENSE) { type = TransactionType.EXPENSE }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { value -> amountText = value.filter { it.isDigit() } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Jumlah (Rp)") },
            visualTransformation = AmountVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        Column {
            Text(
                text = "Kategori",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(LiquidSpacing.tight))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline)) {
                items(filteredCategories) { category: Category ->
                    CategoryChip(
                        category = category,
                        selected = categoryId == category.id,
                        onClick = { categoryId = category.id },
                    )
                }
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Catatan") },
            placeholder = { Text("Opsional") },
        )

        // Field tanggal: kartu yang bisa diklik membuka DatePicker bawaan Android.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LiquidRadii.small))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = dateMillis
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val c = Calendar.getInstance()
                            c.set(year, month, day, 12, 0, 0)
                            dateMillis = c.timeInMillis
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH),
                    ).show()
                }
                .padding(horizontal = LiquidSpacing.card, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline),
        ) {
            Icon(
                Icons.Rounded.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = Format.date(dateMillis),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Column {
            Text(
                text = "Metode Pembayaran",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(LiquidSpacing.tight))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline)) {
                items(PaymentMethods.all) { method ->
                    FilterChip(
                        text = method,
                        selected = paymentMethod == method,
                        onClick = { paymentMethod = method },
                    )
                }
            }
        }

        if (type == TransactionType.INCOME && targets.isNotEmpty()) {
            Column {
                Text(
                    text = "Alokasi Tabungan",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(LiquidSpacing.tight))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.inline)) {
                    item {
                        FilterChip(
                            text = "Tanpa Target",
                            selected = targetId == null,
                            onClick = { targetId = null },
                        )
                    }
                    items(targets) { target ->
                        FilterChip(
                            text = target.name,
                            selected = targetId == target.id,
                            onClick = { targetId = target.id },
                        )
                    }
                }
            }
        }

        if (error != null) {
            Text(
                text = error ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Button(
            onClick = {
                val amount = amountText.toLongOrNull() ?: 0L
                val catId = categoryId
                when {
                    amount <= 0L -> error = "Jumlah harus lebih dari nol"
                    catId == null -> error = "Pilih kategori terlebih dahulu"
                    else -> {
                        error = null
                        viewModel.save(
                            type = type,
                            amount = amount,
                            categoryId = catId,
                            note = note.trim(),
                            date = dateMillis,
                            paymentMethod = paymentMethod,
                            savingsTargetId = if (type == TransactionType.INCOME) targetId else null,
                            onDone = onDismiss,
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (initial == null) "Simpan Transaksi" else "Perbarui Transaksi")
        }

        if (initial != null) {
            TextButton(onClick = { viewModel.delete(onDismiss) }, modifier = Modifier.fillMaxWidth()) {
                Text("Hapus Transaksi", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun TypeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = content,
        modifier = Modifier
            .clip(RoundedCornerShape(LiquidRadii.small))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    )
}

@Composable
private fun CategoryChip(category: Category, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(LiquidRadii.small))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.tight),
    ) {
        Icon(
            imageVector = com.moneysav.app.ui.components.MoneySavIcons.forKey(category.icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (selected) content else androidx.compose.ui.graphics.Color(category.colorArgb),
        )
        Text(text = category.name, style = MaterialTheme.typography.labelLarge, color = content)
    }
}
