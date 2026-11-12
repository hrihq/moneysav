package com.moneysav.app.ui.features.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneysav.app.data.repository.FinanceRepository
import com.moneysav.app.domain.Category
import com.moneysav.app.domain.FinanceCalculations
import com.moneysav.app.domain.Transaction
import androidx.compose.ui.graphics.Color
import com.moneysav.app.ui.components.BarEntry
import com.moneysav.app.ui.components.DonutSlice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Calendar

enum class StatPeriod(val label: String) {
    DAILY("Harian"),
    WEEKLY("Mingguan"),
    MONTHLY("Bulanan"),
    YEARLY("Tahunan"),
}

data class StatisticsUiState(
    val period: StatPeriod = StatPeriod.MONTHLY,
    val rangeLabel: String = "",
    val income: Long = 0,
    val expense: Long = 0,
    val previousIncome: Long = 0,
    val previousExpense: Long = 0,
    val incomeChange: Double? = null,
    val expenseChange: Double? = null,
    val bars: List<BarEntry> = emptyList(),
    val donut: List<DonutSlice> = emptyList(),
    val topCategories: List<Pair<Category, Long>> = emptyList(),
    val categories: List<Category> = emptyList(),
)

class StatisticsViewModel(private val repo: FinanceRepository) : ViewModel() {

    private val period = MutableStateFlow(StatPeriod.MONTHLY)

    val uiState: StateFlow<StatisticsUiState> = combine(
        repo.transactions,
        repo.categories,
        period,
    ) { transactions, categories, p ->
        buildState(transactions, categories, p)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsUiState())

    fun setPeriod(p: StatPeriod) = period.update { p }

    private fun buildState(
        transactions: List<Transaction>,
        categories: List<Category>,
        period: StatPeriod,
    ): StatisticsUiState {
        val today = FinanceCalculations.startOfToday()
        val cal = Calendar.getInstance()

        val (rangeStart, rangeEnd, previousStart, previousEnd, bars, rangeLabel) = when (period) {
            StatPeriod.DAILY -> {
                val start = today - 6L * DAY
                val prevStart = start - 7L * DAY
                val entries = (0..6).map { i ->
                    val dayStart = start + i * DAY
                    val (s, e) = FinanceCalculations.dayRange(dayStart)
                    BarEntry(
                        label = FinanceCalculations.dayShort(dayStart),
                        income = FinanceCalculations.totalIncome(transactions, s, e),
                        expense = FinanceCalculations.totalExpense(transactions, s, e),
                    )
                }
                RangeData(
                    rangeStart = start,
                    rangeEnd = today + DAY,
                    previousStart = prevStart,
                    previousEnd = start,
                    bars = entries,
                    label = "7 hari terakhir",
                )
            }

            StatPeriod.WEEKLY -> {
                val start = today - 5L * 7L * DAY
                val prevStart = start - 42L * DAY
                val entries = (0..5).map { i ->
                    val weekStart = start + i * 7L * DAY
                    val s = weekStart
                    val e = weekStart + 7L * DAY
                    BarEntry(
                        label = FinanceCalculations.dayShort(weekStart),
                        income = FinanceCalculations.totalIncome(transactions, s, e),
                        expense = FinanceCalculations.totalExpense(transactions, s, e),
                    )
                }
                RangeData(
                    rangeStart = start,
                    rangeEnd = today + DAY,
                    previousStart = prevStart,
                    previousEnd = start,
                    bars = entries,
                    label = "6 minggu terakhir",
                )
            }

            StatPeriod.MONTHLY -> {
                val month = FinanceCalculations.currentMonth()
                val (s, e) = FinanceCalculations.monthRange(month)
                cal.timeInMillis = s
                val days = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val entries = (1..days).map { day ->
                    cal.timeInMillis = s
                    cal.set(Calendar.DAY_OF_MONTH, day)
                    val (ds, de) = FinanceCalculations.dayRange(cal.timeInMillis)
                    BarEntry(
                        label = if (day % 5 == 0) day.toString() else "",
                        income = FinanceCalculations.totalIncome(transactions, ds, de),
                        expense = FinanceCalculations.totalExpense(transactions, ds, de),
                    )
                }
                val prev = FinanceCalculations.previousMonth(month)
                val (ps, pe) = FinanceCalculations.monthRange(prev)
                RangeData(
                    rangeStart = s,
                    rangeEnd = e,
                    previousStart = ps,
                    previousEnd = pe,
                    bars = entries,
                    label = FinanceCalculations.monthLabel(month),
                )
            }

            StatPeriod.YEARLY -> {
                val month = FinanceCalculations.currentMonth()
                val entries = (11 downTo 0).map { delta ->
                    val key = FinanceCalculations.addMonths(month, -delta)
                    val (s, e) = FinanceCalculations.monthRange(key)
                    BarEntry(
                        label = FinanceCalculations.monthShort(key),
                        income = FinanceCalculations.totalIncome(transactions, s, e),
                        expense = FinanceCalculations.totalExpense(transactions, s, e),
                    )
                }
                val firstKey = FinanceCalculations.addMonths(month, -11)
                val (s, e) = FinanceCalculations.monthRange(firstKey)
                val (ps, pe) = FinanceCalculations.monthRange(FinanceCalculations.addMonths(firstKey, -12))
                RangeData(
                    rangeStart = s,
                    rangeEnd = e,
                    previousStart = ps,
                    previousEnd = pe,
                    bars = entries,
                    label = "12 bulan terakhir",
                )
            }
        }

        val income = FinanceCalculations.totalIncome(transactions, rangeStart, rangeEnd)
        val expense = FinanceCalculations.totalExpense(transactions, rangeStart, rangeEnd)
        val prevIncome = FinanceCalculations.totalIncome(transactions, previousStart, previousEnd)
        val prevExpense = FinanceCalculations.totalExpense(transactions, previousStart, previousEnd)

        val byCategory = FinanceCalculations.expensesByCategory(transactions, rangeStart, rangeEnd)
        val categoryById = categories.associateBy { it.id }
        val top = byCategory.entries
            .sortedByDescending { it.value }
            .mapNotNull { (id, value) -> categoryById[id]?.let { it to value } }
        val donut = top.take(5).map { (category, value) ->
            DonutSlice(category.name, value, Color(category.colorArgb))
        }

        return StatisticsUiState(
            period = period,
            rangeLabel = rangeLabel,
            income = income,
            expense = expense,
            previousIncome = prevIncome,
            previousExpense = prevExpense,
            incomeChange = FinanceCalculations.percentChange(income, prevIncome),
            expenseChange = FinanceCalculations.percentChange(expense, prevExpense),
            bars = bars,
            donut = donut,
            topCategories = top,
            categories = categories,
        )
    }

    private data class RangeData(
        val rangeStart: Long,
        val rangeEnd: Long,
        val previousStart: Long,
        val previousEnd: Long,
        val bars: List<BarEntry>,
        val label: String,
    )

    private companion object {
        const val DAY = 24L * 60L * 60L * 1000L
    }
}
