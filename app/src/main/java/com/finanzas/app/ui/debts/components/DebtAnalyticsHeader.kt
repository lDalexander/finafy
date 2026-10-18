package com.finanzas.app.ui.debts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.domain.model.DebtAnalyticsSummary
import com.finanzas.app.ui.theme.Amber500
import com.finanzas.app.ui.theme.BackgroundDark
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald400
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.Rose500
import com.finanzas.app.ui.theme.SurfaceCardDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

@Composable
fun DebtAnalyticsHeader(
    summary: DebtAnalyticsSummary,
    onNewDebtClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(SurfaceCardDark, BackgroundDark)
                )
            )
            .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Encabezado + Botón Nueva Deuda
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Total Deuda Pendiente",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondaryDark
                )
                Text(
                    text = CurrencyFormatter.format(summary.totalRemainingDebt, "USD"),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (CurrencyFormatter.isPositive(summary.totalRemainingDebt)) Rose500 else Emerald400,
                    letterSpacing = (-0.5).sp
                )
            }

            Button(
                onClick = onNewDebtClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber500,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(text = "Nueva Deuda", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Sub-Métricas: Intereses Pagados vs Mora Pagada
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Intereses Pagados
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceVariantDark)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Amber500.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Percent,
                        contentDescription = null,
                        tint = Amber500,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(text = "Interés Pagado", fontSize = 11.sp, color = TextMutedDark)
                    Text(
                        text = CurrencyFormatter.format(summary.totalPaidInterest, "USD"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Amber500
                    )
                }
            }

            // Mora / Recargos Pagados
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceVariantDark)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Rose500.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Rose500,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(text = "Mora Pagada", fontSize = 11.sp, color = TextMutedDark)
                    Text(
                        text = CurrencyFormatter.format(summary.totalPaidLateFees, "USD"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Rose500
                    )
                }
            }
        }

        // Resumen de Amortización Capital
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceVariantDark)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Capital Amortizado:",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }
            Text(
                text = CurrencyFormatter.format(summary.totalPaidPrincipal, "USD") + " / " +
                        CurrencyFormatter.format(summary.totalOriginalPrincipal, "USD"),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )
        }
    }
}
