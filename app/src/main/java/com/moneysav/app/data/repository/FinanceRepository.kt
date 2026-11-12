package com.moneysav.app.data.repository

import com.moneysav.app.data.db.AppDatabase
import com.moneysav.app.data.db.BudgetEntity
import com.moneysav.app.data.db.DefaultCategories
import com.moneysav.app.data.db.SavingsTargetEntity
import com.moneysav.app.data.db.TransactionEntity
import com.moneysav.app.domain.Budget
import com.moneysav.app.domain.Category
import com.moneysav.app.domain.SavingsTarget
import com.moneysav.app.domain.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Satu sumber kebenaran untuk data keuangan.
 *
 * Semua query bersifat reaktif (Flow) sehingga saldo, grafik, dan progress anggaran
 * otomatis ikut berubah setiap transaksi ditambah / diubah / dihapus.
 */
class FinanceRepository(private val db: AppDatabase) {

    val categories: Flow<List<Category>> =
        db.categoryDao().observeAll().map { list -> list.map { it.toDomain() } }

    val transactions: Flow<List<Transaction>> =
        db.transactionDao().observeAll().map { list -> list.map { it.toDomain() } }

    val budgets: Flow<List<Budget>> =
        db.budgetDao().observeAll().map { list -> list.map { it.toDomain() } }

    val savingsTargets: Flow<List<SavingsTarget>> =
        db.savingsTargetDao().observeAll().map { list -> list.map { it.toDomain() } }

    /** Isi kategori bawaan sekali saja. */
    suspend fun ensureDefaultCategories() {
        if (db.categoryDao().count() == 0) {
            db.categoryDao().insertAll(DefaultCategories.entities())
        }
    }

    suspend fun categoryById(id: Long): Category? = db.categoryDao().getById(id)?.toDomain()

    suspend fun transactionById(id: Long): Transaction? = db.transactionDao().getById(id)?.toDomain()

    suspend fun addTransaction(transaction: Transaction): Long =
        db.transactionDao().insert(TransactionEntity.fromDomain(transaction))

    suspend fun updateTransaction(transaction: Transaction) =
        db.transactionDao().update(TransactionEntity.fromDomain(transaction))

    suspend fun deleteTransaction(id: Long) = db.transactionDao().deleteById(id)

    suspend fun addBudget(budget: Budget): Long =
        db.budgetDao().insert(BudgetEntity.fromDomain(budget))

    suspend fun updateBudget(budget: Budget) =
        db.budgetDao().update(BudgetEntity.fromDomain(budget))

    suspend fun deleteBudget(id: Long) = db.budgetDao().deleteById(id)

    suspend fun addSavingsTarget(target: SavingsTarget): Long =
        db.savingsTargetDao().insert(SavingsTargetEntity.fromDomain(target))

    suspend fun updateSavingsTarget(target: SavingsTarget) =
        db.savingsTargetDao().update(SavingsTargetEntity.fromDomain(target))

    suspend fun deleteSavingsTarget(id: Long) = db.savingsTargetDao().deleteById(id)
}
