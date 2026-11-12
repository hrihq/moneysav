package com.moneysav.app.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.moneysav.app.data.repository.FinanceRepository
import com.moneysav.app.glass.AppBackground
import com.moneysav.app.glass.GlassNavBarContent
import com.moneysav.app.glass.LocalGlassBackdrop
import com.moneysav.app.glass.glassNavBar
import com.moneysav.app.glass.rememberGlassBackdrop
import com.moneysav.app.ui.VmFactory
import com.moneysav.app.ui.components.MoneySavIcons
import com.moneysav.app.ui.features.budget.BudgetScreen
import com.moneysav.app.ui.features.budget.BudgetViewModel
import com.moneysav.app.ui.features.home.HomeScreen
import com.moneysav.app.ui.features.home.HomeViewModel
import com.moneysav.app.ui.features.statistics.StatisticsScreen
import com.moneysav.app.ui.features.statistics.StatisticsViewModel
import com.moneysav.app.ui.features.transactions.AddEditTransactionScreen
import com.moneysav.app.ui.features.transactions.AddEditTransactionViewModel
import com.moneysav.app.ui.features.transactions.TransactionsScreen
import com.moneysav.app.ui.features.transactions.TransactionsViewModel
import com.moneysav.app.ui.theme.LiquidMotion
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class Tab(val title: String) {
    HOME("Beranda"),
    TRANSACTIONS("Transaksi"),
    STATISTICS("Statistik"),
    BUDGET("Anggaran"),
}

private sealed interface Overlay {
    data object AddTransaction : Overlay
    data class EditTransaction(val id: Long) : Overlay
}

private val BarMargin = 16.dp
private val BarHeight = 64.dp
private val BarPressedScale = 1.04f

/**
 * Struktur ini mengikuti liquid-miuix: layer pengambilan (backdrop) berisi latar + konten,
 * sedangkan nav bar kaca diposisikan sebagai sibling di luar layer tersebut.
 */
