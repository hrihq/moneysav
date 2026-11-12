package com.moneysav.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.moneysav.app.domain.Budget
import com.moneysav.app.domain.Category
import com.moneysav.app.domain.SavingsTarget
import com.moneysav.app.domain.Transaction
import com.moneysav.app.domain.TransactionType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** "INCOME" | "EXPENSE" */
    val type: String,
    val icon: String,
    val colorArgb: Long,
) {
    fun toDomain() = Category(
        id = id,
        name = name,
        type = if (type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE,
        icon = icon,
        colorArgb = colorArgb,
    )
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** "INCOME" | "EXPENSE" */
    val type: String,
    val amount: Long,
    val categoryId: Long,
    val note: String = "",
    val date: Long,
    val paymentMethod: String = "Tunai",
    val savingsTargetId: Long? = null,
) {
    fun toDomain() = Transaction(
        id = id,
        type = if (type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE,
        amount = amount,
        categoryId = categoryId,
        note = note,
        date = date,
        paymentMethod = paymentMethod,
        savingsTargetId = savingsTargetId,
    )

    companion object {
        fun fromDomain(t: Transaction) = TransactionEntity(
            id = t.id,
            type = if (t.type == TransactionType.INCOME) "INCOME" else "EXPENSE",
            amount = t.amount,
            categoryId = t.categoryId,
            note = t.note,
            date = t.date,
            paymentMethod = t.paymentMethod,
            savingsTargetId = t.savingsTargetId,
        )
    }
}

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** -1 = semua kategori. */
    val categoryId: Long,
    /** "yyyy-MM" */
    val month: String,
    val amount: Long,
) {
    fun toDomain() = Budget(id = id, categoryId = categoryId, month = month, amount = amount)

    companion object {
        fun fromDomain(b: Budget) = BudgetEntity(id = b.id, categoryId = b.categoryId, month = b.month, amount = b.amount)
    }
}

@Entity(tableName = "savings_targets")
data class SavingsTargetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Long,
    val colorArgb: Long = 0xFF3482FF,
) {
    fun toDomain() = SavingsTarget(id = id, name = name, targetAmount = targetAmount, colorArgb = colorArgb)

    companion object {
        fun fromDomain(t: SavingsTarget) = SavingsTargetEntity(
            id = t.id, name = t.name, targetAmount = t.targetAmount, colorArgb = t.colorArgb,
        )
    }
}
