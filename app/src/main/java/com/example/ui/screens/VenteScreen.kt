package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import com.example.ui.components.PinAuthDialog
import com.example.ui.components.UserProfileSwitchDialog
import com.example.util.UserManager
import com.example.util.UserRole
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProductEntity
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.ProductVisualIcon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.StatusYellowBg
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.CartItem
import com.example.ui.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VenteScreen(
    products: List<ProductEntity>,
    cartItems: Map<Int, CartItem>,
    selectedCategory: String,
    searchQuery: String,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    onOpenCart: () -> Unit
) {
    val context = LocalContext.current
    var showScannerDialog by remember { mutableStateOf(false) }
    var showUserProfileDialog by remember { mutableStateOf(false) }
    var currentUserRole by remember { mutableStateOf(UserManager.getCurrentRole(context)) }
    var currentUserName by remember { mutableStateOf(UserManager.getCurrentUserName(context)) }

    val categories = listOf("Tous", "Boissons", "Alimentation", "Cosmétique", "Hygiène", "Divers")

    val filteredProducts = products.filter { product ->
        val matchesCategory = selectedCategory == "Tous" || product.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() || product.name.contains(searchQuery, ignoreCase = true) || product.barcode.contains(searchQuery)
        matchesCategory && matchesSearch
    }

    val totalCartCount = cartItems.values.sumOf { it.quantity }
    val totalCartAmount = cartItems.values.sumOf { it.product.sellPrice * it.quantity }

    if (showUserProfileDialog) {
        UserProfileSwitchDialog(
            currentRole = currentUserRole,
            currentUserName = currentUserName,
            onDismiss = { showUserProfileDialog = false },
            onSwitchToUser = { newRole, newName ->
                UserManager.setCurrentUser(context, newRole, newName)
                currentUserRole = newRole
                currentUserName = newName
                showUserProfileDialog = false
                Toast.makeText(context, "Connecté en tant que $newName ($newRole)", Toast.LENGTH_SHORT).show()
                // Force reload activity to refresh navigation bar
                (context as? ComponentActivity)?.recreate()
            }
        )
    }

    if (showScannerDialog) {
        BarcodeScannerDialog(
            products = products,
            onBarcodeScanned = { barcode ->
                val matched = products.firstOrNull { it.barcode == barcode }
                if (matched != null) {
                    if (matched.stockQuantity > 0) {
                        onAddToCart(matched)
                        Toast.makeText(context, "+1 ${matched.name} ajouté au panier !", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "${matched.name} est en rupture de stock !", Toast.LENGTH_LONG).show()
                    }
                } else {
                    onSearchQueryChanged(barcode)
                    Toast.makeText(context, "Code-barres $barcode non associé. Recherche filtrée.", Toast.LENGTH_LONG).show()
                }
                showScannerDialog = false
            },
            onDismiss = { showScannerDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("vente_screen_container")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Obsidian Header Section
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
                    // Title and Cart Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Vente Rapide (POS)",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextLight
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Role Indicator chip (clickable to switch profile if PIN known)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (currentUserRole == UserRole.GERANT) GoldPrimary.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                        .border(
                                            BorderStroke(1.dp, if (currentUserRole == UserRole.GERANT) GoldPrimary else DarkSurfaceCardBorder),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { showUserProfileDialog = true }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("vente_user_profile_chip")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (currentUserRole == UserRole.GERANT) "👑 Gérant" else "👤 Caissier",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentUserRole == UserRole.GERANT) GoldLight else TextMuted
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Opérateur : $currentUserName",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        // Top Cart Icon Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(GoldContainer)
                                .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)), RoundedCornerShape(20.dp))
                                .clickable { onOpenCart() }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("open_cart_top_pill")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Panier",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$totalCartCount",
                                    color = GoldLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Input Bar with Scan Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = {
                                Text("Rechercher ou scanner code-barres...", fontSize = 13.sp, color = TextMuted)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = GoldLight)
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { showScannerDialog = true },
                                    modifier = Modifier.testTag("scan_barcode_icon_button")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GoldContainer)
                                            .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCodeScanner,
                                            contentDescription = "Scanner Code-barres",
                                            tint = GoldLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
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
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Pills
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) GoldPrimary else DarkSurfaceCard)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isSelected) GoldPrimary else DarkSurfaceCardBorder
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { onCategorySelected(category) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) TextDark else TextLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2-Column Product Grid
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun produit trouvé",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = if (totalCartCount > 0) 140.dp else 90.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductPOSCard(
                            product = product,
                            inCartCount = cartItems[product.id]?.quantity ?: 0,
                            onAddToCart = { onAddToCart(product) }
                        )
                    }
                }
            }
        }

        // Floating Bottom Cart Checkout Bar
        AnimatedVisibility(
            visible = totalCartCount > 0,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 80.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCart() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = TextDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "$totalCartCount article(s)",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = ShopViewModel.formatCFA(totalCartAmount),
                                color = GoldLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = onOpenCart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = TextDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Encaisser",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductPOSCard(
    product: ProductEntity,
    inCartCount: Int,
    onAddToCart: () -> Unit
) {
    val isOutOfStock = product.stockQuantity <= 0
    val isLowStock = product.stockQuantity in 1..product.minStockAlert

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pos_product_${product.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(1.dp, DarkSurfaceCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Product 3D Visual Icon
                ProductVisualIcon(
                    iconKey = product.iconKey,
                    size = 56.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Product Name
                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Price
                Text(
                    text = ShopViewModel.formatNumber(product.sellPrice),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldLight
                )
                Text(
                    text = "FCFA",
                    fontSize = 10.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Stock Badge
                val (badgeBg, badgeColor, badgeLabel) = when {
                    isOutOfStock -> Triple(StatusRedBg, StatusRed, "Rupture")
                    isLowStock -> Triple(StatusYellowBg, StatusYellow, "${product.stockQuantity} unités")
                    else -> Triple(StatusGreenBg, StatusGreen, "${product.stockQuantity} unités")
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            // Gold "+" Add Button in top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isOutOfStock) DarkSurfaceElevated else GoldPrimary)
                    .clickable(enabled = !isOutOfStock) { onAddToCart() },
                contentAlignment = Alignment.Center
            ) {
                if (inCartCount > 0) {
                    Text(
                        text = "$inCartCount",
                        color = TextDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter",
                        tint = if (isOutOfStock) TextMuted else TextDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
