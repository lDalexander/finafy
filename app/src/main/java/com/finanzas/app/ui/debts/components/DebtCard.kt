package com.finanzas.app.ui.debts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.core.util.DateFormatter
import com.finanzas.app.domain.model.Debt
import com.finanzas.app.domain.model.DebtInstallment
import com.finanzas.app.domain.model.DebtStatus
import com.finanzas.app.domain.model.DebtWithInstallments
import com.finanzas.app.ui.theme.Amber500
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald400
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.Rose500
import com.finanzas.app.ui.theme.SurfaceCardDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark
import kotlin.math.roundToInt

@Composable
fun DebtCard(
    debtWithInstallments: DebtWithInstallments,
    onCardClick: () -> Unit,
    onPayNextInstallmentClick: (Debt, DebtInstallment) -> Unit,
    modifier: Modifier = Modifier
) {
    val debt = debtWithInstallments.debt
    val isPaid = debt.status == DebtStatus.PAID
    val nextInstallment = debtWithInstallments.nextPendingInstallment

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCardDark)
            .border(1.dp, if (isPaid) Emerald500.copy(alpha = 0.3f) else BorderDark, RoundedCornerShape(18.dp))
            .clickable { onCardClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Fila 1: Entidad + Badge de Estado
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isPaid) Emerald500.copy(alpha = 0.2f) else Amber500.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.Storefront,
                        contentDescription = null,
                        tint = if (isPaid) Emerald400 else Amber500,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = debt.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Entidad: ${debt.entityName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMutedDark
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isPaid -> Emerald500.copy(alpha = 0.15f)
                            nextInstallment?.isOverdue == true -> Rose500.copy(alpha = 0.15f)
                            else -> Amber500.copy(alpha = 0.15f)
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when {
                        isPaid -> "PAGADA"
                        nextInstallment?.isOverdue == true -> "EN MORA"
                        else -> "ACTIVA"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = when {
                        isPaid -> Emerald400
                        nextInstallment?.isOverdue == true -> Rose500
                        else -> Amber500
                    }
                )
            }
        }

        // Fila 2: Montos (Saldo Restante vs Total Financiado)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = if (isPaid) "Total Pagado" else "Saldo Pendiente",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
                Text(
                    text = CurrencyFormatter.format(
                        if (isPaid) debtWithInstallments.totalPaidOverall else debtWithInstallments.remainingTotalDebt,
                        debt.currency
                    ),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isPaid) Emerald400 else Rose500
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Capital: ${CurrencyFormatter.format(debt.originalPrincipal, debt.currency)}",
                    fontSize = 11.sp,
                    color = TextMutedDark
                )
                if (debt.interestRatePercentage > 0) {
                    Text(
                        text = "Interés: ${CurrencyFormatter.formatPercent(debt.interestRatePercentage)}% (Total: ${CurrencyFormatter.format(debt.totalWithInterest, debt.currency)})",
                        fontSize = 11.sp,
                        color = Amber500
                    )
                }
            }
        }

        // Barra de Progreso de Cuotas
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (debtWithInstallments.isFlexible)
                        "Abonos realizados: ${debtWithInstallments.paidInstallmentsCount}"
                    else
                        "Progreso de Cuotas: ${debtWithInstallments.paidInstallmentsCount} de ${debt.totalInstallments}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondaryDark
                )
                Text(
                    text = "${(debtWithInstallments.progress * 100).roundToInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPaid) Emerald400 else Amber500
                )
            }
            LinearProgressIndicator(
                progress = { debtWithInstallments.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isPaid) Emerald500 else Amber500,
                trackColor = SurfaceVariantDark
            )
        }

        // Fila 3: Próxima Cuota & Botones de Acción
        if (!isPaid && nextInstallment != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantDark)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = if (nextInstallment.isOverdue) Rose500 else TextSecondaryDark,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Cuota #${nextInstallment.installmentNumber} (${DateFormatter.formatDateOnly(nextInstallment.dueDate)})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (nextInstallment.isOverdue) Rose500 else TextPrimaryDark
                        )
                    }
                    Text(
                        text = "Valor: ${CurrencyFormatter.format(nextInstallment.totalDue, debt.currency)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Amber500
                    )
                }

                Button(
                    onClick = { onPayNextInstallmentClick(debt, nextInstallment) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald500,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text(text = "Pagar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
