package com.moneysav.app.ui.features.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneysav.app.data.repository.FinanceRepository
import com.moneysav.app.domain.Category
import com.moneysav.app.domain.FinanceCalculations
import com.moneysav.app.domain.Transaction
import com.moneysav.app.ui.TransactionWithCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Null = semua (tidak difilter). */
data class TransactionFilters(
    val query: String = "",
    val type: String? = null,
    val categoryId: Long? = null,
    val month: String? = null,
)

data class TransactionsUiState(
    val filters: TransactionFilters = TransactionFilters(),
    val items: List<TransactionWithCategory> = emptyList(),
    val categories: List<Category> = emptyList(),
    val totalIncome: Long = 0,
    val totalExpense: Long = 0,
)

class TransactionsViewModel(private val repo: FinanceRepository) : ViewModel() {

    private val filters = MutableStateFlow(TransactionFilters())

    val uiState: StateFlow<TransactionsUiState> = combine(
        repo.transactions,
        repo.categories,
        filters,
    ) { transactions, categories, f ->
        val byCategory = categories.associateBy { it.id }
        val items = transactions
            .asSequence()
            .filter { t ->
                val matchQuery = f.query.isBlank() ||
                    t.note.contains(f.query, ignoreCase = true) ||
                    (byCategory[t.categoryId]?.name?.contains(f.query, ignoreCase = true) == true)
                val matchType = f.type == null || t.type.name == f.type
                val matchCategory = f.categoryId == null || t.categoryId == f.categoryId
                val matchMonth = f.month == null || com.moneysav.app.util.Format.monthKey(t.date) == f.month
                matchQuery && matchType && matchCategory && matchMonth
            }
            .map { TransactionWithCategory(it, byCategory[it.categoryId]) }
            .toList()

        val (start, end) = f.month?.let { FinanceCalculations.monthRange(it) }
            ?: Long.MIN_VALUE to Long.MAX_VALUE

        TransactionsUiState(
            filters = f,
            items = items,
            categories = categories,
            totalIncome = FinanceCalculations.totalIncome(transactions, start, end),
            totalExpense = FinanceCalculations.totalExpense(transactions, start, end),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionsUiState())

    fun setQuery(query: String) = filters.update { it.copy(query = query) }
    fun setType(type: String?) = filters.update { it.copy(type = type) }
    fun setCategory(categoryId: Long?) = filters.update { it.copy(categoryId = categoryId) }
    fun setMonth(month: String?) = filters.update { it.copy(month = month) }

    fun delete(transaction: Transaction) {
        viewModelScope.launch { repo.deleteTransaction(transaction.id) }
    }
}
