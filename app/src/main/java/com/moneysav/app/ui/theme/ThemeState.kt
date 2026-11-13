package com.moneysav.app.ui.theme

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Status tema (warna aksen + mode gelap) yang bertahan di SharedPreferences.
 *
 * [dark] bernilai null berarti mengikuti sistem.
 */
class ThemeState(private val prefs: SharedPreferences) {

    var accent by mutableStateOf(
        LiquidAccentColor.fromId(prefs.getString(KEY_ACCENT, null)),
    )
        private set

    var dark by mutableStateOf(
        when (prefs.getString(KEY_DARK, "system")) {
            "dark" -> true
            "light" -> false
            else -> null
        },
    )
        private set

    fun selectAccent(value: LiquidAccentColor) {
        accent = value
        prefs.edit().putString(KEY_ACCENT, value.name).apply()
    }

    fun selectDark(value: Boolean?) {
        dark = value
        prefs.edit().putString(KEY_DARK, if (value == null) "system" else if (value) "dark" else "light").apply()
    }

    companion object {
        const val KEY_ACCENT = "accent"
        const val KEY_DARK = "dark"
    }
}
