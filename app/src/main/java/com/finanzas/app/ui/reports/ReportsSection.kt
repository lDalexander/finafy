package com.finanzas.app.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzas.app.core.util.AppCategories
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.core.util.MoneyConverter
import com.finanzas.app.core.util.DateFormatter
import com.finanzas.app.domain.model.Account
import com.finanzas.app.domain.model.Category
import com.finanzas.app.domain.model.TransactionType
import com.finanzas.app.domain.model.TransactionWithDetails
import com.finanzas.app.ui.overlay.components.getCategoryIcon
import com.finanzas.app.ui.overlay.components.parseColorHex
import com.finanzas.app.ui.reports.components.BarChart
import com.finanzas.app.ui.reports.components.ChartSlice
import com.finanzas.app.ui.reports.components.DonutChart
import com.finanzas.app.ui.reports.components.MonthlyBarData
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Cyan500
import com.finanzas.app.ui.theme.Emerald400
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.Rose500
import com.finanzas.app.ui.theme.SurfaceCardDark
import com.finanzas.app.ui.theme.SurfaceDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportViewMode(val label: String) {
    BY_CATEGORY("Por Categoría"),
    BY_ACCOUNT("Por Cuenta"),
    BY_MONTH("Por Meses")
}

@Composable
fun ReportsSection(
    transactions: List<TransactionWithDetails>,
    accounts: List<Account>,
    usdToCopRate: Double = 4150.0,
    onTransactionClick: (TransactionWithDetails) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpenseMode by remember { mutableStateOf(true) } // true: Gastos, false: Ingresos
    var viewMode by remember { mutableStateOf(ReportViewMode.BY_CATEGORY) }

    // Filtrar transacciones según el modo seleccionado
    val targetType = if (isExpenseMode) TransactionType.EXPENSE else TransactionType.INCOME
    val relevantTransactions = transactions.filter {
        if (isExpenseMode) {
            // Se excluyen los pagos de deuda por el mismo criterio que el Dashboard:
            // amortizar no es consumo. Siguen visibles en la linea de tiempo.
            (it.transaction.type == TransactionType.EXPENSE ||
                    it.transaction.type == TransactionType.CREDIT_PURCHASE) &&
                    it.transaction.categoryId != AppCategories.DEBT_PAYMENT
        } else {
            it.transaction.type == TransactionType.INCOME
        }
    }

    // INV-3: nunca sumar montos de monedas distintas sin normalizar.
    val totalAmount = relevantTransactions.sumOf { it.amountInUsd(usdToCopRate) }
    val primaryColor = if (isExpenseMode) Rose500 else Emerald500

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selector Principal: [Gastos] vs [Ingresos]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isExpenseMode) Rose500 else Color.Transparent)
                    .clickable { isExpenseMode = true }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📉 Gastos",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpenseMode) Color.White else TextSecondaryDark
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!isExpenseMode) Emerald500 else Color.Transparent)
                    .clickable { isExpenseMode = false }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📈 Ingresos",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isExpenseMode) Color.Black else TextSecondaryDark
                )
            }
        }

        // Sub-tabs: Por Categoría | Por Cuenta | Por Meses
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReportViewMode.values().forEach { mode ->
                val isSelected = viewMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) primaryColor.copy(alpha = 0.2f) else SurfaceCardDark)
                        .border(1.dp, if (isSelected) primaryColor else BorderDark, RoundedCornerShape(10.dp))
                        .clickable { viewMode = mode }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) primaryColor else TextSecondaryDark
                    )
                }
            }
        }

        // Renderizado del Gráfico según la opción
        when (viewMode) {
            ReportViewMode.BY_CATEGORY -> {
                // Agrupado por Categoría
                val categoryGroups = relevantTransactions.groupBy { it.category?.name ?: "Sin categoría" }
                val defaultColors = listOf(
                    "#10B981", "#3B82F6", "#F59E0B", "#EC4899", "#8B5CF6", "#06B6D4", "#F97316", "#64748B"
                )
                var colorIndex = 0

                val slices = categoryGroups.map { (catName, txList) ->
                    val sum = txList.sumOf { it.amountInUsd(usdToCopRate) }
                    val catColorHex = txList.firstOrNull()?.category?.colorHex
                    val sliceColor = if (catColorHex != null) {
                        parseColorHex(catColorHex)
                    } else {
                        parseColorHex(defaultColors[colorIndex % defaultColors.size]).also { colorIndex++ }
                    }
                    ChartSlice(label = catName, value = sum, color = sliceColor)
                }.sortedByDescending { it.value }

                DonutChart(
                    slices = slices,
                    totalAmount = totalAmount,
                    centerTitle = if (isExpenseMode) "Total Gastos" else "Total Ingresos"
                )
            }

            ReportViewMode.BY_ACCOUNT -> {
                // Agrupado por Cuenta (Origen en gastos, Destino/Origen en ingresos)
                val accountGroups = relevantTransactions.groupBy {
                    it.sourceAccount?.name ?: it.targetAccount?.name ?: "Cuenta general"
                }

                val slices = accountGroups.map { (accName, txList) ->
                    val sum = txList.sumOf { it.amountInUsd(usdToCopRate) }
                    val accColor = txList.firstOrNull()?.sourceAccount?.colorHex
                        ?: txList.firstOrNull()?.targetAccount?.colorHex
                    ChartSlice(
                        label = accName,
                        value = sum,
                        color = parseColorHex(accColor ?: "#3B82F6", primaryColor)
                    )
                }.sortedByDescending { it.value }

                DonutChart(
                    slices = slices,
                    totalAmount = totalAmount,
                    centerTitle = if (isExpenseMode) "Por Cuentas" else "Por Cuentas"
                )
            }

            ReportViewMode.BY_MONTH -> {
                // Agrupado por Mes (Últimos 6 meses)
                val monthFormat = SimpleDateFormat("MMM", Locale("es", "ES"))
                val calendar = Calendar.getInstance()
                val monthlyData = mutableListOf<MonthlyBarData>()

                for (i in 5 downTo 0) {
                    val cal = Calendar.getInstance().apply {
                        add(Calendar.MONTH, -i)
                    }
                    val targetYear = cal.get(Calendar.YEAR)
                    val targetMonth = cal.get(Calendar.MONTH)
                    val label = monthFormat.format(cal.time).replaceFirstChar { it.uppercase() }

                    val monthTotal = relevantTransactions.filter {
                        val itemCal = Calendar.getInstance().apply { timeInMillis = it.transaction.timestamp }
                        itemCal.get(Calendar.YEAR) == targetYear && itemCal.get(Calendar.MONTH) == targetMonth
                    }.sumOf { it.amountInUsd(usdToCopRate) }

                    monthlyData.add(MonthlyBarData(monthLabel = label, amount = monthTotal))
                }

                BarChart(
                    data = monthlyData,
                    barColor = primaryColor
                )
            }
        }

        // Lista Detallada de Movimientos Relevantes
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Detalle de " + (if (isExpenseMode) "Gastos" else "Ingresos") + " (${relevantTransactions.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )

            if (relevantTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCardDark)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay registros para este informe",
                        fontSize = 13.sp,
                        color = TextMutedDark
                    )
                }
            } else {
                relevantTransactions.take(30).forEach { item ->
                    val tx = item.transaction
                    val cat = item.category
                    val categoryColor = parseColorHex(cat?.colorHex ?: "#64748B", primaryColor)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCardDark)
                            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                            .clickable { onTransactionClick(item) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(cat?.iconName ?: "category"),
                                    contentDescription = null,
                                    tint = categoryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = cat?.name ?: "Sin categoría",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = DateFormatter.formatDateOnly(tx.timestamp) + " • " +
                                            (item.sourceAccount?.name ?: item.targetAccount?.name ?: "Sin cuenta"),
                                    fontSize = 10.sp,
                                    color = TextMutedDark
                                )
                            }
                        }

                        Text(
                            text = (if (isExpenseMode) "-" else "+") +
                                CurrencyFormatter.format(tx.amount + tx.feeAmount, item.sourceAccount?.currency ?: "USD"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Monto del movimiento (incluida la comision) expresado en USD.
 * La moneda del movimiento es la de la cuenta que lo origina o lo recibe.
 */
private fun TransactionWithDetails.amountInUsd(usdToCopRate: Double): Double =
    MoneyConverter.toBase(
        transaction.amount + transaction.feeAmount,
        sourceAccount?.currency ?: targetAccount?.currency,
        usdToCopRate
    )
