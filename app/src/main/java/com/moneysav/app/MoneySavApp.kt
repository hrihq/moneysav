package com.moneysav.app

import android.app.Application
import com.moneysav.app.data.db.AppDatabase
import com.moneysav.app.data.repository.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MoneySavApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val repository: FinanceRepository by lazy { FinanceRepository(database) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { repository.ensureDefaultCategories() }
    }
}
