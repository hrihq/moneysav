package com.moneysav.app.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneysav.app.data.repository.FinanceRepository
import com.moneysav.app.domain.Budget
import com.moneysav.app.domain.Category
import com.moneysav.app.domain.FinanceCalculations
import com.moneysav.app.domain.Transaction
import com.moneysav.app.ui.TransactionWithCategory
import com.moneysav.app.ui.components.BarEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val month: String = FinanceCalculations.currentMonth(),
    val balance: Long = 0,
    val incomeMonth: Long = 0,
    val expenseMonth: Long = 0,
    val budgetTotal: Long = 0,
    val budgetSpent: Long = 0,
    val budgetRemaining: Long = 0,
    val recent: List<TransactionWithCategory> = emptyList(),
    val monthBars: List<BarEntry> = emptyList(),
    val categories: List<Category> = emptyList(),
)

class HomeViewModel(private val repo: FinanceRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repo.transactions,
        repo.categories,
        repo.budgets,
    ) { transactions, categories, budgets ->
        buildState(transactions, categories, budgets)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    private fun buildState(
        transactions: List<Transaction>,
        categories: List<Category>,
        budgets: List<Budget>,
    ): HomeUiState {
        val month = FinanceCalculations.currentMonth()
        val (start, end) = FinanceCalculations.monthRange(month)

        val income = FinanceCalculations.totalIncome(transactions, start, end)
        val expense = FinanceCalculations.totalExpense(transactions, start, end)

        val monthBudgets = budgets.filter { it.month == month }
        val totalBudget = monthBudgets
            .filter { it.categoryId == -1L }
            .ifEmpty { monthBudgets.filter { it.categoryId != -1L } }
            .sumOf { it.amount }

        val bars = (5 downTo 0).map { delta ->
            val key = FinanceCalculations.addMonths(month, -delta)
            val (s, e) = FinanceCalculations.monthRange(key)
            BarEntry(
                label = FinanceCalculations.monthShort(key),
                income = FinanceCalculations.totalIncome(transactions, s, e),
                expense = FinanceCalculations.totalExpense(transactions, s, e),
            )
        }

        val byCategory = categories.associateBy { it.id }
        val recent = transactions.take(5).map { TransactionWithCategory(it, byCategory[it.categoryId]) }

        return HomeUiState(
            month = month,
            balance = FinanceCalculations.balance(transactions),
            incomeMonth = income,
            expenseMonth = expense,
            budgetTotal = totalBudget,
            budgetSpent = expense,
            budgetRemaining = totalBudget - expense,
            recent = recent,
            monthBars = bars,
            categories = categories,
        )
    }
}
