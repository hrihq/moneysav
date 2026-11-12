package com.moneysav.app.ui.features.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneysav.app.data.repository.FinanceRepository
import com.moneysav.app.domain.Budget
import com.moneysav.app.domain.Category
import com.moneysav.app.domain.FinanceCalculations
import com.moneysav.app.domain.SavingsTarget
import com.moneysav.app.domain.Transaction
import com.moneysav.app.ui.BudgetWithProgress
import com.moneysav.app.ui.TargetWithProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BudgetUiState(
    val month: String = FinanceCalculations.currentMonth(),
    val budgets: List<BudgetWithProgress> = emptyList(),
    val targets: List<TargetWithProgress> = emptyList(),
    val categories: List<Category> = emptyList(),
    val totalBudget: Long = 0,
    val totalSpent: Long = 0,
    val warnings: List<String> = emptyList(),
)

class BudgetViewModel(private val repo: FinanceRepository) : ViewModel() {

    private val month = MutableStateFlow(FinanceCalculations.currentMonth())

    val uiState: StateFlow<BudgetUiState> = combine(
        repo.transactions,
        repo.categories,
        repo.budgets,
        repo.savingsTargets,
        month,
    ) { transactions, categories, budgets, targets, m ->
        buildState(transactions, categories, budgets, targets, m)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetUiState())

    fun setMonth(m: String) = month.update { m }

    fun addBudget(categoryId: Long, amount: Long) {
        viewModelScope.launch {
            repo.addBudget(
                Budget(
                    id = 0,
                    categoryId = categoryId,
                    month = month.value,
                    amount = amount,
                ),
            )
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch { repo.deleteBudget(id) }
    }

    fun addTarget(name: String, targetAmount: Long) {
        viewModelScope.launch {
            repo.addSavingsTarget(
                SavingsTarget(id = 0, name = name, targetAmount = targetAmount),
            )
        }
    }

    fun deleteTarget(id: Long) {
        viewModelScope.launch { repo.deleteSavingsTarget(id) }
    }

    private fun buildState(
        transactions: List<Transaction>,
        categories: List<Category>,
        budgets: List<Budget>,
        targets: List<SavingsTarget>,
        month: String,
    ): BudgetUiState {
        val (start, end) = FinanceCalculations.monthRange(month)
        val byCategory = FinanceCalculations.expensesByCategory(transactions, start, end)
        val totalSpent = byCategory.values.sum()
        val categoryById = categories.associateBy { it.id }

        val monthBudgets = budgets.filter { it.month == month }
        val budgetItems = monthBudgets.map { budget ->
            val spent = if (budget.categoryId == -1L) totalSpent else (byCategory[budget.categoryId] ?: 0L)
            val remaining = FinanceCalculations.budgetRemaining(budget.amount, spent)
            val progress = if (budget.amount <= 0L) 0f else spent.toFloat() / budget.amount.toFloat()
            BudgetWithProgress(
                budget = budget,
                category = categoryById[budget.categoryId],
                spent = spent,
                remaining = remaining,
                progress = progress,
            )
        }

        val totalBudget = monthBudgets
            .filter { it.categoryId == -1L }
            .ifEmpty { monthBudgets.filter { it.categoryId != -1L } }
            .sumOf { it.amount }

        val targetItems = targets.map { target ->
            val saved = FinanceCalculations.savingsSaved(target.id, transactions)
            val progress = if (target.targetAmount <= 0L) 0f else saved.toFloat() / target.targetAmount.toFloat()
            TargetWithProgress(target, saved, progress)
        }

        val warnings = budgetItems
            .filter { it.progress >= 0.8f }
            .map {
                val name = it.category?.name ?: "Semua Kategori"
                if (it.progress > 1f) {
                    "Anggaran $name sudah terlampaui (${(it.progress * 100).toInt()}%)"
                } else {
                    "Anggaran $name tersisa ${(1f - it.progress) * 100}%"
                }
            }

        return BudgetUiState(
            month = month,
            budgets = budgetItems,
            targets = targetItems,
            categories = categories,
            totalBudget = totalBudget,
            totalSpent = totalSpent,
            warnings = warnings,
        )
    }
}
