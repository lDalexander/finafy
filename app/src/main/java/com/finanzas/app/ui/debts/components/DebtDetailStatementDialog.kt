package com.finanzas.app.ui.debts.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.core.util.DateFormatter
import com.finanzas.app.domain.model.Debt
import com.finanzas.app.domain.model.DebtInstallment
import com.finanzas.app.domain.model.DebtWithInstallments
import com.finanzas.app.ui.theme.Amber500
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald400
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.Rose500
import com.finanzas.app.ui.theme.SurfaceDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark
import kotlin.math.roundToInt

@Composable
fun DebtDetailStatementDialog(
    debtWithInstallments: DebtWithInstallments,
    onDismiss: () -> Unit,
    onPayInstallmentClick: (Debt, DebtInstallment) -> Unit,
    onAddAbonoClick: (DebtWithInstallments) -> Unit,
    onDeleteDebtClick: (String) -> Unit
) {
    val debt = debtWithInstallments.debt
    val installments = debtWithInstallments.installments

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = debt.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Estado de Cuenta • " + debt.entityName +
                                    (if (debtWithInstallments.isFlexible) " (Abonos Libres)" else " (Cuotas Fijas)"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Amber500
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantDark)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMutedDark, modifier = Modifier.size(16.dp))
                    }
                }

                // Tarjeta de Resumen Financiero de la Deuda
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceVariantDark)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Capital Original:", fontSize = 12.sp, color = TextSecondaryDark)
                        Text(
                            text = CurrencyFormatter.format(debt.originalPrincipal, debt.currency),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Inicial Financiado:", fontSize = 12.sp, color = TextSecondaryDark)
                        Text(
                            text = CurrencyFormatter.format(debt.totalWithInterest, debt.currency),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Abonado / Pagado:", fontSize = 12.sp, color = TextSecondaryDark)
                        Text(
                            text = CurrencyFormatter.format(debtWithInstallments.totalPaidOverall, debt.currency),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald400
                        )
                    }
                    if (debtWithInstallments.totalPaidLateFees > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total Mora Pagada:", fontSize = 12.sp, color = TextSecondaryDark)
                            Text(
                                text = CurrencyFormatter.format(debtWithInstallments.totalPaidLateFees, debt.currency),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Rose500
                            )
                        }
                    }

                    // Barra de Progreso
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = { debtWithInstallments.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Emerald500,
                            trackColor = BorderDark
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(debtWithInstallments.progress * 100).roundToInt()}% amortizado",
                                fontSize = 10.sp,
                                color = TextMutedDark
                            )
                            Text(
                                text = if (!CurrencyFormatter.isPositive(debtWithInstallments.remainingTotalDebt)) "¡Deuda Pagada!" else "Pendiente",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!CurrencyFormatter.isPositive(debtWithInstallments.remainingTotalDebt)) Emerald400 else Amber500
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Saldo Total Pendiente:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        Text(
                            text = CurrencyFormatter.format(debtWithInstallments.remainingTotalDebt, debt.currency),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (CurrencyFormatter.isPositive(debtWithInstallments.remainingTotalDebt)) Rose500 else Emerald400
                        )
                    }
                }

                // Botón Acción: Realizar Abono a Deuda
                if (CurrencyFormatter.isPositive(debtWithInstallments.remainingTotalDebt)) {
                    Button(
                        onClick = { onAddAbonoClick(debtWithInstallments) },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Realizar Abono a la Deuda",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Título del Historial / Cuotas
                Text(
                    text = if (debtWithInstallments.isFlexible)
                        "Historial de Abonos (${debtWithInstallments.paidInstallmentsCount} realizados)"
                    else
                        "Calendario de Cuotas (${debtWithInstallments.paidInstallmentsCount} de ${debt.totalInstallments} pagadas)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )

                // Lista de Cuotas / Abonos
                if (installments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceVariantDark)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no se han registrado abonos. Toca 'Realizar Abono' arriba para descontar saldo.",
                            fontSize = 12.sp,
                            color = TextMutedDark,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        installments.forEach { inst ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceVariantDark)
                                    .border(
                                        1.dp,
                                        when {
                                            inst.isPaid -> Emerald500.copy(alpha = 0.3f)
                                            inst.isOverdue -> Rose500.copy(alpha = 0.4f)
                                            else -> BorderDark
                                        },
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Icono + Detalle Cuota
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = when {
                                            inst.isPaid -> Icons.Default.CheckCircle
                                            inst.isOverdue -> Icons.Default.Warning
                                            else -> Icons.Default.Schedule
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            inst.isPaid -> Emerald400
                                            inst.isOverdue -> Rose500
                                            else -> Amber500
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Column {
                                        Text(
                                            text = (if (debtWithInstallments.isFlexible) "Abono #" else "Cuota #") + inst.installmentNumber,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryDark
                                        )
                                        Text(
                                            text = if (inst.isPaid) "Pagado el " + DateFormatter.formatDateOnly(inst.paidDate ?: inst.dueDate)
                                            else "Vence: " + DateFormatter.formatDateOnly(inst.dueDate),
                                            fontSize = 11.sp,
                                            color = if (inst.isOverdue) Rose500 else TextMutedDark
                                        )
                                        if (!inst.notes.isNullOrBlank()) {
                                            Text(
                                                text = inst.notes,
                                                fontSize = 10.sp,
                                                color = TextSecondaryDark
                                            )
                                        }
                                        if (inst.lateFeeAmount > 0) {
                                            Text(
                                                text = "+ Mora: " + CurrencyFormatter.format(inst.lateFeeAmount, debt.currency),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Rose500
                                            )
                                        }
                                    }
                                }

                                // Monto + Botón
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = CurrencyFormatter.format(inst.actualPaidAmount ?: inst.totalDue, debt.currency),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (inst.isPaid) Emerald400 else TextPrimaryDark
                                    )

                                    if (!inst.isPaid) {
                                        Button(
                                            onClick = { onPayInstallmentClick(debt, inst) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Emerald500,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(text = "Pagar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Botón Eliminar Deuda
                OutlinedButton(
                    onClick = { onDeleteDebtClick(debt.id) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose500),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(text = "Eliminar Registro de Deuda", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}