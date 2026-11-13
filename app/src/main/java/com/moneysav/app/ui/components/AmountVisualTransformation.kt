package com.moneysav.app.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Menampilkan angka mentah (mis. "50000") sebagai "50.000" tanpa mengubah nilai di baliknya.
 * Menghindari salah input karena pemisah ribuan selalu terlihat.
 */
class AmountVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = formatDigits(digits)
        return TransformedText(
            AnnotatedString(formatted),
            object : OffsetMapping {
                override fun originalToTransformed(index: Int): Int {
                    if (index <= 0) return 0
                    if (index >= digits.length) return formatted.length
                    var dots = 0
                    for (i in 0 until index) {
                        if (i != 0 && (digits.length - i) % 3 == 0) dots++
                    }
                    return index + dots
                }

                override fun transformedToOriginal(index: Int): Int {
                    if (index <= 0) return 0
                    if (index >= formatted.length) return digits.length
                    val dots = formatted.substring(0, index).count { it == '.' }
                    return (index - dots).coerceIn(0, digits.length)
                }
            },
        )
    }

    companion object {
        /** "50000" -> "50.000" */
        fun formatDigits(digits: String): String {
            val cleaned = digits.filter { it.isDigit() }
            if (cleaned.isEmpty()) return ""
            val builder = StringBuilder()
            cleaned.forEachIndexed { index, c ->
                if (index != 0 && (cleaned.length - index) % 3 == 0) builder.append('.')
                builder.append(c)
            }
            return builder.toString()
        }
    }
}
