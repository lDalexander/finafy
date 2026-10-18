package com.finanzas.app.domain.model

/**
 * Resumen financiero consolidado con balance global, liquidez, ingresos, gastos y pasivos.
 */
data class FinancialSummary(
    val totalLiquidAssets: Double = 0.0, // Saldo Total disponible
    val totalIncome: Double = 0.0,       // Total Ingresos acumulados
    val totalExpenses: Double = 0.0,     // Total Gastos acumulados
    val totalCreditDebt: Double = 0.0,   // Deuda en Tarjetas de Crédito
    val totalStandaloneDebts: Double = 0.0, // Deudas y compras diferidas activas (Roland, créditos)
    val totalDebts: Double = 0.0,        // Total deudas consolidadas (Tarjetas + Diferidos)
    val netBalance: Double = 0.0,        // Balance (Ingresos - Gastos)
    val netWorth: Double = 0.0,          // Patrimonio Neto (Saldo Total - Deudas)
    val totalAccountsCount: Int = 0,
    val recentMovementsCount: Int = 0
)
