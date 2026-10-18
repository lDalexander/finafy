package com.finanzas.app.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import com.finanzas.app.domain.model.Category
import com.finanzas.app.domain.model.Transaction
import com.finanzas.app.domain.model.TransactionType
import com.finanzas.app.domain.model.TransactionWithDetails
import com.finanzas.app.ui.overlay.components.getCategoryIcon
import com.finanzas.app.ui.theme.Amber500
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.Rose500
import com.finanzas.app.ui.theme.SurfaceDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditTransactionDialog(
    item: TransactionWithDetails,
    accounts: List<Account>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSaveTransaction: (Transaction) -> Unit
) {
    val tx = item.transaction
    var amountText by remember { mutableStateOf(tx.amount.toString()) }
    var selectedCategoryId by remember { mutableStateOf(tx.categoryId) }
    var selectedSourceAccountId by remember { mutableStateOf(tx.sourceAccountId) }
    var selectedTargetAccountId by remember { mutableStateOf(tx.targetAccountId) }
    var noteText by remember { mutableStateOf(tx.note ?: "") }

    val amount = amountText.toDoubleOrNull() ?: 0.0

    val relevantCategories = remember(tx.type, categories) {
        when (tx.type) {
            TransactionType.INCOME -> categories.filter { it.type == TransactionType.INCOME }
            else -> categories.filter { it.type == TransactionType.EXPENSE }
        }
    }

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
                                .background(
                                    when (tx.type) {
                                        TransactionType.INCOME -> Emerald500.copy(alpha = 0.2f)
                                        TransactionType.EXPENSE -> Rose500.copy(alpha = 0.2f)
                                        else -> Amber500.copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = when (tx.type) {
                                    TransactionType.INCOME -> Emerald500
                                    TransactionType.EXPENSE -> Rose500
                                    else -> Amber500
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Editar Movimiento",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = tx.type.displayName,
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

                // Input Monto
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto ($)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = Emerald500,
                        unfocusedBorderColor = BorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Cuenta Origen
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Cuenta Origen:", fontSize = 12.sp, color = TextSecondaryDark)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.forEach { acc ->
                            val isSelected = acc.id == selectedSourceAccountId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Emerald500 else SurfaceVariantDark)
                                    .border(1.dp, if (isSelected) Emerald500 else BorderDark, RoundedCornerShape(10.dp))
                                    .clickable { selectedSourceAccountId = acc.id }
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
                                    Text(
                                        text = acc.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else TextPrimaryDark
                                    )
                                }
                            }
                        }
                    }
                }

                // Si es transferencia, Selector de Cuenta Destino
                if (tx.type == TransactionType.TRANSFER) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Cuenta / Tarjeta Destino:", fontSize = 12.sp, color = TextSecondaryDark)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            accounts.forEach { acc ->
                                val isSelected = acc.id == selectedTargetAccountId
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Emerald500 else SurfaceVariantDark)
                                        .border(1.dp, if (isSelected) Emerald500 else BorderDark, RoundedCornerShape(10.dp))
                                        .clickable { selectedTargetAccountId = acc.id }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = acc.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else TextPrimaryDark
                                    )
                                }
                            }
                        }
                    }
                }

                // Selector de Categoría
                if (relevantCategories.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Categoría:", fontSize = 12.sp, color = TextSecondaryDark)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            relevantCategories.forEach { cat ->
                                val isSelected = cat.id == selectedCategoryId
                                val catColor = try {
                                    Color(android.graphics.Color.parseColor(cat.colorHex))
                                } catch (e: Exception) {
                                    Emerald500
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) catColor.copy(alpha = 0.25f) else SurfaceVariantDark)
                                        .border(1.dp, if (isSelected) catColor else BorderDark, RoundedCornerShape(10.dp))
                                        .clickable { selectedCategoryId = cat.id }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(cat.iconName),
                                            contentDescription = cat.name,
                                            tint = if (isSelected) catColor else TextSecondaryDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = cat.name,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) catColor else TextPrimaryDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Nota
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Nota / Descripción") },
                    placeholder = { Text("Ej: Supermercado, Almuerzo") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = Emerald500,
                        unfocusedBorderColor = BorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Botón Guardar
                Button(
                    onClick = {
                        val updated = tx.copy(
                            amount = amount,
                            categoryId = selectedCategoryId,
                            sourceAccountId = selectedSourceAccountId,
                            targetAccountId = selectedTargetAccountId,
                            note = if (noteText.isNotBlank()) noteText else null
                        )
                        onSaveTransaction(updated)
                    },
                    enabled = amount > 0,
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
                        text = "Guardar Cambios",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}