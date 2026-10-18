package com.finanzas.app.ui.reports.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.SurfaceCardDark
import com.finanzas.app.ui.theme.SurfaceVariantDark
import com.finanzas.app.ui.theme.TextMutedDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

data class MonthlyBarData(
    val monthLabel: String,
    val amount: Double
)

@Composable
fun BarChart(
    data: List<MonthlyBarData>,
    barColor: Color = Emerald500,
    maxHeight: Dp = 160.dp,
    currency: String = "USD",
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val maxVal = (data.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(1.0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCardDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Evolución por Meses",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondaryDark
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                val ratio = (item.amount / maxVal).toFloat() * progress.value

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    if (item.amount > 0) {
                        Text(
                            text = CurrencyFormatter.formatCompact(item.amount, currency),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = barColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height((maxHeight.value * 0.7f * ratio.coerceIn(0.05f, 1f)).dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(if (item.amount > 0) barColor else SurfaceVariantDark)
                    )

                    Text(
                        text = item.monthLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMutedDark,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}