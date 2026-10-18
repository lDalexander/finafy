package com.finanzas.app.ui.debts.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
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
import com.finanzas.app.core.util.DateFormatter
import com.finanzas.app.domain.model.Account
import com.finanzas.app.domain.model.Debt
import com.finanzas.app.domain.model.DebtInstallment
import com.finanzas.app.ui.theme.Amber500
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.Rose500
import com.finanzas.app.ui.theme.SurfaceDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

@Composable
fun PayInstallmentDialog(
    debt: Debt,
    installment: DebtInstallment,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirmPayment: (
        installmentId: String,
        actualAmount: Double,
        lateFee: Double,
        sourceAccountId: String?,
        notes: String?
    ) -> Unit
) {
    var lateFeeText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedAccountId by remember {
        mutableStateOf(accounts.firstOrNull { it.isDefault }?.id ?: accounts.firstOrNull()?.id)
    }

    val lateFee = lateFeeText.toDoubleOrNull() ?: 0.0
    val totalToPay = installment.principalAmount + installment.interestAmount + lateFee

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
                            text = "Registrar Pago de Cuota",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Cuota #${installment.installmentNumber}" +
                                (if (debt.totalInstallments > 0) " de ${debt.totalInstallments}" else "") +
                                " • Vence ${DateFormatter.formatDateOnly(installment.dueDate)}",
                            fontSize = 12.sp,
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

                // Card Resumen Cuota
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceVariantDark)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Fecha de Vencimiento:", fontSize = 12.sp, color = TextSecondaryDark)
                        Text(
                            text = DateFormatter.formatDateOnly(installment.dueDate),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (installment.isOverdue) Rose500 else TextPrimaryDark
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Porción Capital:", fontSize = 12.sp, color = TextSecondaryDark)
                        Text(
                            text = CurrencyFormatter.format(installment.principalAmount, debt.currency),
                            fontSize = 12.sp,
                            color = TextPrimaryDark
                        )
                    }
                    if (installment.interestAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Porción Interés:", fontSize = 12.sp, color = TextSecondaryDark)
                            Text(
                                text = CurrencyFormatter.format(installment.interestAmount, debt.currency),
                                fontSize = 12.sp,
                                color = Amber500
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Valor Total Cuota Base:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        Text(
                            text = CurrencyFormatter.format(installment.principalAmount + installment.interestAmount, debt.currency),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Emerald500
                        )
                    }
                }

                // Campo opcional: Mora / Recargo
                OutlinedTextField(
                    value = lateFeeText,
                    onValueChange = { lateFeeText = it },
                    label = { Text("Mora / Recargo por pago tardío ($) [Opcional]") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Rose500, modifier = Modifier.size(18.dp))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = Rose500,
                        unfocusedBorderColor = BorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Cuenta para descontar dinero
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Pagar desde la cuenta:", fontSize = 12.sp, color = TextSecondaryDark)
                    accounts.filter { it.type != com.finanzas.app.domain.model.AccountType.CREDIT_CARD }.forEach { account ->
                        val isSelected = selectedAccountId == account.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Emerald500.copy(alpha = 0.15f) else SurfaceVariantDark)
                                .border(1.dp, if (isSelected) Emerald500 else BorderDark, RoundedCornerShape(10.dp))
                                .clickable { selectedAccountId = account.id }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = account.name,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Emerald500 else TextPrimaryDark
                            )
                            Text(
                                text = CurrencyFormatter.format(account.currentBalance, account.currency),
                                fontSize = 12.sp,
                                color = TextMutedDark
                            )
                        }
                    }
                }

                // Total Final a Pagar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariantDark)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total a Pagar", fontSize = 11.sp, color = TextMutedDark)
                        Text(
                            text = CurrencyFormatter.format(totalToPay, debt.currency),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Emerald500
                        )
                    }
                }

                // Botón Confirmar
                Button(
                    onClick = {
                        onConfirmPayment(
                            installment.id,
                            totalToPay,
                            lateFee,
                            selectedAccountId,
                            if (notes.isNotBlank()) notes else null
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald500,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Confirmar Pago de Cuota",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}