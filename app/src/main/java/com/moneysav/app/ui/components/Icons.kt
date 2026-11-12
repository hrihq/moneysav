package com.moneysav.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

/** Pemetaan kunci ikon kategori -> ImageVector. */
object MoneySavIcons {

    fun forKey(key: String): ImageVector = when (key) {
        "restaurant" -> Icons.Rounded.Restaurant
        "bus" -> Icons.Rounded.DirectionsBus
        "shopping_bag" -> Icons.Rounded.ShoppingBag
        "receipt" -> Icons.Rounded.Receipt
        "movie" -> Icons.Rounded.Movie
        "health" -> Icons.Rounded.HealthAndSafety
        "school" -> Icons.Rounded.School
        "home" -> Icons.Rounded.Home
        "work" -> Icons.Rounded.Work
        "store" -> Icons.Rounded.Store
        "trending_up" -> Icons.Rounded.TrendingUp
        "gift" -> Icons.Rounded.CardGiftcard
        "wallet" -> Icons.Rounded.Wallet
        "card" -> Icons.Rounded.CreditCard
        "bank" -> Icons.Rounded.AccountBalance
        "savings" -> Icons.Rounded.Savings
        "payments" -> Icons.Rounded.Payments
        else -> Icons.Rounded.Category
    }

    val home = Icons.Rounded.Home
    val transactions = Icons.Rounded.ReceiptLong
    val statistics = Icons.Rounded.BarChart
    val budget = Icons.Rounded.AccountBalanceWallet
    val income = Icons.Rounded.TrendingUp
    val expense = Icons.Rounded.TrendingDown
    val check = Icons.Rounded.Check
}
