package com.finanzas.app.ui.overlay.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
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
import com.finanzas.app.domain.model.Category
import com.finanzas.app.ui.theme.BorderDark
import com.finanzas.app.ui.theme.Emerald500
import com.finanzas.app.ui.theme.SurfaceCardDark
import com.finanzas.app.ui.theme.TextPrimaryDark
import com.finanzas.app.ui.theme.TextSecondaryDark

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "restaurant", "dining" -> Icons.Default.Restaurant
        "shopping_cart", "supermarket" -> Icons.Default.ShoppingCart
        "shopping_bag" -> Icons.Default.ShoppingBag
        "directions_car", "transport" -> Icons.Default.DirectionsCar
        "home" -> Icons.Default.Home
        "movie" -> Icons.Default.Movie
        "medical_services", "health" -> Icons.Default.LocalHospital
        "school" -> Icons.Default.School
        "devices" -> Icons.Default.Devices
        "flight" -> Icons.Default.Flight
        "subscriptions" -> Icons.Default.Subscriptions
        "send" -> Icons.Default.Send
        "savings" -> Icons.Default.Savings
        "payments" -> Icons.Default.Payments
        "store" -> Icons.Default.Store
        "trending_up" -> Icons.Default.TrendingUp
        "work" -> Icons.Default.Work
        "account_balance" -> Icons.Default.AccountBalance
        "receipt_long", "receipt" -> Icons.Default.Receipt
        else -> Icons.Default.Category
    }
}

@Composable
fun CategoryPicker(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Categoría",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondaryDark,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { category ->
                val isSelected = category.id == selectedCategoryId
                val colorAccent = parseColorHex(category.colorHex, Emerald500)
                val bgColor = if (isSelected) colorAccent.copy(alpha = 0.22f) else SurfaceCardDark
                val borderColor = if (isSelected) colorAccent else BorderDark

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .clickable { onCategorySelected(category) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category.iconName),
                        contentDescription = category.name,
                        tint = if (isSelected) colorAccent else TextSecondaryDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = category.name,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) TextPrimaryDark else TextSecondaryDark
                    )
                }
            }
        }
    }
}
