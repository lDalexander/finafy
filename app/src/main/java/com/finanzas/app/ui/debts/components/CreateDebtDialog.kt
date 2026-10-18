package com.finanzas.app.ui.debts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.core.util.RateMath
import com.finanzas.app.ui.theme.Amber500
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald400
import com.finanzas.app.ui.theme.SurfaceDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateDebtDialog(
    onDismiss: () -> Unit,
    onCreateDebt: (
        title: String,
        entityName: String,
        principal: Double,
        interestPercent: Double,
        installments: Int,
        dueDay: Int,
        currency: String,
        notes: String?
    ) -> Unit
) {
    var entityName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var principalText by remember { mutableStateOf("") }
    var interestPercentText by remember { mutableStateOf("0") }
    var isFixedInstallments by remember { mutableStateOf(false) } // Default: Abonos Libres
    var installmentsText by remember { mutableStateOf("6") }
    var dueDayText by remember { mutableStateOf("15") }
    // Una deuda vive en una sola moneda: la del pais donde se debe.
    var selectedCurrency by remember { mutableStateOf("USD") }
    var notes by remember { mutableStateOf("") }

    val principal = principalText.toDoubleOrNull() ?: 0.0
    val interestPercent = interestPercentText.toDoubleOrNull() ?: 0.0
    val installments = if (isFixedInstallments) (installmentsText.toIntOrNull() ?: 6) else 0
    val dueDay = (dueDayText.toIntOrNull() ?: 15).coerceIn(1, 31)

    val totalInterest = principal * (interestPercent / 100.0)
    val monedaLabel = if (selectedCurrency == "COP") "COP" else "USD"
    val totalWithInterest = principal + totalInterest
    val installmentAmount = if (installments > 0) totalWithInterest / installments else 0.0

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
                            text = "Registrar Deuda / Diferido",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Abonos libres o cuotas fijas diferidas",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
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

                // Modalidad de Pago: Abonos Libres vs Cuotas Fijas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariantDark)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isFixedInstallments) Amber500 else Color.Transparent)
                            .clickable { isFixedInstallments = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💸 Abonos Libres",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isFixedInstallments) Color.Black else TextSecondaryDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isFixedInstallments) Amber500 else Color.Transparent)
                            .clickable { isFixedInstallments = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔢 En Cuotas Fijas",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFixedInstallments) Color.Black else TextSecondaryDark
                        )
                    }
                }

                OutlinedTextField(
                    value = entityName,
                    onValueChange = { entityName = it },
                    label = { Text("Entidad, Banco o Comercio") },
                    placeholder = { Text("Ej: Roland, Banco, Tarjeta, Amigo") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = Amber500,
                        unfocusedBorderColor = BorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Concepto o Motivo") },
                    placeholder = { Text("Ej: Compra Ropa, Préstamo, Teclado") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = Amber500,
                        unfocusedBorderColor = BorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Moneda de la deuda. Una deuda vive en la moneda del pais donde se
                // debe: la MasterCard del Banco Nu se debe en COP y se paga en COP.
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Moneda de la deuda:", fontSize = 12.sp, color = TextSecondaryDark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            "USD" to "USD · Ecuador",
                            "COP" to "COP · Colombia"
                        ).forEach { (code, label) ->
                            val isSelected = selectedCurrency == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Amber500.copy(alpha = 0.2f) else SurfaceVariantDark)
                                    .border(
                                        1.dp,
                                        if (isSelected) Amber500 else BorderDark,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedCurrency = code }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Amber500 else TextSecondaryDark
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = principalText,
                    onValueChange = { principalText = it },
                    label = { Text("Monto Original / Deuda Total ($monedaLabel)") },
                    placeholder = { Text("200.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = Amber500,
                        unfocusedBorderColor = BorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Si es con cuotas fijas, mostrar selector de cuotas
                if (isFixedInstallments) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Número de Cuotas:", fontSize = 12.sp, color = TextSecondaryDark)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(3, 6, 12, 18, 24, 36).forEach { count ->
                                val isSelected = installments == count
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Amber500 else SurfaceVariantDark)
                                        .clickable { installmentsText = count.toString() }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "$count cuotas",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else TextSecondaryDark
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = interestPercentText,
                        onValueChange = { interestPercentText = it },
                        label = { Text("% Recargo total (plano, no anual)") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = BorderDark
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = dueDayText,
                        onValueChange = { dueDayText = it },
                        label = { Text("Día Límite de Pago") },
                        placeholder = { Text("15") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = Amber500,
                            unfocusedBorderColor = BorderDark
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (principal > 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceVariantDark)
                            .border(1.dp, Amber500.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Amber500, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Resumen de la Deuda",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber500
                            )
                        }
                        if (isFixedInstallments) {
                            Text(
                                text = "• Pagarás $installments cuotas de " + CurrencyFormatter.format(installmentAmount, selectedCurrency),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryDark
                            )
                        } else {
                            Text(
                                text = "• Modalidad: Abonos libres a capital cuando puedas.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryDark
                            )
                        }
                        Text(
                            text = "• Deuda total inicial: " + CurrencyFormatter.format(totalWithInterest, selectedCurrency),
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                        if (isFixedInstallments && totalInterest > 0 && installments > 0) {
                            // El "%" que se escribe arriba es un recargo plano sobre el
                            // capital. Esta linea muestra lo que ese recargo cuesta de verdad.
                            val tea = RateMath.effectiveAnnualRate(principal, installmentAmount, installments)
                            Text(
                                text = "• Costo real del crédito: " +
                                        CurrencyFormatter.formatPercent(tea * 100.0, 2) + "% efectivo anual",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber500
                            )
                        }
                        if (totalInterest > 0) {
                            Text(
                                text = "• Intereses adicionales: " + CurrencyFormatter.format(totalInterest, selectedCurrency),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber500
                            )
                        } else {
                            Text(
                                text = "• 0% intereses (Sin recargo)",
                                fontSize = 12.sp,
                                color = Emerald400
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val fallbackName = if (entityName.isNotBlank()) entityName else "Comercio"
                        val finalTitle = if (title.isNotBlank()) title else "Deuda en $fallbackName"
                        onCreateDebt(
                            finalTitle,
                            fallbackName,
                            principal,
                            interestPercent,
                            installments,
                            dueDay,
                            selectedCurrency,
                            if (notes.isNotBlank()) notes else null
                        )
                    },
                    enabled = principal > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber500,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (isFixedInstallments) "Guardar y Generar Cuotas" else "Guardar Deuda con Abonos Libres",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}