package com.moneysav.app

import com.moneysav.app.domain.FinanceCalculations
import com.moneysav.app.domain.Transaction
import com.moneysav.app.domain.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class FinanceCalculationsTest {

    private fun millis(year: Int, month1Based: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month1Based - 1, day, 12, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun tx(id: Long, type: TransactionType, amount: Long, day: Int, targetId: Long? = null) =
        Transaction(
            id = id,
            type = type,
            amount = amount,
            categoryId = 1L,
            note = "",
            date = millis(2026, 11, day),
            paymentMethod = "Tunai",
            savingsTargetId = targetId,
        )

    @Test
    fun `saldo adalah pemasukan dikurangi pengeluaran`() {
        val list = listOf(
            tx(1, TransactionType.INCOME, 5_000_000, 1),
            tx(2, TransactionType.EXPENSE, 1_500_000, 2),
            tx(3, TransactionType.EXPENSE, 750_000, 3),
        )
        assertEquals(2_750_000L, FinanceCalculations.balance(list))
    }

    @Test
    fun `total bulanan hanya menghitung transaksi pada rentangnya`() {
        val (start, end) = FinanceCalculations.monthRange("2026-11")
        val list = listOf(
            tx(1, TransactionType.INCOME, 1_000_000, 1),
            tx(2, TransactionType.EXPENSE, 250_000, 15),
            tx(3, TransactionType.INCOME, 9_999_999, 20),
        )
        // Transaksi di luar November 2026.
        val outside = tx(4, TransactionType.EXPENSE, 5_000_000, 1).copy(date = millis(2026, 12, 1))

        assertEquals(10_999_999L, FinanceCalculations.totalIncome(list + outside, start, end))
        assertEquals(250_000L, FinanceCalculations.totalExpense(list + outside, start, end))
    }

    @Test
    fun `sisa anggaran adalah anggaran dikurangi pemakaian`() {
        assertEquals(3_000_000L, FinanceCalculations.budgetRemaining(5_000_000L, 2_000_000L))
        assertEquals(-500_000L, FinanceCalculations.budgetRemaining(1_000_000L, 1_500_000L))
    }

    @Test
    fun `progress tabungan hanya menghitung pemasukan ke target tersebut`() {
        val list = listOf(
            tx(1, TransactionType.INCOME, 1_000_000, 1, targetId = 7L),
            tx(2, TransactionType.INCOME, 500_000, 2, targetId = 8L),
            tx(3, TransactionType.EXPENSE, 250_000, 3, targetId = 7L),
        )
        assertEquals(1_000_000L, FinanceCalculations.savingsSaved(7L, list))
        assertEquals(500_000L, FinanceCalculations.savingsSaved(8L, list))
    }

    @Test
    fun `persentase perubahan null bila periode sebelumnya nol`() {
        assertNull(FinanceCalculations.percentChange(1_000L, 0L))
        assertEquals(50.0, FinanceCalculations.percentChange(1_500L, 1_000L)!!, 0.0001)
    }

    @Test
    fun `navigasi bulan berjalan`() {
        assertEquals("2026-10", FinanceCalculations.previousMonth("2026-11"))
        assertEquals("2026-12", FinanceCalculations.addMonths("2026-11", 1))
        assertEquals("2027-01", FinanceCalculations.addMonths("2026-12", 1))
    }
}
