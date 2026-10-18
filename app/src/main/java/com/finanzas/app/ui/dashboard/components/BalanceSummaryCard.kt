package com.finanzas.app.ui.dashboard.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.finanzas.app.domain.model.FinancialSummary
import com.finanzas.app.ui.theme.Amber500
import com.finanzas.app.ui.theme.BackgroundDark
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

@Composable
fun BalanceSummaryCard(
    summary: FinancialSummary,
    isFloatingServiceActive: Boolean,
    onToggleFloatingService: (Boolean) -> Unit,
    isSyncing: Boolean,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SurfaceCardDark,
                        BackgroundDark
                    )
                )
            )
            .border(1.dp, BorderDark, RoundedCornerShape(26.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Línea 1: Encabezado "Saldo Total" + Botón Sincronización
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Emerald500)
                )
                Text(
                    text = "Saldo Total",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondaryDark,
                    letterSpacing = 0.5.sp
                )
            }

            IconButton(
                onClick = onSyncClick,
                enabled = !isSyncing,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariantDark)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Cyan500
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Sincronizar",
                        tint = Cyan500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Línea 2: El Valor del Saldo Total en grande
        Text(
            text = CurrencyFormatter.format(summary.totalLiquidAssets, "USD"),
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimaryDark,
            letterSpacing = (-0.5).sp
        )

        HorizontalDivider(color = BorderDark.copy(alpha = 0.6f), thickness = 1.dp)

        // Líneas 3, 4, 5, 6: Métricas estructuradas en orden
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Línea 3: Flecha Verde hacia arriba + Ingresos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantDark.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Emerald500.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Ingresos",
                            tint = Emerald500,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "Ingresos",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondaryDark
                    )
                }
                Text(
                    text = "+" + CurrencyFormatter.format(summary.totalIncome, "USD"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Emerald400
                )
            }

            // Línea 4: Flecha Roja hacia abajo + Gastos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantDark.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Rose500.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Gastos",
                            tint = Rose500,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "Gastos",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondaryDark
                    )
                }
                Text(
                    text = "-" + CurrencyFormatter.format(summary.totalExpenses, "USD"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Rose500
                )
            }

            // Línea 5: Deudas (Total diferidos + tarjetas)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantDark.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Amber500.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "Deudas",
                            tint = Amber500,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "Deudas & Tarjetas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondaryDark
                    )
                }
                Text(
                    text = "-" + CurrencyFormatter.format(summary.totalDebts, "USD"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Amber500
                )
            }

            // Línea 6: Balance Neto (Ingresos - Gastos)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (summary.netBalance >= 0) Emerald500.copy(alpha = 0.12f)
                        else Rose500.copy(alpha = 0.12f)
                    )
                    .border(
                        1.dp,
                        if (summary.netBalance >= 0) Emerald500.copy(alpha = 0.3f)
                        else Rose500.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                if (summary.netBalance >= 0) Emerald500.copy(alpha = 0.25f)
                                else Rose500.copy(alpha = 0.25f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = "Balance",
                            tint = if (summary.netBalance >= 0) Emerald400 else Rose500,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "Balance Neto",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryDark
                    )
                }
                Text(
                    text = if (summary.netBalance >= 0)
                        "+" + CurrencyFormatter.format(summary.netBalance, "USD")
                    else
                        CurrencyFormatter.format(summary.netBalance, "USD"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = if (summary.netBalance >= 0) Emerald400 else Rose500
                )
            }
        }

        // Switch de activación del Botón Flotante AssistiveTouch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (isFloatingServiceActive) Emerald500.copy(alpha = 0.12f) else SurfaceVariantDark)
                .border(
                    1.dp,
                    if (isFloatingServiceActive) Emerald500.copy(alpha = 0.4f) else BorderDark,
                    RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = if (isFloatingServiceActive) Emerald500 else TextSecondaryDark,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Botón Flotante",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isFloatingServiceActive) TextPrimaryDark else TextSecondaryDark
                    )
                    Text(
                        text = if (isFloatingServiceActive) "Activo en pantalla" else "Toca para habilitar",
                        fontSize = 10.sp,
                        color = if (isFloatingServiceActive) Emerald400 else TextMutedDark
                    )
                }
            }

            Switch(
                checked = isFloatingServiceActive,
                onCheckedChange = onToggleFloatingService,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Emerald500,
                    checkedTrackColor = Emerald500.copy(alpha = 0.35f),
                    uncheckedThumbColor = TextMutedDark,
                    uncheckedTrackColor = SurfaceCardDark
                )
            )
        }
    }
}