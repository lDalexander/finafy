package com.finanzas.app.ui.overlay.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzas.app.domain.model.TransactionType
import com.finanzas.app.ui.theme.Amber500
import com.finanzas.app.ui.theme.Blue500
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.Rose500
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark

data class MovementTypeOption(
    val type: TransactionType,
    val title: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun MovementTypeChips(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        MovementTypeOption(TransactionType.EXPENSE, "Gasto", Icons.Default.NorthEast, Rose500),
        MovementTypeOption(TransactionType.CREDIT_PURCHASE, "Compra Crédito", Icons.Default.CreditCard, Amber500),
        MovementTypeOption(TransactionType.TRANSFER, "Pagar Tarjeta / Transferir", Icons.Default.SyncAlt, Blue500),
        MovementTypeOption(TransactionType.REMITTANCE, "Giro / Remesa", Icons.Default.Public, Emerald500),
        MovementTypeOption(TransactionType.INCOME, "Ingreso", Icons.Default.SouthWest, Emerald500)
    )

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            val isSelected = option.type == selectedType
            val bgColor = if (isSelected) option.accentColor.copy(alpha = 0.2f) else SurfaceVariantDark
            val borderColor = if (isSelected) option.accentColor else BorderDark
            val contentColor = if (isSelected) option.accentColor else TextPrimaryDark

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                    .clickable { onTypeSelected(option.type) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.title,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = option.title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor
                    )
                }
            }
        }
    }
}
