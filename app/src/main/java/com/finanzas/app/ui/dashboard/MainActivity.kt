package com.finanzas.app.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.finanzas.app.FinanzasApp
import com.finanzas.app.ui.dashboard.components.AccountsSection
import com.finanzas.app.ui.dashboard.components.BalanceSummaryCard
import com.finanzas.app.ui.dashboard.components.CreateAccountDialog
import com.finanzas.app.ui.dashboard.components.EditAccountDialog
import com.finanzas.app.ui.dashboard.components.EditTransactionDialog
import com.finanzas.app.ui.dashboard.components.ExportBackupDialog
import com.finanzas.app.ui.dashboard.components.OverlayPermissionBanner
import com.finanzas.app.ui.dashboard.components.TransactionTimelineSection
import com.finanzas.app.ui.debts.DebtsSection
import com.finanzas.app.ui.debts.DebtsViewModel
import com.finanzas.app.ui.manage.ManageSection
import com.finanzas.app.ui.manage.components.CreateCategoryDialog
import com.finanzas.app.ui.overlay.QuickMovementOverlayScreen
import com.finanzas.app.ui.overlay.QuickMovementViewModel
import com.finanzas.app.ui.reports.ReportsSection
import com.finanzas.app.ui.theme.Amber500
import com.finanzas.app.ui.theme.BackgroundDark
import com.finanzas.app.ui.theme.Cyan500
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.SurfaceDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

