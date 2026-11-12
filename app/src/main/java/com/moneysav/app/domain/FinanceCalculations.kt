package com.moneysav.app.domain

import java.util.Calendar
import java.util.Locale

/**
 * Perhitungan keuangan murni (tanpa Android) — dipakai bersama oleh ViewModel dan unit test.
 *
 * Semua nominal dalam Rupiah (Long, bulat) agar tidak ada kesalahan pembulatan.
 */
object FinanceCalculations {

    /** Saldo = total pemasukan - total pengeluaran. */
    fun balance(transactions: List<Transaction>): Long = transactions.sumOf {
        if (it.type == TransactionType.INCOME) it.amount else -it.amount
    }

    fun totalIncome(transactions: List<Transaction>, startMillis: Long, endMillis: Long): Long =
        sum(transactions, TransactionType.INCOME, startMillis, endMillis)

    fun totalExpense(transactions: List<Transaction>, startMillis: Long, endMillis: Long): Long =
        sum(transactions, TransactionType.EXPENSE, startMillis, endMillis)

    private fun sum(
        transactions: List<Transaction>,
        type: TransactionType,
        startMillis: Long,
        endMillis: Long,
    ): Long = transactions.sumOf {
        if (it.type == type && it.date >= startMillis && it.date < endMillis) it.amount else 0L
    }

    /** Total pengeluaran per kategori pada rentang waktu tertentu. */
    fun expensesByCategory(
        transactions: List<Transaction>,
        startMillis: Long,
        endMillis: Long,
    ): Map<Long, Long> = transactions
        .filter { it.type == TransactionType.EXPENSE && it.date >= startMillis && it.date < endMillis }
        .groupingBy { it.categoryId }
        .fold(0L) { acc, t -> acc + t.amount }

    /** Sisa anggaran = anggaran - pengeluaran kategori. */
    fun budgetRemaining(budgetAmount: Long, spent: Long): Long = budgetAmount - spent

    /** Progress tabungan = jumlah transaksi yang dialokasikan ke target tersebut. */
    fun savingsSaved(targetId: Long, transactions: List<Transaction>): Long = transactions.sumOf {
        if (it.savingsTargetId == targetId && it.type == TransactionType.INCOME) it.amount else 0L
    }

    /**
     * Persentase perubahan. null bila periode sebelumnya nol (tidak ada dasar pembanding).
     */
    fun percentChange(current: Long, previous: Long): Double? =
        if (previous == 0L) null else (current - previous).toDouble() / previous.toDouble() * 100.0

    /** Rentang [start, end) untuk bulan "yyyy-MM" tertentu. */
    fun monthRange(month: String): Pair<Long, Long> {
        val parts = month.split("-")
        val year = parts[0].toInt()
        val monthIdx = parts[1].toInt()
        val cal = Calendar.getInstance()
        cal.set(year, monthIdx - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        return start to cal.timeInMillis
    }

    /** Rentang hari untuk tanggal epoch tertentu. */
    fun dayRange(millis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return start to cal.timeInMillis
    }

    /** Label bulan "yyyy-MM" -> "Jan 2026" (locale Indonesia). */
    fun monthLabel(month: String): String {
        val parts = month.split("-")
        val cal = Calendar.getInstance()
        cal.set(parts[0].toInt(), parts[1].toInt() - 1, 1)
        val sb = StringBuilder()
        sb.append(cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale("in", "ID")) ?: "")
        sb.append(" ").append(cal.get(Calendar.YEAR))
        return sb.toString()
    }

    /** Bulan sebelumnya (format "yyyy-MM"). */
    fun previousMonth(month: String): String {
        val parts = month.split("-")
        val cal = Calendar.getInstance()
        cal.set(parts[0].toInt(), parts[1].toInt() - 1, 1)
        cal.add(Calendar.MONTH, -1)
        return String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    /** Bulan berjalan sebagai "yyyy-MM". */
    fun currentMonth(): String {
        val cal = Calendar.getInstance()
        return String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    /** Geser bulan sebuah kunci "yyyy-MM". */
    fun addMonths(month: String, delta: Int): String {
        val parts = month.split("-")
        val cal = Calendar.getInstance()
        cal.set(parts[0].toInt(), parts[1].toInt() - 1, 1)
        cal.add(Calendar.MONTH, delta)
        return String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    /** Label singkat bulan, mis. "Jan". */
    fun monthShort(month: String): String {
        val parts = month.split("-")
        val cal = Calendar.getInstance()
        cal.set(parts[0].toInt(), parts[1].toInt() - 1, 1)
        return (cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale("in", "ID")) ?: "").take(3)
    }

    /** Label singkat hari dalam minggu, mis. "Sen". */
    fun dayShort(millis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val name = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Sen"
            Calendar.TUESDAY -> "Sel"
            Calendar.WEDNESDAY -> "Rab"
            Calendar.THURSDAY -> "Kam"
            Calendar.FRIDAY -> "Jum"
            Calendar.SATURDAY -> "Sab"
            else -> "Min"
        }
        return name
    }

    /** Awal hari ini (jam 00:00). */
    fun startOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
