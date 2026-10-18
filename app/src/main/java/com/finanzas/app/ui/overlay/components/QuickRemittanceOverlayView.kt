package com.finanzas.app.ui.overlay.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.core.util.HapticHelper
import com.finanzas.app.domain.model.QuoteSource
import com.finanzas.app.ui.overlay.QuickMovementUiState
import com.finanzas.app.ui.overlay.QuickMovementViewModel
import com.finanzas.app.ui.theme.Amber500
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

/**
 * Calculadora de giro USD -> COP con la cotizacion real de Western Union.
 *
 * El monto tecleado arriba es el TOTAL que sale de la cuenta. El desglose que se
 * muestra es literalmente el que aplica el operador:
 *
 *     (total - comision) * tasa = COP recibidos
 */
@Composable
fun QuickRemittanceOverlayView(
    uiState: QuickMovementUiState,
    viewModel: QuickMovementViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAdjustes by remember { mutableStateOf(false) }

    val quote = uiState.remittanceQuote
    val calibration = quote?.calibration

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // ---------- 1. Cotizacion vigente ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceCardDark)
                .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
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
                            Icons.Default.CurrencyExchange,
                            contentDescription = null,
                            tint = Amber500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Western Union",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = when (quote?.source) {
                                QuoteSource.LIVE -> "Cotización en vivo"
                                QuoteSource.CACHE -> "Cotización reciente"
                                else -> "Sin conexión · última conocida"
                            },
                            fontSize = 9.sp,
                            color = when (quote?.source) {
                                QuoteSource.LIVE -> Emerald400
                                QuoteSource.CACHE -> TextMutedDark
                                else -> Rose500
                            }
                        )
                    }
                }

                IconButton(
                    onClick = {
                        HapticHelper.playHeavyClick(context)
                        viewModel.fetchExchangeRate(forceRefresh = true)
                    },
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariantDark)
                ) {
                    if (uiState.isFetchingRate) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Amber500,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualizar cotización",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceDark)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Tasa aplicada", fontSize = 9.sp, color = TextMutedDark)
                    Text(
                        text = "1 USD = $" + CurrencyFormatter.formatRate(uiState.remittanceRate) + " COP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Amber500
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Tasa de mercado", fontSize = 9.sp, color = TextMutedDark)
                    Text(
                        text = "$" + CurrencyFormatter.formatRate(uiState.midMarketRate),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondaryDark
                    )
                }
            }

            if (uiState.quoteUnavailable) {
                Text(
                    text = "Western Union no cotiza este monto. Se muestra la tasa de mercado.",
                    fontSize = 10.sp,
                    color = Rose500
                )
            }

            // Aviso honesto: el quote es EE.UU. -> Colombia.
            Text(
                text = if (calibration?.isCalibrated == true) {
                    "Referencia EE.UU.→CO ajustada con tus " + calibration.samples +
                            " giro(s) reales (" +
                            CurrencyFormatter.formatPercent(calibration.deviationPercent, 2) + "%)"
                } else {
                    "Referencia EE.UU.→Colombia. Escribe abajo los COP reales de ventanilla y la app aprende tu diferencia."
                },
                fontSize = 9.sp,
                color = if (calibration?.isCalibrated == true) Emerald400 else TextMutedDark
            )
        }

        // ---------- 2. Cuentas ----------
        AccountPicker(
            title = "Debitar de (Cuenta USD)",
            accounts = uiState.availableSourceAccounts,
            selectedAccountId = uiState.selectedSourceAccount?.id,
            onAccountSelected = viewModel::onSourceAccountSelected
        )

        AccountPicker(
            title = "Receptor / Destino en Colombia (COP)",
            accounts = uiState.availableTargetAccounts,
            selectedAccountId = uiState.selectedTargetAccount?.id,
            onAccountSelected = viewModel::onTargetAccountSelected
        )

        // ---------- 3. El calculo, paso a paso ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCardDark)
                .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BreakdownRow(
                label = "Sale de tu cuenta",
                value = CurrencyFormatter.format(uiState.numericAmount, "USD"),
                emphasis = true
            )
            BreakdownRow(
                label = "Comisión Western Union",
                value = "− " + CurrencyFormatter.format(uiState.remittanceFee, "USD"),
                valueColor = Rose500
            )
            BreakdownRow(
                label = "Se convierte",
                value = CurrencyFormatter.format(uiState.convertedUsd, "USD")
            )
            BreakdownRow(
                label = "× tasa " + CurrencyFormatter.formatRate(uiState.remittanceRate),
                value = CurrencyFormatter.format(uiState.estimatedCop, "COP"),
                valueColor = Emerald500,
                emphasis = true
            )
        }

        // ---------- 4. COP reales de ventanilla (calibra) ----------
        OutlinedTextField(
            value = uiState.targetCopInput,
            onValueChange = viewModel::onCopInputChanged,
            label = { Text("COP que recibe (ajusta al valor de ventanilla)", fontSize = 10.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Emerald500,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = Emerald500,
                unfocusedTextColor = Emerald500
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.isCopEditedByUser) {
            Text(
                text = "Tasa real de tu giro: $" + CurrencyFormatter.formatRate(uiState.appliedRate) +
                        " · al registrarlo se usará para afinar las próximas estimaciones",
                fontSize = 9.sp,
                color = Cyan500
            )
        }

        // ---------- 5. Costo real del giro ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Rose500.copy(alpha = 0.10f))
                .border(1.dp, Rose500.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Costo total del giro", fontSize = 10.sp, color = TextSecondaryDark)
                Text(
                    text = "comisión + margen sobre la tasa",
                    fontSize = 9.sp,
                    color = TextMutedDark
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatPercent(uiState.totalCostPercent, 2) + "%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Rose500
                )
                Text(
                    text = CurrencyFormatter.format(uiState.totalCostUsd, "USD"),
                    fontSize = 10.sp,
                    color = TextMutedDark
                )
            }
        }

        // ---------- 6. Comparativa con otros operadores ----------
        val mejor = quote?.bestAlternative
        if (mejor != null && quote.savingsVsBestAlternative > 0.0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Emerald500.copy(alpha = 0.10f))
                    .border(1.dp, Emerald500.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Con " + mejor.displayName + " llegarían",
                        fontSize = 10.sp,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = "+" + CurrencyFormatter.format(quote.savingsVsBestAlternative, "COP"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald400
                    )
                }
                Text(
                    text = "tasa $" + CurrencyFormatter.formatRate(mejor.rate) +
                            "\nfee " + CurrencyFormatter.format(mejor.fee, "USD"),
                    fontSize = 9.sp,
                    color = TextMutedDark
                )
            }
        }

        // ---------- 7. Ajustes manuales ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceVariantDark)
                .clickable { showAdjustes = !showAdjustes }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(14.dp))
                Text(text = "Ajustes manuales", fontSize = 11.sp, color = TextSecondaryDark)
            }
            Text(
                text = if (showAdjustes) "Ocultar" else "Mostrar",
                fontSize = 10.sp,
                color = Amber500
            )
        }

        AnimatedVisibility(
            visible = showAdjustes,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.feeInput,
                    onValueChange = viewModel::onFeeChanged,
                    label = { Text("Comisión real ($ USD)", fontSize = 10.sp) },
                    placeholder = {
                        Text(
                            CurrencyFormatter.format(quote?.effectiveFee ?: 0.0, "USD") + " según cotización",
                            fontSize = 10.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald500,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // El margen manual solo tiene sentido sin cotizacion en vivo.
                if (uiState.quoteIsStale) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Margen estimado bajo la tasa", fontSize = 10.sp, color = TextSecondaryDark)
                        Text(
                            text = "−" + CurrencyFormatter.formatPercent(uiState.spreadPercent, 2) + "%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Amber500
                        )
                    }
                    Slider(
                        value = uiState.spreadPercent.toFloat().coerceIn(0f, 6f),
                        onValueChange = { viewModel.onSpreadChanged(it.toDouble()) },
                        valueRange = 0f..6f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = Amber500,
                            activeTrackColor = Amber500,
                            inactiveTrackColor = BorderDark
                        )
                    )
                }

                if (calibration?.isCalibrated == true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ajuste aprendido: " +
                                    CurrencyFormatter.formatPercent(calibration.deviationPercent, 2) +
                                    "% · " + calibration.samples + " giro(s)",
                            fontSize = 10.sp,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = "Reiniciar",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Rose500,
                            modifier = Modifier.clickable { viewModel.resetCalibration() }
                        )
                    }
                }
            }
        }

        // ---------- 8. Registrar ----------
        Button(
            onClick = {
                viewModel.submitMovement {
                    HapticHelper.playSuccess(context)
                    onDismiss()
                }
            },
            enabled = !uiState.isLoading && uiState.convertedUsd > 0.0,
            colors = ButtonDefaults.buttonColors(
                containerColor = Emerald500,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Registrar Giro (" + CurrencyFormatter.format(uiState.numericAmount, "USD") + ")",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimaryDark,
    emphasis: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (emphasis) 12.sp else 11.sp,
            color = if (emphasis) TextSecondaryDark else TextMutedDark
        )
        Text(
            text = value,
            fontSize = if (emphasis) 14.sp else 12.sp,
            fontWeight = if (emphasis) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}
