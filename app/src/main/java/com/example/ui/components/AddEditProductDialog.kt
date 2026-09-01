package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.example.data.local.ProductEntity
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted

@Composable
fun AddEditProductDialog(
    initialProduct: ProductEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        id: Int,
        name: String,
        category: String,
        buyPrice: Double,
        sellPrice: Double,
        stockQuantity: Int,
        minAlert: Int,
        iconKey: String,
        barcode: String
    ) -> Unit,
    onDelete: ((ProductEntity) -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Alimentation") }
    var buyPriceStr by remember { mutableStateOf(initialProduct?.buyPrice?.toInt()?.toString() ?: "") }
    var sellPriceStr by remember { mutableStateOf(initialProduct?.sellPrice?.toInt()?.toString() ?: "") }
    var stockQuantityStr by remember { mutableStateOf(initialProduct?.stockQuantity?.toString() ?: "10") }
    var minAlertStr by remember { mutableStateOf(initialProduct?.minStockAlert?.toString() ?: "5") }
    var selectedIcon by remember { mutableStateOf(initialProduct?.iconKey ?: "rice") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }

    val categories = listOf("Alimentation", "Boissons", "Hygiène", "Cosmétique", "Divers")
    val icons = listOf("drink", "milk", "rice", "soap", "oil", "perfume", "sugar", "tea", "coffee", "box")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurfaceElevated,
            border = BorderStroke(1.dp, DarkSurfaceCardBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialProduct == null) "Ajouter un Produit" else "Modifier le Produit",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de l'article (ex: Youki 33cl)", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        focusedLabelColor = GoldLight,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                Text("Catégorie", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GoldPrimary else DarkSurfaceCard)
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isSelected) GoldPrimary else DarkSurfaceCardBorder
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { category = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextDark else TextLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price Inputs
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = buyPriceStr,
                        onValueChange = { buyPriceStr = it },
                        label = { Text("Prix Achat (FCFA)", fontSize = 11.sp, color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldLight,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = sellPriceStr,
                        onValueChange = { sellPriceStr = it },
                        label = { Text("Prix Vente (FCFA)", fontSize = 11.sp, color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldLight,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stock & Alert Threshold
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = stockQuantityStr,
                        onValueChange = { stockQuantityStr = it },
                        label = { Text("Quantité en stock", fontSize = 11.sp, color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldLight,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = minAlertStr,
                        onValueChange = { minAlertStr = it },
                        label = { Text("Seuil alerte", fontSize = 11.sp, color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldLight,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Visual Icon selector
                Text("Icône visuelle", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(icons) { ic ->
                        val isSelected = selectedIcon == ic
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) GoldPrimary.copy(alpha = 0.2f) else DarkSurfaceCard)
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isSelected) GoldPrimary else DarkSurfaceCardBorder
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedIcon = ic }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ProductVisualIcon(iconKey = ic, size = 36.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (initialProduct != null && onDelete != null) {
                        OutlinedButton(
                            onClick = { onDelete(initialProduct) },
                            modifier = Modifier.weight(0.4f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                            border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = {
                            val buyP = buyPriceStr.toDoubleOrNull() ?: 0.0
                            val sellP = sellPriceStr.toDoubleOrNull() ?: 0.0
                            val stock = stockQuantityStr.toIntOrNull() ?: 0
                            val alert = minAlertStr.toIntOrNull() ?: 5
                            if (name.isNotBlank()) {
                                onSave(
                                    initialProduct?.id ?: 0,
                                    name.trim(),
                                    category,
                                    buyP,
                                    sellP,
                                    stock,
                                    alert,
                                    selectedIcon,
                                    barcode
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = TextDark
                        )
                    ) {
                        Text(
                            text = if (initialProduct == null) "Ajouter au Stock" else "Enregistrer",
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                }
            }
        }
    }
}

