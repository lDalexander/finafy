package com.finanzas.app.ui.debts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
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
import com.finanzas.app.domain.model.Account
import com.finanzas.app.domain.model.AccountType
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

@Composable
fun AddDebtAbonoDialog(
    debtWithInstallments: DebtWithInstallments,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirmAbono: (debtId: String, amount: Double, sourceAccountId: String?, notes: String?) -> Unit
) {
    val debt = debtWithInstallments.debt
    val validAccounts = accounts.filter { it.type != AccountType.CREDIT_CARD }
    var selectedAccountId by remember { mutableStateOf(validAccounts.firstOrNull()?.id) }
    var amountText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val remaining = debtWithInstallments.remainingTotalDebt
    val newRemaining = (remaining - amount).coerceAtLeast(0.0)

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Amber500.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Amber500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Abonar a Deuda",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "${debt.entityName} (${debt.title})",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantDark)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMutedDark, modifier = Modifier.size(16.dp))
                    }
                }

                // Balance actual
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariantDark)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Saldo pendiente actual:", fontSize = 12.sp, color = TextSecondaryDark)
                    Text(
                        text = CurrencyFormatter.format(remaining, debt.currency),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Rose500
                    )
                }

                // Input Monto Abono
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Valor a Abonar ($)") },
                    placeholder = { Text("0.00") },
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

                // Montos Rápidos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(10.0, 20.0, 50.0, remaining).distinct().forEach { quickVal ->
                        val label = if (quickVal == remaining) "Total" else "$${quickVal.toInt()}"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceVariantDark)
                                .clickable { amountText = String.format(java.util.Locale.US, "%.2f", quickVal) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Amber500)
                        }
                    }
                }

                // Selector de Cuenta de Pago
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Pagar y descontar de la cuenta:", fontSize = 12.sp, color = TextSecondaryDark)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        validAccounts.forEach { acc ->
                            val isSelected = acc.id == selectedAccountId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Amber500 else SurfaceVariantDark)
                                    .border(1.dp, if (isSelected) Amber500 else BorderDark, RoundedCornerShape(10.dp))
                                    .clickable { selectedAccountId = acc.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.Black else TextPrimaryDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Column {
                                        Text(
                                            text = acc.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else TextPrimaryDark
                                        )
                                        Text(
                                            text = CurrencyFormatter.format(acc.currentBalance, acc.currency),
                                            fontSize = 10.sp,
                                            color = if (isSelected) Color.Black.copy(alpha = 0.7f) else TextSecondaryDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Nota Opcional
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Nota / Referencia (Opcional)") },
                    placeholder = { Text("Ej: Abono quincena") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = Amber500,
                        unfocusedBorderColor = BorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Resumen del impacto
                if (amount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceVariantDark)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Nuevo saldo de la deuda:", fontSize = 12.sp, color = TextSecondaryDark)
                        Text(
                            text = CurrencyFormatter.format(newRemaining, debt.currency),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!CurrencyFormatter.isPositive(newRemaining)) Emerald400 else Amber500
                        )
                    }
                }

                // Botón Confirmar
                Button(
                    onClick = {
                        onConfirmAbono(
                            debt.id,
                            amount,
                            selectedAccountId,
                            if (notesText.isNotBlank()) notesText else null
                        )
                    },
                    enabled = amount > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber500,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(
                        text = "Registrar Abono de ${CurrencyFormatter.format(amount, debt.currency)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}