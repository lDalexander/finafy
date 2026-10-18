package com.finanzas.app.ui.overlay.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Cyan500
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.SurfaceCardDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

@Composable
fun RemittanceForm(
    amount: Double,
    feeInput: String,
    onFeeChanged: (String) -> Unit,
    targetCurrency: String,
    onTargetCurrencyChanged: (String) -> Unit,
    exchangeRateInput: String,
    onExchangeRateChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val rate = exchangeRateInput.toDoubleOrNull() ?: 4120.0
    val fee = feeInput.toDoubleOrNull() ?: 0.0
    val calculatedTargetAmount = amount * rate
    val totalCost = amount + fee

    val currencies = listOf("COP", "MXN", "EUR", "PEN", "ARS")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardDark)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Monedas destino
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Moneda Destino:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondaryDark
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currencies.forEach { curr ->
                    val isSelected = curr == targetCurrency
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Emerald500.copy(alpha = 0.25f) else SurfaceVariantDark)
                            .border(1.dp, if (isSelected) Emerald500 else BorderDark, RoundedCornerShape(8.dp))
                            .clickable { onTargetCurrencyChanged(curr) }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = curr,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Emerald500 else TextSecondaryDark
                        )
                    }
                }
            }
        }

        // Inputs: Comisión y Tasa de Cambio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = feeInput,
                onValueChange = onFeeChanged,
                label = { Text("Comisión ($)", fontSize = 10.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                ),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = exchangeRateInput,
                onValueChange = onExchangeRateChanged,
                label = { Text("Tasa ($targetCurrency)", fontSize = 10.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan500,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                ),
                modifier = Modifier.weight(1.2f)
            )
        }

        // Tarjeta de Cálculo en Vivo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceVariantDark)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Total a debitar (Monto + Fee):",
                    fontSize = 10.sp,
                    color = TextMutedDark
                )
                Text(
                    text = CurrencyFormatter.format(totalCost, "USD"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Recibe en destino ($targetCurrency):",
                    fontSize = 10.sp,
                    color = TextMutedDark
                )
                Text(
                    text = CurrencyFormatter.format(calculatedTargetAmount, targetCurrency),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Emerald500
                )
            }
        }
    }
}
