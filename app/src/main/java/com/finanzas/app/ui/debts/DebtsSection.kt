package com.finanzas.app.ui.debts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzas.app.ui.debts.components.AddDebtAbonoDialog
import com.finanzas.app.ui.debts.components.CreateDebtDialog
import com.finanzas.app.ui.debts.components.DebtAnalyticsHeader
import com.finanzas.app.ui.debts.components.DebtCard
import com.finanzas.app.ui.debts.components.DebtDetailStatementDialog
import com.finanzas.app.ui.debts.components.PayInstallmentDialog
import com.finanzas.app.ui.theme.Amber500
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.SurfaceCardDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

@Composable
fun DebtsSection(
    viewModel: DebtsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado de Métricas Globales de Deuda
        DebtAnalyticsHeader(
            summary = uiState.analyticsSummary,
            onNewDebtClick = { viewModel.showCreateDialog(true) }
        )

        // Título de Sección
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Deudas & Diferidos Activos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Text(
                text = "${uiState.debtsWithInstallments.size} registros",
                fontSize = 12.sp,
                color = TextMutedDark
            )
        }

        // Lista de Deudas o Estado Vacío
        if (uiState.debtsWithInstallments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceCardDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(18.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Amber500.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = Amber500,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Text(
                        text = "No tienes deudas registradas",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )

                    Text(
                        text = "Registra compras a cuotas, diferidos, créditos o préstamos abiertos para controlar tus abonos, intereses y moras pagadas.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = { viewModel.showCreateDialog(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Amber500,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(text = "Registrar Primera Deuda", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.debtsWithInstallments.forEach { item ->
                    DebtCard(
                        debtWithInstallments = item,
                        onCardClick = { viewModel.selectDebtForDetail(item) },
                        onPayNextInstallmentClick = { debt, installment ->
                            viewModel.selectInstallmentForPayment(debt, installment)
                        }
                    )
                }
            }
        }
    }

    // Modal Crear Deuda
    if (uiState.showCreateDebtDialog) {
        CreateDebtDialog(
            onDismiss = { viewModel.showCreateDialog(false) },
            onCreateDebt = { title, entityName, principal, interestPercent, installments, dueDay, currency, notes ->
                viewModel.createDebt(
                    title = title,
                    entityName = entityName,
                    originalPrincipal = principal,
                    interestRatePercentage = interestPercent,
                    totalInstallments = installments,
                    dueDayOfMonth = dueDay,
                    currency = currency,
                    notes = notes
                )
            }
        )
    }

    // Modal Estado de Cuenta Detallado
    uiState.selectedDebtForDetail?.let { detail ->
        DebtDetailStatementDialog(
            debtWithInstallments = detail,
            onDismiss = { viewModel.selectDebtForDetail(null) },
            onPayInstallmentClick = { debt, installment ->
                viewModel.selectInstallmentForPayment(debt, installment)
            },
            onAddAbonoClick = { debtWithInst ->
                viewModel.selectDebtForAbono(debtWithInst)
            },
            onDeleteDebtClick = { debtId ->
                viewModel.deleteDebt(debtId)
            }
        )
    }

    // Modal Realizar Abono a Deuda
    uiState.selectedDebtForAbono?.let { debtWithInst ->
        AddDebtAbonoDialog(
            debtWithInstallments = debtWithInst,
            // Una deuda en COP se paga con una cuenta en COP: no hay conversion aqui.
            accounts = uiState.accounts.filter { it.currency == debtWithInst.debt.currency },
            onDismiss = { viewModel.selectDebtForAbono(null) },
            onConfirmAbono = { debtId, amount, sourceAccountId, notes ->
                viewModel.addAbono(debtId, amount, sourceAccountId, notes)
            }
        )
    }

    // Modal Pagar Cuota
    uiState.selectedInstallmentForPayment?.let { (debt, installment) ->
        PayInstallmentDialog(
            debt = debt,
            installment = installment,
            accounts = uiState.accounts.filter { it.currency == debt.currency },
            onDismiss = { viewModel.selectInstallmentForPayment(null, null) },
            onConfirmPayment = { installmentId, actualAmount, lateFee, sourceAccountId, notes ->
                viewModel.payInstallment(
                    installmentId = installmentId,
                    actualAmountPaid = actualAmount,
                    lateFee = lateFee,
                    sourceAccountId = sourceAccountId,
                    notes = notes
                )
            }
        )
    }
}