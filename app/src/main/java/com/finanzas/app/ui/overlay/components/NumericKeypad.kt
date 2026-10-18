package com.finanzas.app.ui.overlay.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzas.app.core.util.HapticHelper
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.SurfaceCardDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

@Composable
fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onQuickAdd: (Double) -> Unit,
    /** 1.0 para USD, 1000.0 para COP: sumar 1 peso no le sirve a nadie. */
    quickAddScale: Double = 1.0,
    modifier: Modifier = Modifier
) {
    val quickSuffix = if (quickAddScale >= 1000.0) "K" else ""
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Fila 1: 1, 2, 3, +1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton(text = "1", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onDigitClick("1")
            }
            KeypadButton(text = "2", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onDigitClick("2")
            }
            KeypadButton(text = "3", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onDigitClick("3")
            }
            QuickAddButton(text = "+$1$quickSuffix", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onQuickAdd(1.0 * quickAddScale)
            }
        }

        // Fila 2: 4, 5, 6, +5
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton(text = "4", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onDigitClick("4")
            }
            KeypadButton(text = "5", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onDigitClick("5")
            }
            KeypadButton(text = "6", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onDigitClick("6")
            }
            QuickAddButton(text = "+$5$quickSuffix", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onQuickAdd(5.0 * quickAddScale)
            }
        }

        // Fila 3: 7, 8, 9, +10
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton(text = "7", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onDigitClick("7")
            }
            KeypadButton(text = "8", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onDigitClick("8")
            }
            KeypadButton(text = "9", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onDigitClick("9")
            }
            QuickAddButton(text = "+$10$quickSuffix", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onQuickAdd(10.0 * quickAddScale)
            }
        }

        // Fila 4: ., 0, 00, ⌫
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton(text = ".", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onDigitClick(".")
            }
            KeypadButton(text = "0", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onDigitClick("0")
            }
            KeypadButton(text = "00", modifier = Modifier.weight(1f)) {
                HapticHelper.playHeavyClick(context)
                onDigitClick("00")
            }
            KeypadIconButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    HapticHelper.playHeavyClick(context)
                    onBackspace()
                }
            )
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCardDark)
            .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Emerald500),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimaryDark
        )
    }
}

@Composable
private fun QuickAddButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceVariantDark)
            .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Emerald500),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Emerald500
        )
    }
}

@Composable
private fun KeypadIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceVariantDark)
            .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Emerald500),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Borrar",
            tint = TextSecondaryDark
        )
    }
}
