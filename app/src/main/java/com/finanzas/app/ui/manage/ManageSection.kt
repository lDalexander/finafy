package com.finanzas.app.ui.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.domain.model.Account
import com.finanzas.app.domain.model.Category
import com.finanzas.app.domain.model.TransactionType
import com.finanzas.app.ui.overlay.components.getAccountTypeIcon
import com.finanzas.app.ui.overlay.components.getCategoryIcon
import com.finanzas.app.ui.overlay.components.parseColorHex
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.Rose500
import com.finanzas.app.ui.theme.SurfaceCardDark
import com.finanzas.app.ui.theme.SurfaceDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

enum class ManageTab(val label: String) {
    ACCOUNTS("Cuentas & Bancos"),
    CATEGORIES("Categorías")
}

@Composable
fun ManageSection(
    accounts: List<Account>,
    categories: List<Category>,
    onAddAccountClick: () -> Unit,
    onAccountClick: (Account) -> Unit = {},
    onDeleteAccountClick: (String) -> Unit,
    onAddCategoryClick: (TransactionType) -> Unit,
    onDeleteCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ManageTab.ACCOUNTS) }
    var selectedCategoryType by remember { mutableStateOf(TransactionType.EXPENSE) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selector de Pestaña Principal: [Cuentas & Bancos] vs [Categorías]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ManageTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Emerald500 else Color.Transparent)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else TextSecondaryDark
                    )
                }
            }
        }

        // --- SUB-SECCIÓN 1: GESTIÓN DE CUENTAS ---
        if (selectedTab == ManageTab.ACCOUNTS) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tus Cuentas Registradas (${accounts.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )

                Button(
                    onClick = onAddAccountClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(text = "Nueva Cuenta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                accounts.forEach { account ->
                    val color = parseColorHex(account.colorHex, Emerald500)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCardDark)
                            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                            .clickable { onAccountClick(account) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getAccountTypeIcon(account.type),
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = account.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                    if (account.isDefault) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Emerald500.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "Default", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Emerald500)
                                        }
                                    }
                                }

                                Text(
                                    text = account.type.displayName + " • " + account.currency,
                                    fontSize = 11.sp,
                                    color = TextMutedDark
                                )

                                Text(
                                    text = if (account.isCreditCard)
                                        "Deuda: " + CurrencyFormatter.format(account.currentBalance, account.currency)
                                    else
                                        "Saldo: " + CurrencyFormatter.format(account.currentBalance, account.currency),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (account.isCreditCard) color else TextPrimaryDark
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onAccountClick(account) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceVariantDark)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextSecondaryDark, modifier = Modifier.size(16.dp))
                            }

                            if (!account.isDefault && accounts.size > 1) {
                                IconButton(
                                    onClick = { onDeleteAccountClick(account.id) },
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceVariantDark)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Rose500, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SUB-SECCIÓN 2: GESTIÓN DE CATEGORÍAS ---
        if (selectedTab == ManageTab.CATEGORIES) {
            // Filtro de Categoría: Gasto vs Ingreso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    TransactionType.EXPENSE to "Categorías de Gasto",
                    TransactionType.INCOME to "Categorías de Ingreso"
                ).forEach { (type, label) ->
                    val isSelected = selectedCategoryType == type
                    val color = if (type == TransactionType.EXPENSE) Rose500 else Emerald500
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) color.copy(alpha = 0.2f) else SurfaceCardDark)
                            .border(1.dp, if (isSelected) color else BorderDark, RoundedCornerShape(10.dp))
                            .clickable { selectedCategoryType = type }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) color else TextSecondaryDark
                        )
                    }
                }
            }

            // Header con Botón de Nueva Categoría
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val filteredCategories = categories.filter { it.type == selectedCategoryType }
                Text(
                    text = "Categorías (${filteredCategories.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )

                Button(
                    onClick = { onAddCategoryClick(selectedCategoryType) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedCategoryType == TransactionType.EXPENSE) Rose500 else Emerald500,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(text = "Nueva Categoría", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Lista de Categorías
            val currentFiltered = categories.filter { it.type == selectedCategoryType }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentFiltered.forEach { cat ->
                    val color = parseColorHex(cat.colorHex, Emerald500)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCardDark)
                            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(cat.iconName),
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = cat.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryDark
                            )
                        }

                        IconButton(
                            onClick = { onDeleteCategoryClick(cat.id) },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariantDark)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = TextMutedDark, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}