@Composable
fun AppShell(repository: FinanceRepository) {

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var overlay by remember { mutableStateOf<Overlay?>(null) }

    val backdrop = rememberGlassBackdrop()
    var barPressed by remember { mutableStateOf(false) }
    val barScale by animateFloatAsState(
        targetValue = if (barPressed) BarPressedScale else 1f,
        animationSpec = LiquidMotion.press(),
        label = "barScale",
    )

    val homeViewModel: HomeViewModel = viewModel(factory = VmFactory { HomeViewModel(repository) })
    val transactionsViewModel: TransactionsViewModel = viewModel(factory = VmFactory { TransactionsViewModel(repository) })
    val statisticsViewModel: StatisticsViewModel = viewModel(factory = VmFactory { StatisticsViewModel(repository) })
    val budgetViewModel: BudgetViewModel = viewModel(factory = VmFactory { BudgetViewModel(repository) })

    val home by homeViewModel.uiState.collectAsState()
    val transactions by transactionsViewModel.uiState.collectAsState()
    val statistics by statisticsViewModel.uiState.collectAsState()
    val budget by budgetViewModel.uiState.collectAsState()

    // Minta izin notifikasi (Android 13+) untuk peringatan anggaran.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    BackHandler(enabled = overlay != null) { overlay = null }

    val currentTab = Tab.entries.getOrElse(tab) { Tab.HOME }
    val title = when (overlay) {
        is Overlay.AddTransaction -> "Tambah Transaksi"
        is Overlay.EditTransaction -> "Ubah Transaksi"
        null -> currentTab.title
    }

    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalGlassBackdrop provides backdrop) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    SmallTopAppBar(
                        title = title,
                        modifier = Modifier,
                        color = Color.Transparent,
                    )
                },
            ) { padding ->
                val ld = LocalLayoutDirection.current
                val contentPadding = PaddingValues(
                    start = padding.calculateStartPadding(ld),
                    top = padding.calculateTopPadding(),
                    end = padding.calculateEndPadding(ld),
                    bottom = padding.calculateBottomPadding(),
                )

                Box(Modifier.fillMaxSize()) {
                    // ── Layer pengambilan: latar + seluruh konten halaman ──
                    Box(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                        AppBackground()
                        if (overlay == null) {
                            AnimatedContent(
                                targetState = tab,
                                transitionSpec = {
                                    val forward = targetState > initialState
                                    val enter: (Int) -> Int = { if (forward) it / 4 else -it / 4 }
                                    val exit: (Int) -> Int = { if (forward) -it / 4 else it / 4 }
                                    (
                                        fadeIn(LiquidMotion.snappy()) +
                                            slideInHorizontally(LiquidMotion.gentle(), enter)
                                        ).togetherWith(
                                        fadeOut(LiquidMotion.snappy()) +
                                            slideOutHorizontally(LiquidMotion.snappy(), exit),
                                    )
                                },
                                label = "page",
                            ) { index ->
                                Box(Modifier.fillMaxSize().padding(contentPadding)) {
                                    when (Tab.entries.getOrElse(index) { Tab.HOME }) {
                                        Tab.HOME -> HomeScreen(
                                            state = home,
                                            onAddTransaction = { overlay = Overlay.AddTransaction },
                                            onOpenTransaction = { overlay = Overlay.EditTransaction(it) },
                                            onOpenAllTransactions = { tab = Tab.TRANSACTIONS.ordinal },
                                            onOpenBudget = { tab = Tab.BUDGET.ordinal },
                                        )

                                        Tab.TRANSACTIONS -> TransactionsScreen(
                                            state = transactions,
                                            onQueryChange = transactionsViewModel::setQuery,
                                            onTypeChange = transactionsViewModel::setType,
                                            onCategoryChange = transactionsViewModel::setCategory,
                                            onMonthChange = transactionsViewModel::setMonth,
                                            onOpenTransaction = { overlay = Overlay.EditTransaction(it) },
                                        )

                                        Tab.STATISTICS -> StatisticsScreen(
                                            state = statistics,
                                            onPeriodChange = statisticsViewModel::setPeriod,
                                        )

                                        Tab.BUDGET -> BudgetScreen(
                                            state = budget,
                                            onMonthChange = budgetViewModel::setMonth,
                                            onAddBudget = budgetViewModel::addBudget,
                                            onDeleteBudget = budgetViewModel::deleteBudget,
                                            onAddTarget = budgetViewModel::addTarget,
                                            onDeleteTarget = budgetViewModel::deleteTarget,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (overlay == null) {
                        // ── Nav bar kaca mengambang (di luar layer pengambilan) ──
                        Box(Modifier.fillMaxSize().padding(padding)) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(start = BarMargin, end = BarMargin, bottom = BarMargin)
                                    .fillMaxWidth()
                                    .height(BarHeight)
                                    .graphicsLayer {
                                        scaleX = barScale
                                        scaleY = barScale
                                    },
                            ) {
                                Box(
                                    Modifier
                                        .matchParentSize()
                                        .glassNavBar(
                                            backdrop = backdrop,
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                                        ),
                                )
                                GlassNavBarContent(
                                    items = listOf(
                                        MoneySavIcons.home to Tab.HOME.title,
                                        MoneySavIcons.transactions to Tab.TRANSACTIONS.title,
                                        MoneySavIcons.statistics to Tab.STATISTICS.title,
                                        MoneySavIcons.budget to Tab.BUDGET.title,
                                    ),
                                    selectedIndex = currentTab.ordinal,
                                    onSelect = { tab = it },
                                    modifier = Modifier.matchParentSize(),
                                    backdrop = backdrop,
                                    contentHeight = BarHeight,
                                    onBarPressedChange = { barPressed = it },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Overlay (tambah/ubah transaksi) berada di luar layer pengambilan.
        overlay?.let { current ->
            val addViewModel: AddEditTransactionViewModel = viewModel(
                key = when (current) {
                    is Overlay.AddTransaction -> "add-transaction"
                    is Overlay.EditTransaction -> "edit-transaction-${current.id}"
                },
                factory = VmFactory {
                    AddEditTransactionViewModel(
                        repo = repository,
                        editId = when (current) {
                            is Overlay.AddTransaction -> null
                            is Overlay.EditTransaction -> current.id
                        },
                    )
                },
            )
            Box(Modifier.fillMaxSize()) {
                AppBackground()
                AddEditTransactionScreen(
                    viewModel = addViewModel,
                    onDismiss = { overlay = null },
                )
            }
        }
    }
}
