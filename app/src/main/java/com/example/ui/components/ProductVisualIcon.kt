package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Sanitizer
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkSurfaceCardBorder
import com.example.ui.theme.GoldLight
import com.example.ui.theme.RoseAccent

@Composable
fun ProductVisualIcon(
    iconKey: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val (bgBrush, iconVector, tintColor) = when (iconKey.lowercase()) {
        "drink", "youki", "boisson" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF132B26), Color(0xFF0D1E1A))),
            Icons.Default.LocalDrink,
            Color(0xFF34D399)
        )
        "milk", "lait", "nido" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF14243B), Color(0xFF0F1B2C))),
            Icons.Default.Opacity,
            Color(0xFF60A5FA)
        )
        "rice", "riz", "cereales" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF2E2614), Color(0xFF1E190E))),
            Icons.Default.Grass,
            GoldLight
        )
        "soap", "savon", "omo", "hygiene" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF2D1826), Color(0xFF1F101A))),
            Icons.Default.Sanitizer,
            RoseAccent
        )
        "oil", "huile" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF132A33), Color(0xFF0E1E24))),
            Icons.Default.CleaningServices,
            Color(0xFF22D3EE)
        )
        "perfume", "parfum", "cosmetique" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF301520), Color(0xFF210E16))),
            Icons.Default.LocalFlorist,
            Color(0xFFFB7185)
        )
        "sugar", "sucre" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF241633), Color(0xFF180F23))),
            Icons.Default.Spa,
            Color(0xFFC084FC)
        )
        "tea", "the" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF152A1C), Color(0xFF0E1D13))),
            Icons.Default.EmojiFoodBeverage,
            Color(0xFF4ADE80)
        )
        "coffee", "cafe" -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF2B1F17), Color(0xFF1C140F))),
            Icons.Default.Coffee,
            Color(0xFFFBBF24)
        )
        else -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF1E212D), Color(0xFF151821))),
            Icons.Default.Inventory2,
            Color(0xFF94A3B8)
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
            .border(BorderStroke(1.dp, DarkSurfaceCardBorder), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

