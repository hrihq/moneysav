package com.moneysav.app.ui

import com.moneysav.app.domain.Budget
import com.moneysav.app.domain.Category
import com.moneysav.app.domain.SavingsTarget
import com.moneysav.app.domain.Transaction

/** Transaksi beserta kategori terkait (untuk list & grafik). */
data class TransactionWithCategory(
    val transaction: Transaction,
    val category: Category?,
)

/** Anggaran + angka pemakaiannya. */
data class BudgetWithProgress(
    val budget: Budget,
    val category: Category?,
    val spent: Long,
    val remaining: Long,
    val progress: Float,
)

/** Target tabungan + progress tersimpan. */
data class TargetWithProgress(
    val target: SavingsTarget,
    val saved: Long,
    val progress: Float,
)