class MainActivity : ComponentActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var debtsViewModel: DebtsViewModel

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as FinanzasApp
        mainViewModel = MainViewModel(
            accountRepository = app.accountRepository,
            categoryRepository = app.categoryRepository,
            transactionRepository = app.transactionRepository,
            currencyRepository = app.currencyRepository,
            getFinancialSummaryUseCase = app.getFinancialSummaryUseCase,
            exportDataUseCase = app.exportDataUseCase,
            syncDataUseCase = app.syncDataUseCase
        )

        debtsViewModel = DebtsViewModel(
            debtRepository = app.debtRepository,
            accountRepository = app.accountRepository,
            createDebtUseCase = app.createDebtUseCase,
            payDebtInstallmentUseCase = app.payDebtInstallmentUseCase,
            getDebtsSummaryUseCase = app.getDebtsSummaryUseCase,
            deleteDebtUseCase = app.deleteDebtUseCase
        )

        requestPostNotificationsPermission()

        setContent {
            FinanzasTheme {
                MainDashboardScreen(
                    mainViewModel = mainViewModel,
                    debtsViewModel = debtsViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val app = application as FinanzasApp
        mainViewModel.updateServiceAndPermissionState(app.hasOverlayPermission())
    }

    private fun requestPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun MainDashboardScreen(
    mainViewModel: MainViewModel,
    debtsViewModel: DebtsViewModel
) {
    val context = LocalContext.current
    val app = context.applicationContext as FinanzasApp
    val uiState by mainViewModel.uiState.collectAsState()
    val debtsUiState by debtsViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0=Movimientos, 1=Deudas, 2=Informes, 3=Gestionar
    var showInAppQuickModal by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userFeedbackMessage) {
        uiState.userFeedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            mainViewModel.clearFeedbackMessage()
        }
    }

    LaunchedEffect(debtsUiState.feedbackMessage) {
        debtsUiState.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            debtsViewModel.clearFeedback()
        }
    }

    LaunchedEffect(debtsUiState.errorMessage) {
        debtsUiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            debtsViewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        1 -> debtsViewModel.showCreateDialog(true)
                        3 -> mainViewModel.setShowCreateAccountDialog(true)
                        else -> showInAppQuickModal = true
                    }
                },
                containerColor = when (selectedTab) {
                    1 -> Amber500
                    2 -> Cyan500
                    else -> Emerald500
                },
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Título de la App y botón de exportar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Finanzas Express",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimaryDark,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = when (selectedTab) {
                            0 -> "Dashboard & Movimientos"
                            1 -> "Deudas, Cuotas & Roland"
                            2 -> "Informes & Gráficos Estadísticos"
                            else -> "Gestión de Cuentas & Categorías"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (selectedTab) {
                            1 -> Amber500
                            2 -> Cyan500
                            else -> Emerald500
                        }
                    )
                }

                IconButton(
                    onClick = { mainViewModel.setShowExportDialog(true) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariantDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Exportar Respaldo",
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Barra de Navegación de 4 Pestañas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceVariantDark)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 0: Movimientos
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) Emerald500 else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = if (selectedTab == 0) Color.Black else TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Inicio",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) Color.Black else TextSecondaryDark
                        )
                    }
                }

                // Tab 1: Deudas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) Amber500 else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = if (selectedTab == 1) Color.Black else TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Deudas",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) Color.Black else TextSecondaryDark
                        )
                    }
                }

                // Tab 2: Informes
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 2) Cyan500 else Color.Transparent)
                        .clickable { selectedTab = 2 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = if (selectedTab == 2) Color.Black else TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Informes",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 2) Color.Black else TextSecondaryDark
                        )
                    }
                }

                // Tab 3: Gestionar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 3) Emerald500 else Color.Transparent)
                        .clickable { selectedTab = 3 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = if (selectedTab == 3) Color.Black else TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Gestionar",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 3) Color.Black else TextSecondaryDark
                        )
                    }
                }
            }

            // Contenido según Tab seleccionada
            when (selectedTab) {
                0 -> {
                    // Banner de Permiso si no está concedido
                    if (!uiState.hasOverlayPermission) {
                        OverlayPermissionBanner()
                    }

                    // Resumen de Balance Hero Card
                    BalanceSummaryCard(
                        summary = uiState.financialSummary,
                        isFloatingServiceActive = uiState.isFloatingServiceActive,
                        onToggleFloatingService = { enable ->
                            mainViewModel.toggleFloatingService(app, enable)
                        },
                        isSyncing = uiState.isSyncing,
                        onSyncClick = mainViewModel::syncCloudData
                    )

                    // Sección de Cuentas & Tarjetas
                    AccountsSection(
                        accounts = uiState.accounts,
                        onAddAccountClick = { mainViewModel.setShowCreateAccountDialog(true) },
                        onAccountClick = { mainViewModel.selectAccountToEdit(it) },
                        onDeleteAccountClick = mainViewModel::deleteAccount
                    )

                    // Timeline de Movimientos agrupados
                    TransactionTimelineSection(
                        transactions = uiState.transactions,
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = mainViewModel::setFilter,
                        onEditTransaction = { mainViewModel.selectTransactionToEdit(it) },
                        onDeleteTransaction = mainViewModel::deleteTransaction
                    )
                }

                1 -> {
                    // Sección Completa de Deudas & Diferidos
                    DebtsSection(viewModel = debtsViewModel)
                }

                2 -> {
                    // Sección de Informes y Gráficos Estadísticos
                    ReportsSection(
                        transactions = uiState.transactions,
                        accounts = uiState.accounts,
                        usdToCopRate = uiState.usdToCopRate,
                        onTransactionClick = { mainViewModel.selectTransactionToEdit(it) }
                    )
                }

                3 -> {
                    // Sección de Gestión de Cuentas y Categorías
                    ManageSection(
                        accounts = uiState.accounts,
                        categories = uiState.categories,
                        onAddAccountClick = { mainViewModel.setShowCreateAccountDialog(true) },
                        onAccountClick = { mainViewModel.selectAccountToEdit(it) },
                        onDeleteAccountClick = mainViewModel::deleteAccount,
                        onAddCategoryClick = { type -> mainViewModel.setShowCreateCategoryDialog(true, type) },
                        onDeleteCategoryClick = mainViewModel::deleteCategory
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Modal Crear Nueva Cuenta / Banco / Tarjeta
    if (uiState.showCreateAccountDialog) {
        CreateAccountDialog(
            onDismiss = { mainViewModel.setShowCreateAccountDialog(false) },
            onCreateAccount = { name, type, currency, initialBalance, colorHex, isDefault ->
                mainViewModel.createAccount(
                    name = name,
                    type = type,
                    currency = currency,
                    initialBalance = initialBalance,
                    colorHex = colorHex,
                    isDefault = isDefault
                )
            }
        )
    }

    // Modal Editar Cuenta / Banco / Tarjeta
    uiState.selectedAccountToEdit?.let { account ->
        EditAccountDialog(
            account = account,
            movementCount = uiState.selectedAccountMovementCount,
            onDismiss = { mainViewModel.selectAccountToEdit(null) },
            onSaveAccount = { updatedAccount ->
                mainViewModel.updateAccount(updatedAccount)
            }
        )
    }

    // Modal Editar Movimiento de Gasto / Ingreso
    uiState.selectedTransactionToEdit?.let { item ->
        EditTransactionDialog(
            item = item,
            accounts = uiState.accounts,
            categories = uiState.categories,
            onDismiss = { mainViewModel.selectTransactionToEdit(null) },
            onSaveTransaction = { updatedTransaction ->
                mainViewModel.updateTransaction(updatedTransaction)
            }
        )
    }

    // Modal Crear Nueva Categoría
    if (uiState.showCreateCategoryDialog) {
        CreateCategoryDialog(
            initialType = uiState.categoryTypeForCreation,
            onDismiss = { mainViewModel.setShowCreateCategoryDialog(false) },
            onCreateCategory = { name, iconName, type, colorHex ->
                mainViewModel.createCategory(name, iconName, type, colorHex)
            }
        )
    }

    // Modal de Exportación JSON / CSV
    if (uiState.showExportDialog) {
        ExportBackupDialog(
            onDismiss = { mainViewModel.setShowExportDialog(false) },
            onExportJson = { mainViewModel.exportJson(context) },
            onExportCsv = { mainViewModel.exportCsv(context) }
        )
    }

    // Modal In-App para captura rápida
    if (showInAppQuickModal) {
        val quickViewModel = remember {
            QuickMovementViewModel(
                accountRepository = app.accountRepository,
                categoryRepository = app.categoryRepository,
                currencyRepository = app.currencyRepository,
                registerMovementUseCase = app.registerMovementUseCase
            )
        }

        Dialog(
            onDismissRequest = { showInAppQuickModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            QuickMovementOverlayScreen(
                viewModel = quickViewModel,
                onDismiss = { showInAppQuickModal = false }
            )
        }
    }
}