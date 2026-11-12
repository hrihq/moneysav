package com.moneysav.app.ui.features.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneysav.app.data.repository.FinanceRepository
import com.moneysav.app.domain.Category
import com.moneysav.app.domain.SavingsTarget
import com.moneysav.app.domain.Transaction
import com.moneysav.app.domain.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddEditTransactionViewModel(
    private val repo: FinanceRepository,
    private val editId: Long?,
) : ViewModel() {

    val categories: StateFlow<List<Category>> = repo.categories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList(),
    )

    val targets: StateFlow<List<SavingsTarget>> = repo.savingsTargets.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList(),
    )

    private val _initial = MutableStateFlow<Transaction?>(null)
    val initial: StateFlow<Transaction?> = _initial

    init {
        if (editId != null) {
            viewModelScope.launch { _initial.value = repo.transactionById(editId) }
        }
    }

    fun save(
        type: TransactionType,
        amount: Long,
        categoryId: Long,
        note: String,
        date: Long,
        paymentMethod: String,
        savingsTargetId: Long?,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            val existing = editId?.let { repo.transactionById(it) }
            val entity = Transaction(
                id = existing?.id ?: 0,
                type = type,
                amount = amount,
                categoryId = categoryId,
                note = note,
                date = date,
                paymentMethod = paymentMethod,
                savingsTargetId = savingsTargetId,
            )
            if (existing == null) repo.addTransaction(entity) else repo.updateTransaction(entity)
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = editId ?: return
        viewModelScope.launch {
            repo.deleteTransaction(id)
            onDone()
        }
    }
}
