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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.domain.model.Account
import com.finanzas.app.ui.overlay.components.getAccountTypeIcon
import com.finanzas.app.ui.overlay.components.parseColorHex
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.SurfaceCardDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

@Composable
fun AccountsSection(
    accounts: List<Account>,
    onAddAccountClick: () -> Unit = {},
    onAccountClick: (Account) -> Unit = {},
    onDeleteAccountClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cuentas & Tarjetas (${accounts.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onAddAccountClick() }
                    .background(Emerald500.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Cuenta", tint = Emerald500, modifier = Modifier.size(14.dp))
                Text(text = "Nueva Cuenta", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald500)
            }
        }

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            accounts.forEach { account ->
                AccountCard(
                    account = account,
                    canDelete = accounts.size > 1 && !account.isDefault,
                    onClick = { onAccountClick(account) },
                    onDeleteClick = { onDeleteAccountClick(account.id) }
                )
            }

            // Tarjeta rápida para añadir nueva cuenta
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(115.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceVariantDark.copy(alpha = 0.5f))
                    .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                    .clickable { onAddAccountClick() }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Emerald500.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Emerald500, modifier = Modifier.size(18.dp))
                    }
                    Text(text = "Agregar Banco / Tarjeta", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondaryDark)
                }
            }
        }
    }
}

@Composable
fun AccountCard(
    account: Account,
    canDelete: Boolean = false,
    onClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accentColor = parseColorHex(account.colorHex, Emerald500)

    Column(
        modifier = modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCardDark)
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getAccountTypeIcon(account.type),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar Cuenta",
                    tint = TextMutedDark,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(end = 4.dp)
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
                } else if (canDelete) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = TextMutedDark, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Column {
            Text(
                text = account.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark,
                maxLines = 1
            )
            Text(
                text = account.type.displayName,
                fontSize = 10.sp,
                color = TextMutedDark
            )
        }

        Text(
            text = if (account.isCreditCard) {
                "Deuda: " + CurrencyFormatter.formatCompact(account.currentBalance, account.currency)
            } else {
                CurrencyFormatter.formatCompact(account.currentBalance, account.currency)
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (account.isCreditCard) accentColor else TextPrimaryDark
        )
    }
}