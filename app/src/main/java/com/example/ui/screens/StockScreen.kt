package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProductEntity
import com.example.data.local.StockMovementEntity
import com.example.ui.components.AddEditProductDialog
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.ProductVisualIcon
import com.example.ui.components.StockMovementDialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.StatusYellowBg
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.ShopStats
import com.example.ui.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StockScreen(
    products: List<ProductEntity>,
    stockMovements: List<StockMovementEntity>,
    stats: ShopStats,
    selectedFilterTab: String,
    onFilterTabSelected: (String) -> Unit,
    onSaveProduct: (
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
    onDeleteProduct: (ProductEntity) -> Unit,
    onAdjustStock: (ProductEntity, Int) -> Unit,
    onRestock: (ProductEntity, Int, String) -> Unit,
    onDeclareLoss: (ProductEntity, Int, String) -> Unit
) {
    var mainViewMode by remember { mutableStateOf(0) } // 0: Articles & Niveaux, 1: Journal Mouvements
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var productForMovement by remember { mutableStateOf<ProductEntity?>(null) }
    var showScanner by remember { mutableStateOf(false) }

    var searchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    if (showScanner) {
        BarcodeScannerDialog(
            products = products,
            onBarcodeScanned = { scannedBarcode ->
                val matched = products.firstOrNull { it.barcode == scannedBarcode }
                if (matched != null) {
                    productToEdit = matched
                } else {
                    searchQuery = scannedBarcode
                    searchVisible = true
                }
                showScanner = false
            },
            onDismiss = { showScanner = false }
        )
    }

    if (showAddDialog || productToEdit != null) {
        AddEditProductDialog(
            initialProduct = productToEdit,
            onDismiss = {
                showAddDialog = false
                productToEdit = null
            },
            onSave = { id, name, cat, buyP, sellP, stock, alert, icon, bar ->
                onSaveProduct(id, name, cat, buyP, sellP, stock, alert, icon, bar)
                showAddDialog = false
                productToEdit = null
            },
            onDelete = { product ->
                onDeleteProduct(product)
                showAddDialog = false
                productToEdit = null
            }
        )
    }

    if (productForMovement != null) {
        StockMovementDialog(
            product = productForMovement!!,
            onRestock = { qty, note ->
                onRestock(productForMovement!!, qty, note)
                productForMovement = null
            },
            onDeclareLoss = { qty, note ->
                onDeclareLoss(productForMovement!!, qty, note)
                productForMovement = null
            },
            onDismiss = { productForMovement = null }
        )
    }

    val filteredProducts = products.filter { product ->
        val matchesSearch = searchQuery.isBlank() || product.name.contains(searchQuery, ignoreCase = true) || product.barcode.contains(searchQuery, ignoreCase = true)
        val matchesTab = when (selectedFilterTab) {
            "Stock Faible" -> product.stockQuantity in 1..product.minStockAlert
            "Rupture" -> product.stockQuantity <= 0
            else -> true
        }
        matchesSearch && matchesTab
    }

    val outOfStockCount = products.count { it.stockQuantity <= 0 }
    val lowStockCount = products.count { it.stockQuantity in 1..it.minStockAlert }
    val inStockCount = products.count { it.stockQuantity > it.minStockAlert }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("stock_screen_container")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1D2236), Color(0xFF121420))
                            )
                        )
                        .border(
                            BorderStroke(1.dp, DarkSurfaceCardBorder),
                            RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Gestion des Stocks",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextLight
                                )
                                Text(
                                    text = "Inventaire · Mouvements · Alertes",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { showScanner = true },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(GoldContainer)
                                        .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "Scanner",
                                        tint = GoldLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { searchVisible = !searchVisible },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurfaceElevated)
                                        .border(BorderStroke(1.dp, DarkSurfaceCardBorder), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (searchVisible) Icons.Default.Close else Icons.Default.Search,
                                        contentDescription = "Rechercher",
                                        tint = GoldLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (searchVisible) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Filtrer par nom ou code-barres...", fontSize = 13.sp, color = TextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurfaceElevated,
                                    unfocusedContainerColor = DarkSurfaceElevated,
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = DarkSurfaceCardBorder,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Metric Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StockMetricBadge(
                                modifier = Modifier.weight(1f),
                                count = "${products.size}",
                                label = "Total",
                                bgColor = DarkSurfaceElevated,
                                textColor = TextLight
                            )

                            StockMetricBadge(
                                modifier = Modifier.weight(1f),
                                count = "$inStockCount",
                                label = "En stock",
                                bgColor = StatusGreenBg,
                                textColor = StatusGreen
                            )

                            StockMetricBadge(
                                modifier = Modifier.weight(1f),
                                count = "$lowStockCount",
                                label = "Alertes",
                                bgColor = StatusYellowBg,
                                textColor = StatusYellow
                            )

                            StockMetricBadge(
                                modifier = Modifier.weight(1f),
                                count = "$outOfStockCount",
                                label = "Ruptures",
                                bgColor = StatusRedBg,
                                textColor = StatusRed
                            )
                        }
                    }
                }
            }

            // Main Tab Switcher (Articles vs Journal Mouvements)
            item {
                Spacer(modifier = Modifier.height(12.dp))
                TabRow(
                    selectedTabIndex = mainViewMode,
                    containerColor = DarkSurfaceCard,
                    contentColor = GoldLight,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[mainViewMode]),
                            color = GoldPrimary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = mainViewMode == 0,
                        onClick = { mainViewMode = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Articles (${products.size})", fontSize = 12.sp, fontWeight = if (mainViewMode == 0) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    )
                    Tab(
                        selected = mainViewMode == 1,
                        onClick = { mainViewMode = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Journal Mouvements (${stockMovements.size})", fontSize = 12.sp, fontWeight = if (mainViewMode == 1) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    )
                }
            }

            if (mainViewMode == 0) {
                // Filter Tabs (Tous, Stock Faible, Rupture)
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tabs = listOf(
                            "Tous" to "${products.size}",
                            "Stock Faible" to "$lowStockCount",
                            "Rupture" to "$outOfStockCount"
                        )

                        tabs.forEach { (tab, count) ->
                            val isSelected = selectedFilterTab == tab
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onFilterTabSelected(tab) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) GoldPrimary else DarkSurfaceCard
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) GoldPrimary else DarkSurfaceCardBorder
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = tab,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) TextDark else TextLight
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = count,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) TextDark else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(10.dp)) }

                // Products List
                if (filteredProducts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aucun article dans cette catégorie de stock.",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredProducts, key = { it.id }) { product ->
                        StockItemCard(
                            product = product,
                            onEdit = { productToEdit = product },
                            onManageMovement = { productForMovement = product }
                        )
                    }
                }
            } else {
                // Stock Movements History
                item { Spacer(modifier = Modifier.height(10.dp)) }

                if (stockMovements.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Aucun mouvement de stock enregistré.", color = TextMuted, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    items(stockMovements, key = { it.id }) { movement ->
                        StockMovementHistoryCard(movement = movement)
                    }
                }
            }
        }

        // Floating Action Button (+)
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 85.dp)
                .testTag("add_stock_fab"),
            containerColor = GoldPrimary,
            contentColor = TextDark,
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Ajouter un produit",
                tint = TextDark,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun StockMetricBadge(
    modifier: Modifier = Modifier,
    count: String,
    label: String,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, DarkSurfaceCardBorder), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun StockItemCard(
    product: ProductEntity,
    onEdit: () -> Unit,
    onManageMovement: () -> Unit
) {
    val isOutOfStock = product.stockQuantity <= 0
    val isLowStock = product.stockQuantity in 1..product.minStockAlert

    val marginPercent = if (product.buyPrice > 0) {
        (((product.sellPrice - product.buyPrice) / product.buyPrice) * 100).toInt()
    } else 0

    val (badgeBg, badgeColor, badgeLabel) = when {
        isOutOfStock -> Triple(StatusRedBg, StatusRed, "Rupture")
        isLowStock -> Triple(StatusYellowBg, StatusYellow, "Faible")
        else -> Triple(StatusGreenBg, StatusGreen, "En stock")
    }

    val quantityColor = when {
        isOutOfStock -> StatusRed
        isLowStock -> StatusYellow
        else -> StatusGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("stock_item_${product.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(1.dp, DarkSurfaceCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductVisualIcon(
                        iconKey = product.iconKey,
                        size = 46.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = product.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "● $badgeLabel",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${product.category}${if (product.barcode.isNotBlank()) " · 🏷️ ${product.barcode}" else ""}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Achat ${ShopViewModel.formatNumber(product.buyPrice)} · Vente ${ShopViewModel.formatNumber(product.sellPrice)}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StatusGreenBg)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "+$marginPercent%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusGreen
                                )
                            }
                        }
                    }
                }

                // Big Quantity Number
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "${product.stockQuantity}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = quantityColor
                    )
                    Text("unités", fontSize = 10.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action strip (+ Réappro / Perte / Modifier)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .border(BorderStroke(1.dp, DarkSurfaceCardBorder), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { onManageMovement() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SwapVert, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mouvement (+/-)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                }

                Row(
                    modifier = Modifier.clickable { onEdit() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Modifier", fontSize = 11.sp, color = TextLight)
                }
            }
        }
    }
}

@Composable
fun StockMovementHistoryCard(movement: StockMovementEntity) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date(movement.timestamp))
    val isPositive = movement.deltaQuantity > 0

    val (badgeBg, badgeColor, reasonLabel) = when (movement.reason) {
        "APPROVISIONNEMENT" -> Triple(StatusGreenBg, StatusGreen, "Approvisionnement (+)")
        "VENTE" -> Triple(DarkSurfaceElevated, TextMuted, "Vente Caisse (-)")
        "PERTE" -> Triple(StatusRedBg, StatusRed, "Perte / Casse (-)")
        else -> Triple(DarkSurfaceElevated, GoldLight, movement.reason)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(1.dp, DarkSurfaceCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = movement.productName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = reasonLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "$dateStr ${if (movement.note.isNotBlank()) "· ${movement.note}" else ""}", fontSize = 10.sp, color = TextMuted)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isPositive) "+" else ""}${movement.deltaQuantity}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isPositive) StatusGreen else if (movement.reason == "PERTE") StatusRed else TextLight
                )
                Text(
                    text = "Reste: ${movement.remainingStock}",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}
