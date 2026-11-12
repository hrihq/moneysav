package com.moneysav.app.data.db

import com.moneysav.app.domain.TransactionType

/** Kategori bawaan saat database pertama dibuat. */
object DefaultCategories {

    private data class Spec(
        val name: String,
        val type: TransactionType,
        val icon: String,
        val colorArgb: Long,
    )

    private val specs = listOf(
        // Pengeluaran
        Spec("Makanan & Minuman", TransactionType.EXPENSE, "restaurant", 0xFFE8743B),
        Spec("Transportasi", TransactionType.EXPENSE, "bus", 0xFF4FC3F7),
        Spec("Belanja", TransactionType.EXPENSE, "shopping_bag", 0xFFBA68C8),
        Spec("Tagihan", TransactionType.EXPENSE, "receipt", 0xFF4DB6AC),
        Spec("Hiburan", TransactionType.EXPENSE, "movie", 0xFFF06292),
        Spec("Kesehatan", TransactionType.EXPENSE, "health", 0xFFEF5350),
        Spec("Pendidikan", TransactionType.EXPENSE, "school", 0xFF9575CD),
        Spec("Rumah Tangga", TransactionType.EXPENSE, "home", 0xFFA1887F),
        Spec("Lainnya", TransactionType.EXPENSE, "category", 0xFF90A4AE),
        // Pemasukan
        Spec("Gaji", TransactionType.INCOME, "work", 0xFF66BB6A),
        Spec("Usaha", TransactionType.INCOME, "store", 0xFFFFA726),
        Spec("Investasi", TransactionType.INCOME, "trending_up", 0xFF26A69A),
        Spec("Hadiah", TransactionType.INCOME, "gift", 0xFFEC407A),
        Spec("Lainnya", TransactionType.INCOME, "category", 0xFF90A4AE),
    )

    fun entities(): List<CategoryEntity> = specs.map {
        CategoryEntity(
            name = it.name,
            type = if (it.type == TransactionType.INCOME) "INCOME" else "EXPENSE",
            icon = it.icon,
            colorArgb = it.colorArgb,
        )
    }
}
