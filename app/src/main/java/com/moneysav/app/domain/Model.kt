package com.moneysav.app.domain

/** Jenis transaksi. */
enum class TransactionType { INCOME, EXPENSE }

/** Kategori pemasukan / pengeluaran. */
data class Category(
    val id: Long,
    val name: String,
    val type: TransactionType,
    /** Kunci ikon vector (dipetakan ke ImageVector di UI). */
    val icon: String,
    val colorArgb: Long,
)

/** Satu transaksi. [amount] dalam Rupiah (bulat). */
data class Transaction(
    val id: Long,
    val type: TransactionType,
    val amount: Long,
    val categoryId: Long,
    val note: String = "",
    /** Epoch millis. */
    val date: Long,
    val paymentMethod: String = "Tunai",
    /** Transaksi ini dialokasikan ke target tabungan (opsional). */
    val savingsTargetId: Long? = null,
)

/** Anggaran bulanan, per kategori. [categoryId] = -1 berarti semua kategori. */
data class Budget(
    val id: Long,
    val categoryId: Long,
    /** Format "yyyy-MM". */
    val month: String,
    val amount: Long,
)

/** Target tabungan. */
data class SavingsTarget(
    val id: Long,
    val name: String,
    val targetAmount: Long,
    val colorArgb: Long = 0xFF3482FF,
)

/** Metode pembayaran tersedia. */
object PaymentMethods {
    val all = listOf("Tunai", "Kartu Debit", "Kartu Kredit", "E-Wallet", "Transfer Bank", "Lainnya")
}
