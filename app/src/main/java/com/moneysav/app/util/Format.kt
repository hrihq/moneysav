package com.moneysav.app.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Utilitas format tampilan (Rupiah & tanggal, locale Indonesia). */
object Format {

    private val idLocale: Locale = Locale("in", "ID")

    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(idLocale).apply {
        if (this is DecimalFormat) {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }

    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy • HH:mm", idLocale)
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", idLocale)
    private val monthFormat = SimpleDateFormat("MMMM yyyy", idLocale)

    /** "Rp 12.500" */
    fun currency(amount: Long): String = currencyFormat.format(amount)

    /** "+Rp 12.500" untuk pemasukan. */
    fun currencySigned(amount: Long, positive: Boolean): String =
        (if (positive) "+" else "-") + currency(kotlin.math.abs(amount))

    fun date(millis: Long): String = dateFormat.format(Date(millis))

    fun dateTime(millis: Long): String = dateTimeFormat.format(Date(millis))

    fun monthYear(millis: Long): String = monthFormat.format(Date(millis))

    /** "2026-11" */
    fun monthKey(millis: Long): String {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = millis
        return String.format(Locale.US, "%04d-%02d", cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1)
    }
}
