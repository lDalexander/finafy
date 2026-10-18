package com.finanzas.app.ui.dashboard.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.core.util.DateFormatter
import com.finanzas.app.domain.model.TransactionType
import com.finanzas.app.domain.model.TransactionWithDetails
import com.finanzas.app.ui.overlay.components.getCategoryIcon
import com.finanzas.app.ui.theme.Amber500
import com.finanzas.app.ui.theme.Blue500
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.Rose500
import com.finanzas.app.ui.theme.SurfaceCardDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

@Composable
fun TransactionTimelineSection(
    transactions: List<TransactionWithDetails>,
    selectedFilter: TransactionType?,
    onFilterSelected: (TransactionType?) -> Unit,
    onEditTransaction: (TransactionWithDetails) -> Unit = {},
    onDeleteTransaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredTransactions = if (selectedFilter == null) {
        transactions
    } else {
        transactions.filter { it.transaction.type == selectedFilter }
    }

    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { DateFormatter.formatHeaderDate(it.transaction.timestamp) }
    }

    var transactionToDelete by remember { mutableStateOf<TransactionWithDetails?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Últimos Movimientos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Text(
                text = "${filteredTransactions.size} registros",
                fontSize = 12.sp,
                color = TextMutedDark
            )
        }

        // Filtros rápidos por Tipo
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChipItem(
                title = "Todos",
                isSelected = selectedFilter == null,
                onClick = { onFilterSelected(null) }
            )
            TransactionType.values().forEach { type ->
                FilterChipItem(
                    title = type.displayName,
                    isSelected = selectedFilter == type,
                    onClick = { onFilterSelected(type) }
                )
            }
        }

        // Lista agrupada por fecha
        if (groupedTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCardDark)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = TextMutedDark,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "No hay movimientos registrados",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = "Usa el botón flotante para capturar uno rápido",
                        fontSize = 12.sp,
                        color = TextMutedDark
                    )
                }
            }
        } else {
            groupedTransactions.forEach { (dateHeader, items) ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = dateHeader,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald500,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    items.forEach { item ->
                        TransactionItemRow(
                            item = item,
                            onClick = { onEditTransaction(item) },
                            onEditClick = { onEditTransaction(item) },
                            onDeleteClick = { transactionToDelete = item }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación de eliminación
    transactionToDelete?.let { targetItem ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Eliminar Movimiento", color = TextPrimaryDark) },
            text = {
                Text(
                    "¿Deseas eliminar este movimiento de ${CurrencyFormatter.format(targetItem.transaction.amount, targetItem.sourceAccount?.currency ?: targetItem.targetAccount?.currency ?: "USD")}? Los saldos de las cuentas involucradas serán revertidos atómicamente.",
                    color = TextSecondaryDark,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(targetItem.transaction.id)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("Eliminar y Revertir", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancelar", color = TextSecondaryDark)
                }
            },
            containerColor = SurfaceCardDark
        )
    }
}

@Composable
private fun FilterChipItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Emerald500.copy(alpha = 0.2f) else SurfaceCardDark)
            .border(1.dp, if (isSelected) Emerald500 else BorderDark, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Emerald500 else TextSecondaryDark
        )
    }
}

@Composable
fun TransactionItemRow(
    item: TransactionWithDetails,
    onClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tx = item.transaction

    val isCardPayment = tx.type == TransactionType.TRANSFER && item.targetAccount?.type == com.finanzas.app.domain.model.AccountType.CREDIT_CARD

    val (typeIcon, typeColor, prefix) = when {
        isCardPayment -> Triple(Icons.Default.CreditCard, Amber500, "💳")
        tx.type == TransactionType.EXPENSE -> Triple(
            item.category?.iconName?.let { getCategoryIcon(it) } ?: Icons.Default.NorthEast,
            Rose500,
            "-"
        )
        tx.type == TransactionType.CREDIT_PURCHASE -> Triple(
            item.category?.iconName?.let { getCategoryIcon(it) } ?: Icons.Default.CreditCard,
            Amber500,
            "-"
        )
        tx.type == TransactionType.TRANSFER -> Triple(Icons.Default.SyncAlt, Blue500, "↔")
        tx.type == TransactionType.REMITTANCE -> Triple(Icons.Default.Public, Emerald500, "-")
        tx.type == TransactionType.INCOME -> Triple(
            item.category?.iconName?.let { getCategoryIcon(it) } ?: Icons.Default.SouthWest,
            Emerald500,
            "+"
        )
        else -> Triple(Icons.Default.SyncAlt, Blue500, "")
    }

    val title = when {
        isCardPayment -> "Pago a Tarjeta (${item.targetAccount?.name ?: "T. Crédito"})"
        else -> item.category?.name ?: tx.type.displayName
    }

    val subtitle = when {
        isCardPayment -> "Debitado de: ${item.sourceAccount?.name ?: "Cuenta"} • Reduce deuda"
        tx.type == TransactionType.EXPENSE -> item.sourceAccount?.name ?: "Cuenta Origen"
        tx.type == TransactionType.CREDIT_PURCHASE -> "${item.sourceAccount?.name ?: "T. Crédito"}${tx.installments?.let { " • $it cuotas" } ?: ""}"
        tx.type == TransactionType.TRANSFER -> "${item.sourceAccount?.name ?: "Origen"} → ${item.targetAccount?.name ?: "Destino"}"
        tx.type == TransactionType.REMITTANCE -> "Giro a ${tx.targetCurrency ?: "COP"}: ${tx.targetAmount?.let { CurrencyFormatter.format(it, tx.targetCurrency ?: "COP") } ?: ""}"
        tx.type == TransactionType.INCOME -> "En: ${item.targetAccount?.name ?: item.sourceAccount?.name ?: "Cuenta"}"
        else -> ""
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCardDark)
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Icono
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(typeColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = typeIcon,
                contentDescription = null,
                tint = typeColor,
                modifier = Modifier.size(18.dp)
            )
        }

        // Info Central
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMutedDark,
                maxLines = 1
            )
            if (!tx.note.isNullOrBlank()) {
                Text(
                    text = "“${tx.note}”",
                    fontSize = 10.sp,
                    color = TextSecondaryDark,
                    maxLines = 1
                )
            }
        }

        // Monto y Hora
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$prefix ${CurrencyFormatter.format(tx.amount, item.sourceAccount?.currency ?: item.targetAccount?.currency ?: "USD")}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = typeColor
            )
            Text(
                text = DateFormatter.formatTime(tx.timestamp),
                fontSize = 10.sp,
                color = TextMutedDark
            )
        }

        // Acciones: Editar & Eliminar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar",
                    tint = TextMutedDark.copy(alpha = 0.7f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
