package com.finanzas.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.finanzas.app.data.local.database.AppDatabase
import com.finanzas.app.data.remote.FakeRemoteSyncDataSource
import com.finanzas.app.data.remote.RemoteSyncDataSource
import com.finanzas.app.data.repository.AccountRepositoryImpl
import com.finanzas.app.data.repository.CategoryRepositoryImpl
import com.finanzas.app.data.repository.TransactionRepositoryImpl
import com.finanzas.app.domain.repository.AccountRepository
import com.finanzas.app.domain.repository.CategoryRepository
import com.finanzas.app.domain.repository.TransactionRepository
import com.finanzas.app.domain.usecase.ExportDataUseCase
import com.finanzas.app.domain.usecase.GetFinancialSummaryUseCase
import com.finanzas.app.domain.usecase.RegisterMovementUseCase
import com.finanzas.app.domain.usecase.SyncDataUseCase
import com.finanzas.app.service.FloatingMovementService

class FinanzasApp : Application() {

    companion object {
        const val OVERLAY_CHANNEL_ID = "channel_floating_service"
        lateinit var instance: FinanzasApp
            private set
    }

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val accountRepository: AccountRepository by lazy {
        AccountRepositoryImpl(database.accountDao())
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(database.categoryDao())
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImpl(
            transactionDao = database.transactionDao(),
            accountDao = database.accountDao(),
            categoryDao = database.categoryDao()
        )
    }

    val remoteSyncDataSource: RemoteSyncDataSource by lazy {
        FakeRemoteSyncDataSource()
    }

    val exchangeRateApi: com.finanzas.app.data.remote.api.ExchangeRateApi by lazy {
        com.finanzas.app.data.remote.api.ExchangeRateApi.create()
    }

    val wiseComparisonApi: com.finanzas.app.data.remote.api.WiseComparisonApi by lazy {
        com.finanzas.app.data.remote.api.WiseComparisonApi.create()
    }

    val currencyRepository: com.finanzas.app.domain.repository.CurrencyRepository by lazy {
        com.finanzas.app.data.repository.CurrencyRepositoryImpl(this, exchangeRateApi, wiseComparisonApi)
    }

    val registerMovementUseCase: RegisterMovementUseCase by lazy {
        RegisterMovementUseCase(transactionRepository, accountRepository)
    }

    val getFinancialSummaryUseCase: GetFinancialSummaryUseCase by lazy {
        GetFinancialSummaryUseCase(accountRepository, transactionRepository, debtRepository, currencyRepository)
    }

    val exportDataUseCase: ExportDataUseCase by lazy {
        ExportDataUseCase(accountRepository, transactionRepository)
    }

    val debtRepository: com.finanzas.app.domain.repository.DebtRepository by lazy {
        com.finanzas.app.data.repository.DebtRepositoryImpl(
            debtDao = database.debtDao(),
            accountDao = database.accountDao(),
            transactionDao = database.transactionDao()
        )
    }

    val createDebtUseCase: com.finanzas.app.domain.usecase.CreateDebtUseCase by lazy {
        com.finanzas.app.domain.usecase.CreateDebtUseCase(debtRepository)
    }

    val payDebtInstallmentUseCase: com.finanzas.app.domain.usecase.PayDebtInstallmentUseCase by lazy {
        com.finanzas.app.domain.usecase.PayDebtInstallmentUseCase(debtRepository)
    }

    val getDebtsSummaryUseCase: com.finanzas.app.domain.usecase.GetDebtsSummaryUseCase by lazy {
        com.finanzas.app.domain.usecase.GetDebtsSummaryUseCase(debtRepository, currencyRepository)
    }

    val deleteDebtUseCase: com.finanzas.app.domain.usecase.DeleteDebtUseCase by lazy {
        com.finanzas.app.domain.usecase.DeleteDebtUseCase(debtRepository)
    }

    val syncDataUseCase: SyncDataUseCase by lazy {
        SyncDataUseCase(transactionRepository, accountRepository, remoteSyncDataSource)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.overlay_service_channel_name)
            val descriptionText = getString(R.string.overlay_service_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(OVERLAY_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    fun startFloatingService() {
        if (hasOverlayPermission()) {
            val intent = Intent(this, FloatingMovementService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }

    fun stopFloatingService() {
        val intent = Intent(this, FloatingMovementService::class.java)
        stopService(intent)
    }
}
