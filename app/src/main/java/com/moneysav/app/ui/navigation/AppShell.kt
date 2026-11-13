package com.moneysav.app.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
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
import com.moneysav.app.ui.components.AnimatedBackdrop
import com.moneysav.app.ui.components.MoneySavIcons
import com.moneysav.app.ui.components.ScreenBottomSheet
import com.moneysav.app.ui.features.budget.BudgetScreen
import com.moneysav.app.ui.features.budget.BudgetViewModel
import com.moneysav.app.ui.features.home.HomeScreen
import com.moneysav.app.ui.features.home.HomeViewModel
import com.moneysav.app.ui.features.settings.SettingsScreen
import com.moneysav.app.ui.features.statistics.StatisticsScreen
import com.moneysav.app.ui.features.statistics.StatisticsViewModel
import com.moneysav.app.ui.features.transactions.AddEditTransactionScreen
import com.moneysav.app.ui.features.transactions.AddEditTransactionViewModel
import com.moneysav.app.ui.features.transactions.TransactionsScreen
import com.moneysav.app.ui.features.transactions.TransactionsViewModel
import com.moneysav.app.ui.theme.LiquidMotion
import com.moneysav.app.ui.theme.ThemeState
import kotlinx.coroutines.delay
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
fun AppShell(repository: FinanceRepository, themeState: ThemeState) {

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var overlay by remember { mutableStateOf<Overlay?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showIntro by remember { mutableStateOf(true) }
    // Menjaga overlay terakhir agar isi sheet tidak hilang saat animasi keluar.
    var lastOverlay by remember { mutableStateOf<Overlay?>(null) }
    if (overlay != null) lastOverlay = overlay

    // Animasi intro saat aplikasi dibuka.
    LaunchedEffect(Unit) {
        delay(900)
        showIntro = false
    }

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

    BackHandler(enabled = overlay != null || showSettings) {
        if (overlay != null) overlay = null else showSettings = false
    }

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
                        actions = {
                            IconButton(onClick = { showSettings = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "Pengaturan",
                                    tint = MiuixTheme.colorScheme.onSurface,
                                )
                            }
                        },
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
                        AnimatedBackdrop(tabIndex = tab)
                        if (overlay == null) {
                            AnimatedContent(
                                targetState = tab,
                                transitionSpec = {
                                    val forward = targetState > initialState
                                    val enter: (Int) -> Int = { if (forward) it / 3 else -it / 3 }
                                    val exit: (Int) -> Int = { if (forward) -it / 3 else it / 3 }
                                    (
                                        fadeIn(LiquidMotion.snappy()) +
                                            slideInHorizontally(LiquidMotion.gentle(), enter) +
                                            scaleIn(initialScale = 0.94f, animationSpec = LiquidMotion.gentle())
                                        ).togetherWith(
                                        fadeOut(LiquidMotion.snappy()) +
                                            slideOutHorizontally(LiquidMotion.snappy(), exit) +
                                            scaleOut(targetScale = 0.94f, animationSpec = LiquidMotion.snappy()),
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

        // Sheet tambah/ubah transaksi: setengah layar, tarik ke bawah untuk menutup.
        AnimatedVisibility(
            visible = overlay != null,
            enter = slideInVertically(LiquidMotion.gentle()) { it } + fadeIn(LiquidMotion.snappy()),
            exit = slideOutVertically(LiquidMotion.snappy()) { it } + fadeOut(LiquidMotion.snappy()),
        ) {
            val current = lastOverlay ?: Overlay.AddTransaction
            ScreenBottomSheet(onDismiss = { overlay = null }) {
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
                AddEditTransactionScreen(
                    viewModel = addViewModel,
                    modifier = Modifier.fillMaxSize(),
                    onDismiss = { overlay = null },
                )
            }
        }

        // Pengaturan tema.
        AnimatedVisibility(
            visible = showSettings,
            enter = fadeIn(LiquidMotion.snappy()) + slideInVertically(LiquidMotion.gentle()) { it / 6 },
            exit = fadeOut(LiquidMotion.snappy()) + slideOutVertically(LiquidMotion.snappy()) { it / 6 },
        ) {
            Box(Modifier.fillMaxSize()) {
                AppBackground()
                SettingsScreen(themeState = themeState, onClose = { showSettings = false })
            }
        }

        // Intro saat aplikasi dibuka.
        AnimatedVisibility(
            visible = showIntro,
            enter = fadeIn(LiquidMotion.snappy()) + scaleIn(initialScale = 0.86f, animationSpec = LiquidMotion.gentle()),
            exit = fadeOut(LiquidMotion.snappy()) + scaleOut(targetScale = 1.08f, animationSpec = LiquidMotion.snappy()),
        ) {
            IntroOverlay()
        }
    }
}

/** Splash singkat saat aplikasi dibuka. */
@Composable
private fun IntroOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MiuixTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MoneySavIcons.income,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MiuixTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                text = "MoneySav",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Catat keuangan dengan tenang",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
