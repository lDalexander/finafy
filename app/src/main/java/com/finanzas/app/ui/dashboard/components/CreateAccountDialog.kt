package com.finanzas.app.ui.dashboard.components

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.finanzas.app.domain.model.AccountType
import com.finanzas.app.ui.overlay.components.getAccountTypeIcon
import com.finanzas.app.ui.overlay.components.parseColorHex
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.SurfaceDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateAccountDialog(
    onDismiss: () -> Unit,
    onCreateAccount: (
        name: String,
        type: AccountType,
        currency: String,
        initialBalance: Double,
        colorHex: String,
        isDefault: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.BANK_SAVINGS) }
    var currency by remember { mutableStateOf("USD") }
    var initialBalanceText by remember { mutableStateOf("0.00") }
    var selectedColorHex by remember { mutableStateOf("#3B82F6") } // Blue by default
    var isDefault by remember { mutableStateOf(false) }

    val colorPalette = listOf(
        "#3B82F6", // Azul
        "#10B981", // Esmeralda
        "#F59E0B", // Ámbar
        "#06B6D4", // Cian
        "#8B5CF6", // Púrpura
        "#EC4899", // Rosa
        "#F97316", // Naranja
        "#64748B"  // Pizarra
    )

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
                            text = "Nueva Cuenta o Tarjeta",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Agrega tus bancos, tarjetas o billeteras",
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

                // Nombre de la cuenta (Ej: Banco Pichincha, Tarjeta Visa, etc.)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la Cuenta") },
                    placeholder = { Text("Ej: Banco Pichincha Ahorros, Visa Gold") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = Emerald500,
                        unfocusedBorderColor = BorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Tipo de Cuenta
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Tipo de Cuenta:", fontSize = 12.sp, color = TextSecondaryDark)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AccountType.values().forEach { type ->
                            val isSelected = selectedType == type
                            val accentColor = parseColorHex(selectedColorHex, Emerald500)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) accentColor.copy(alpha = 0.22f) else SurfaceVariantDark)
                                    .border(1.dp, if (isSelected) accentColor else BorderDark, RoundedCornerShape(10.dp))
                                    .clickable { selectedType = type }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = getAccountTypeIcon(type),
                                    contentDescription = null,
                                    tint = if (isSelected) accentColor else TextSecondaryDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = type.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TextPrimaryDark else TextSecondaryDark
                                )
                            }
                        }
                    }
                }

                // Saldo Inicial y Moneda
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = initialBalanceText,
                        onValueChange = { initialBalanceText = it },
                        label = {
                            Text(if (selectedType == AccountType.CREDIT_CARD) "Deuda / Cupo Usado ($)" else "Saldo Inicial ($)")
                        },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = Emerald500,
                            unfocusedBorderColor = BorderDark
                        ),
                        modifier = Modifier.weight(1.3f)
                    )

                    // Moneda (USD / COP)
                    Column(
                        modifier = Modifier.weight(0.7f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Moneda", fontSize = 11.sp, color = TextSecondaryDark)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("USD", "COP").forEach { curr ->
                                val isSelected = currency == curr
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Emerald500 else SurfaceVariantDark)
                                        .clickable { currency = curr }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = curr,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else TextSecondaryDark
                                    )
                                }
                            }
                        }
                    }
                }

                // Paleta de Colores
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Color Identificador:", fontSize = 12.sp, color = TextSecondaryDark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colorPalette.forEach { hex ->
                            val color = parseColorHex(hex)
                            val isSelected = selectedColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(2.dp, if (isSelected) Color.White else Color.Transparent, CircleShape)
                                    .clickable { selectedColorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Botón Guardar
                Button(
                    onClick = {
                        val initialBalance = initialBalanceText.toDoubleOrNull() ?: 0.0
                        val finalName = if (name.isNotBlank()) name else selectedType.displayName
                        onCreateAccount(
                            finalName,
                            selectedType,
                            currency,
                            initialBalance,
                            selectedColorHex,
                            isDefault
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = parseColorHex(selectedColorHex, Emerald500),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Guardar Cuenta",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}