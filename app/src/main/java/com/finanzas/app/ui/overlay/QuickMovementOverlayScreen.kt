package com.finanzas.app.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.core.util.HapticHelper
import com.finanzas.app.domain.model.AccountType
import com.finanzas.app.domain.model.TransactionType
import com.finanzas.app.ui.overlay.components.AccountPicker
import com.finanzas.app.ui.overlay.components.CategoryPicker
import com.finanzas.app.ui.overlay.components.InstallmentsPicker
import com.finanzas.app.ui.overlay.components.MovementTypeChips
import com.finanzas.app.ui.overlay.components.NumericKeypad
import com.finanzas.app.ui.overlay.components.QuickRemittanceOverlayView
import com.finanzas.app.ui.theme.Amber500
import com.finanzas.app.ui.theme.Blue500
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.FinanzasTheme
import com.finanzas.app.ui.theme.Rose500
import com.finanzas.app.ui.theme.SurfaceDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

@Composable
fun QuickMovementOverlayScreen(
    viewModel: QuickMovementViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val isCardPayment = uiState.selectedType == TransactionType.TRANSFER &&
            uiState.selectedTargetAccount?.type == AccountType.CREDIT_CARD

    // Tocar fuera de la tarjeta cierra; tocar la tarjeta no.
    val scrimSource = remember { MutableInteractionSource() }
    val cardSource = remember { MutableInteractionSource() }

    FinanzasTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = scrimSource,
                    indication = null,
                    onClick = onDismiss
                )
                // Barras de estado y navegacion, muesca de camara y teclado.
                .safeDrawingPadding()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .clickable(
                        interactionSource = cardSource,
                        indication = null,
                        onClick = {}
                    )
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header: Tipos de Movimiento + Cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MovementTypeChips(
                        selectedType = uiState.selectedType,
                        onTypeSelected = viewModel::onTypeSelected,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextMutedDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Error Message
                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.errorMessage?.let { msg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Rose500.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Rose500, modifier = Modifier.size(16.dp))
                            Text(text = msg, fontSize = 12.sp, color = Rose500)
                        }
                    }
                }

                // Si es Giro/Remesa a Colombia -> Vista Especializada con TRM en vivo, Spread y cálculo bidireccional
                if (uiState.selectedType == TransactionType.REMITTANCE) {
                    // Display de Monto en USD
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceVariantDark)
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = CurrencyFormatter.formatCompact(uiState.numericAmount, "USD") + " USD",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Emerald500,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Giro a Colombia • " + CurrencyFormatter.format(uiState.numericTargetCop, "COP"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMutedDark
                            )
                        }
                    }

                    QuickRemittanceOverlayView(
                        uiState = uiState,
                        viewModel = viewModel,
                        onDismiss = onDismiss
                    )

                    // Teclado numérico para ingresar USD rápidamente
                    NumericKeypad(
                        onDigitClick = viewModel::onDigitClick,
                        onBackspace = viewModel::onBackspace,
                        onQuickAdd = viewModel::onQuickAdd,
                        quickAddScale = uiState.quickAddScale
                    )
                } else {
                    // Display Estándar para Gastos, Tarjeta, Transferencias e Ingresos
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceVariantDark)
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val accentColor = when {
                            isCardPayment -> Amber500
                            uiState.selectedType == TransactionType.EXPENSE -> Rose500
                            uiState.selectedType == TransactionType.CREDIT_PURCHASE -> Amber500
                            uiState.selectedType == TransactionType.TRANSFER -> Blue500
                            uiState.selectedType == TransactionType.INCOME -> Emerald500
                            else -> Emerald500
                        }

                        val titleText = when {
                            isCardPayment -> "Pago a Tarjeta • " + (uiState.selectedTargetAccount?.name ?: "Sin tarjeta")
                            else -> uiState.selectedType.displayName + " • " + (uiState.selectedSourceAccount?.name ?: "Sin cuenta")
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = CurrencyFormatter.formatCompact(uiState.numericAmount, uiState.activeCurrency),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = titleText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMutedDark
                            )
                        }
                    }

                    // Selectores Contextuales de un Toque
                    when (uiState.selectedType) {
                        TransactionType.EXPENSE -> {
                            AccountPicker(
                                title = "Cuenta Origen (Sale dinero)",
                                accounts = uiState.availableSourceAccounts,
                                selectedAccountId = uiState.selectedSourceAccount?.id,
                                onAccountSelected = viewModel::onSourceAccountSelected
                            )
                            CategoryPicker(
                                categories = uiState.categories,
                                selectedCategoryId = uiState.selectedCategory?.id,
                                onCategorySelected = viewModel::onCategorySelected
                            )
                        }

                        TransactionType.CREDIT_PURCHASE -> {
                            AccountPicker(
                                title = "Tarjeta de Crédito (Genera Deuda)",
                                accounts = uiState.availableSourceAccounts,
                                selectedAccountId = uiState.selectedSourceAccount?.id,
                                onAccountSelected = viewModel::onSourceAccountSelected
                            )
                            CategoryPicker(
                                categories = uiState.categories,
                                selectedCategoryId = uiState.selectedCategory?.id,
                                onCategorySelected = viewModel::onCategorySelected
                            )
                            InstallmentsPicker(
                                selectedInstallments = uiState.installments,
                                onInstallmentsChanged = viewModel::onInstallmentsChanged
                            )
                        }

                        TransactionType.TRANSFER -> {
                            if (isCardPayment) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Amber500.copy(alpha = 0.15f))
                                        .border(1.dp, Amber500.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = Amber500, modifier = Modifier.size(20.dp))
                                        Column {
                                            Text("Abono / Pago a Tarjeta de Crédito", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Amber500)
                                            Text(
                                                "Se descontará de ${uiState.selectedSourceAccount?.name ?: "la cuenta origen"} y reducirá la deuda de ${uiState.selectedTargetAccount?.name ?: "la tarjeta"} en ${CurrencyFormatter.format(uiState.numericAmount, uiState.activeCurrency)}.",
                                                fontSize = 11.sp,
                                                color = TextSecondaryDark
                                            )
                                        }
                                    }
                                }
                            }

                            AccountPicker(
                                title = if (isCardPayment) "Pagar desde (Cuenta Origen / Ahorros / Efectivo)" else "De: Cuenta Origen",
                                accounts = uiState.availableSourceAccounts,
                                selectedAccountId = uiState.selectedSourceAccount?.id,
                                onAccountSelected = viewModel::onSourceAccountSelected
                            )
                            AccountPicker(
                                title = if (isCardPayment) "Tarjeta a Pagar (Destino)" else "A: Cuenta o Tarjeta Destino",
                                accounts = uiState.availableTargetAccounts,
                                selectedAccountId = uiState.selectedTargetAccount?.id,
                                onAccountSelected = viewModel::onTargetAccountSelected
                            )
                        }

                        TransactionType.INCOME -> {
                            AccountPicker(
                                title = "Depositar en (Cuenta Destino)",
                                accounts = uiState.accounts.filter { it.type != AccountType.CREDIT_CARD },
                                selectedAccountId = uiState.selectedTargetAccount?.id ?: uiState.selectedSourceAccount?.id,
                                onAccountSelected = viewModel::onTargetAccountSelected
                            )
                            CategoryPicker(
                                categories = uiState.categories,
                                selectedCategoryId = uiState.selectedCategory?.id,
                                onCategorySelected = viewModel::onCategorySelected
                            )
                        }

                        else -> Unit
                    }

                    // Numeric Keypad
                    NumericKeypad(
                        onDigitClick = viewModel::onDigitClick,
                        onBackspace = viewModel::onBackspace,
                        onQuickAdd = viewModel::onQuickAdd,
                        quickAddScale = uiState.quickAddScale
                    )

                    // Botón Acción: Registrar Movimiento / Pago
                    val buttonColor = when {
                        isCardPayment -> Amber500
                        uiState.selectedType == TransactionType.EXPENSE -> Rose500
                        uiState.selectedType == TransactionType.CREDIT_PURCHASE -> Amber500
                        uiState.selectedType == TransactionType.TRANSFER -> Blue500
                        uiState.selectedType == TransactionType.INCOME -> Emerald500
                        else -> Emerald500
                    }

                    val buttonText = when {
                        isCardPayment -> "Registrar Pago a Tarjeta (${CurrencyFormatter.format(uiState.numericAmount, uiState.activeCurrency)})"
                        uiState.selectedType == TransactionType.CREDIT_PURCHASE -> "Registrar Compra Tarjeta (${CurrencyFormatter.format(uiState.numericAmount, uiState.activeCurrency)})"
                        else -> "Registrar ${uiState.selectedType.displayName} (${CurrencyFormatter.format(uiState.numericAmount, uiState.activeCurrency)})"
                    }

                    Button(
                        onClick = {
                            viewModel.submitMovement {
                                HapticHelper.playSuccess(context)
                                onDismiss()
                            }
                        },
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCardPayment) Icons.Default.CreditCard else Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = buttonText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}