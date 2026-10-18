package com.finanzas.app.ui.manage.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.finanzas.app.domain.model.TransactionType
import com.finanzas.app.ui.overlay.components.getCategoryIcon
import com.finanzas.app.ui.overlay.components.parseColorHex
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
fun CreateCategoryDialog(
    initialType: TransactionType = TransactionType.EXPENSE,
    onDismiss: () -> Unit,
    onCreateCategory: (name: String, iconName: String, type: TransactionType, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedIcon by remember { mutableStateOf(if (initialType == TransactionType.INCOME) "payments" else "shopping_cart") }
    var selectedColorHex by remember { mutableStateOf(if (initialType == TransactionType.INCOME) "#10B981" else "#F59E0B") }

    val availableIcons = listOf(
        "restaurant", "shopping_cart", "shopping_bag", "directions_car",
        "home", "movie", "medical_services", "school",
        "devices", "flight", "subscriptions", "payments",
        "store", "trending_up", "work", "account_balance",
        "receipt", "savings"
    )

    val colorPalette = listOf(
        "#10B981", "#3B82F6", "#F59E0B", "#EC4899",
        "#8B5CF6", "#06B6D4", "#EF4444", "#F97316",
        "#64748B", "#84CC16"
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
                            text = "Nueva Categoría",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Personaliza tus tipos de ingresos y gastos",
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

                // Tipo: Gasto vs Ingreso
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedType == TransactionType.EXPENSE) Rose500 else Color.Transparent)
                            .clickable { selectedType = TransactionType.EXPENSE }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gasto",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == TransactionType.EXPENSE) Color.White else TextSecondaryDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedType == TransactionType.INCOME) Emerald500 else Color.Transparent)
                            .clickable { selectedType = TransactionType.INCOME }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ingreso",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == TransactionType.INCOME) Color.Black else TextSecondaryDark
                        )
                    }
                }

                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la Categoría") },
                    placeholder = { Text(if (selectedType == TransactionType.INCOME) "Ej: Bono, Inversión" else "Ej: Gym, Mascotas") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = parseColorHex(selectedColorHex),
                        unfocusedBorderColor = BorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Ícono
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Seleccionar Ícono:", fontSize = 12.sp, color = TextSecondaryDark)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableIcons.forEach { iconName ->
                            val isSelected = selectedIcon == iconName
                            val color = parseColorHex(selectedColorHex)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.25f) else SurfaceVariantDark)
                                    .border(1.dp, if (isSelected) color else BorderDark, RoundedCornerShape(10.dp))
                                    .clickable { selectedIcon = iconName },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(iconName),
                                    contentDescription = null,
                                    tint = if (isSelected) color else TextSecondaryDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Selector de Color
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
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(2.dp, if (isSelected) Color.White else Color.Transparent, CircleShape)
                                    .clickable { selectedColorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                // Botón Guardar
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onCreateCategory(name, selectedIcon, selectedType, selectedColorHex)
                        }
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
                        text = "Guardar Categoría",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}