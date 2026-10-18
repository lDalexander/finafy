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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import kotlin.math.abs
import com.finanzas.app.domain.model.Account
import com.finanzas.app.domain.model.AccountType
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.SurfaceDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditAccountDialog(
    account: Account,
    movementCount: Int,
    onDismiss: () -> Unit,
    onSaveAccount: (Account) -> Unit
) {
    // La moneda es una propiedad intrinseca de la cuenta, no una preferencia.
    // Cambiarla no convierte nada: reinterpreta el saldo y TODO el historial,
    // porque la moneda de cada movimiento se deduce de su cuenta.
    val monedaBloqueada = movementCount > 0 ||
            abs(account.currentBalance) > CurrencyFormatter.EPSILON
    var name by remember { mutableStateOf(account.name) }
    var selectedType by remember { mutableStateOf(account.type) }
    var selectedCurrency by remember { mutableStateOf(account.currency) }
    var selectedColor by remember { mutableStateOf(account.colorHex) }
    var isDefault by remember { mutableStateOf(account.isDefault) }

    val colorsPalette = listOf(
        "#10B981", "#3B82F6", "#8B5CF6", "#F59E0B",
        "#EC4899", "#14B8A6", "#EF4444", "#6366F1",
        "#84CC16", "#06B6D4"
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
                            text = "Editar Cuenta / Tarjeta",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Modifica el nombre, color o saldo",
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

                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la Cuenta") },
                    placeholder = { Text("Ej: Banco Pichincha, Billetera") },
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
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Emerald500 else SurfaceVariantDark)
                                    .border(1.dp, if (isSelected) Emerald500 else BorderDark, RoundedCornerShape(10.dp))
                                    .clickable { selectedType = type }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = type.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else TextSecondaryDark
                                )
                            }
                        }
                    }
                }

                // Saldo: SOLO LECTURA.
                //
                // El saldo es el resultado de los movimientos, no un dato que se
                // teclee. Editarlo a mano rompia de forma irrastreable la identidad
                //     saldo = saldo_inicial + suma(movimientos)
                // Para corregirlo se registra un ingreso o un gasto, o se elimina el
                // movimiento que sobra.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariantDark)
                        .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = TextMutedDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (selectedType == AccountType.CREDIT_CARD) "Deuda / Cupo Usado" else "Saldo Actual",
                                fontSize = 12.sp,
                                color = TextSecondaryDark
                            )
                        }
                        Text(
                            text = CurrencyFormatter.format(account.currentBalance, account.currency),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimaryDark
                        )
                    }
                    Text(
                        text = "El saldo sale de tus movimientos. Para ajustarlo registra un " +
                                "ingreso o un gasto, o elimina el movimiento que sobra.",
                        fontSize = 10.sp,
                        color = TextMutedDark
                    )
                }

                // Moneda
                if (monedaBloqueada) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceVariantDark)
                            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = TextMutedDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(text = "Moneda", fontSize = 12.sp, color = TextSecondaryDark)
                            }
                            Text(
                                text = account.currency,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimaryDark
                            )
                        }
                        Text(
                            text = if (movementCount > 0) {
                                "No se puede cambiar: la cuenta tiene $movementCount movimiento(s) " +
                                        "registrados en ${account.currency}. Para otra moneda, crea una cuenta nueva."
                            } else {
                                "No se puede cambiar mientras la cuenta tenga saldo. " +
                                        "Para otra moneda, crea una cuenta nueva."
                            },
                            fontSize = 10.sp,
                            color = TextMutedDark
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("USD", "COP").forEach { curr ->
                            val isSelected = selectedCurrency == curr
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Emerald500.copy(alpha = 0.2f) else SurfaceVariantDark)
                                    .border(1.dp, if (isSelected) Emerald500 else BorderDark, RoundedCornerShape(10.dp))
                                    .clickable { selectedCurrency = curr }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = curr,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Emerald500 else TextSecondaryDark
                                )
                            }
                        }
                    }
                }

                // Paleta de Colores
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Color identificador:", fontSize = 12.sp, color = TextSecondaryDark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colorsPalette.forEach { hex ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) {
                                Emerald500
                            }
                            val isSelected = selectedColor.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Cuenta Default
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isDefault = !isDefault }
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        colors = CheckboxDefaults.colors(checkedColor = Emerald500)
                    )
                    Text(
                        text = "Establecer como cuenta predeterminada",
                        fontSize = 12.sp,
                        color = TextPrimaryDark
                    )
                }

                // Botón Guardar
                Button(
                    onClick = {
                        // currentBalance no se toca: solo se editan datos de presentacion.
                        val updated = account.copy(
                            name = if (name.isNotBlank()) name else account.name,
                            type = selectedType,
                            currency = if (monedaBloqueada) account.currency else selectedCurrency,
                            colorHex = selectedColor,
                            isDefault = isDefault
                        )
                        onSaveAccount(updated)
                    },
                    enabled = name.isNotBlank(),
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