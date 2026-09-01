package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CashRegisterSessionEntity
import com.example.data.local.ProductEntity
import com.example.data.local.PurchaseOrderEntity
import com.example.data.local.SaleTransactionEntity
import com.example.data.local.SupplierEntity
import com.example.ui.components.AccountingReportDialog
import com.example.ui.components.CashRegisterDialog
import com.example.ui.components.PinAuthDialog
import com.example.ui.components.ReceiptPrinterDialog
import com.example.ui.components.ReportDialog
import com.example.ui.components.SettingsAndBackupDialog
import com.example.ui.components.StockAlertCenterDialog
import com.example.ui.components.SuppliersManagementDialog
import com.example.ui.components.UserProfileSwitchDialog
import com.example.util.UserManager
import com.example.util.UserRole
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.RoseContainer
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
fun HomeScreen(
    stats: ShopStats,
    recentSales: List<SaleTransactionEntity>,
    allSales: List<SaleTransactionEntity> = emptyList(),
    allProducts: List<ProductEntity> = emptyList(),
    suppliers: List<SupplierEntity> = emptyList(),
    purchaseOrders: List<PurchaseOrderEntity> = emptyList(),
    activeCashSession: CashRegisterSessionEntity?,
    pastCashSessions: List<CashRegisterSessionEntity>,
    criticalProducts: List<ProductEntity> = emptyList(),
    onRestockProduct: (ProductEntity, Int) -> Unit = { _, _ -> },
    onUpdateProductThreshold: (ProductEntity, Int) -> Unit = { _, _ -> },
    onOpenCashSession: (cashier: String, floatAmount: Double, note: String) -> Unit,
    onCloseCashSession: (session: CashRegisterSessionEntity, actualCash: Double, note: String) -> Unit,
    onAddSupplier: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> },
    onDeleteSupplier: (SupplierEntity) -> Unit = {},
    onSettleSupplierDebt: (SupplierEntity, Double) -> Unit = { _, _ -> },
    onCreatePurchaseOrder: (SupplierEntity, ProductEntity, Int, Double, Boolean, String) -> Unit = { _, _, _, _, _, _ -> },
    onExportBackup: ((String) -> Unit) -> Unit,
    onImportBackup: (String, (Boolean, String) -> Unit) -> Unit,
    onFactoryReset: ((() -> Unit) -> Unit) = {},
    onNavigateToTab: (Int) -> Unit,
    onQuickNewSale: () -> Unit
) {
    val context = LocalContext.current
    var currentUserRole by remember { mutableStateOf(UserManager.getCurrentRole(context)) }
    var currentUserName by remember { mutableStateOf(UserManager.getCurrentUserName(context)) }

    var showReportDialog by remember { mutableStateOf(false) }
    var showAccountingReportDialog by remember { mutableStateOf(false) }
    var showSuppliersDialog by remember { mutableStateOf(false) }
    var showUserProfileDialog by remember { mutableStateOf(false) }
    var showCashRegisterDialog by remember { mutableStateOf(false) }
    var showSettingsBackupDialog by remember { mutableStateOf(false) }
    var showStockAlertCenterDialog by remember { mutableStateOf(false) }
    var selectedSaleForReceipt by remember { mutableStateOf<SaleTransactionEntity?>(null) }
    var showPinAuthDialog by remember { mutableStateOf(false) }
    var pendingManagerAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun requireManagerAccess(action: () -> Unit) {
        if (UserManager.isManager(context)) {
            action()
        } else {
            pendingManagerAction = action
            showPinAuthDialog = true
        }
    }

    if (showPinAuthDialog) {
        PinAuthDialog(
            actionTitle = "Accès Gérant Requis",
            actionDescription = "Cette section nécessite le code PIN du gérant.",
            onSuccess = {
                showPinAuthDialog = false
                pendingManagerAction?.invoke()
                pendingManagerAction = null
            },
            onDismiss = {
                showPinAuthDialog = false
                pendingManagerAction = null
            }
        )
    }

    if (showUserProfileDialog) {
        UserProfileSwitchDialog(
            currentRole = currentUserRole,
            currentName = currentUserName,
            onRoleChanged = { newRole, newName ->
                currentUserRole = newRole
                currentUserName = newName
            },
            onDismiss = {
                showUserProfileDialog = false
                currentUserRole = UserManager.getCurrentRole(context)
                currentUserName = UserManager.getCurrentUserName(context)
            }
        )
    }

    if (showSuppliersDialog) {
        SuppliersManagementDialog(
            suppliers = suppliers,
            products = allProducts,
            purchaseOrders = purchaseOrders,
            onAddSupplier = onAddSupplier,
            onDeleteSupplier = onDeleteSupplier,
            onSettleSupplierDebt = onSettleSupplierDebt,
            onCreatePurchaseOrder = onCreatePurchaseOrder,
            onDismiss = { showSuppliersDialog = false }
        )
    }

    if (showAccountingReportDialog) {
        AccountingReportDialog(
            stats = stats,
            sales = allSales.ifEmpty { recentSales },
            products = allProducts,
            cashSessions = pastCashSessions,
            onDismiss = { showAccountingReportDialog = false }
        )
    }

    if (showStockAlertCenterDialog) {
        StockAlertCenterDialog(
            criticalProducts = criticalProducts,
            onDismiss = { showStockAlertCenterDialog = false },
            onRestock = onRestockProduct,
            onUpdateThreshold = onUpdateProductThreshold,
            onNavigateToStockTab = { onNavigateToTab(2) }
        )
    }

    if (showReportDialog) {
        ReportDialog(stats = stats, onDismiss = { showReportDialog = false })
    }

    if (showCashRegisterDialog) {
        CashRegisterDialog(
            activeSession = activeCashSession,
            pastSessions = pastCashSessions,
            onOpenSession = { cashier, amt, note ->
                onOpenCashSession(cashier, amt, note)
            },
            onCloseSession = { session, actual, note ->
                onCloseCashSession(session, actual, note)
            },
            onDismiss = { showCashRegisterDialog = false }
        )
    }

    if (showSettingsBackupDialog) {
        SettingsAndBackupDialog(
            onExportBackup = onExportBackup,
            onImportBackup = onImportBackup,
            onFactoryReset = onFactoryReset,
            onDismiss = { showSettingsBackupDialog = false }
        )
    }

    if (selectedSaleForReceipt != null) {
        ReceiptPrinterDialog(
            sale = selectedSaleForReceipt!!,
            onDismiss = { selectedSaleForReceipt = null }
        )
    }

    val todayDateFormatted = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.FRANCE).format(Date())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRANCE) else it.toString() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("home_screen_container"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Obsidian & Gold Hero Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1D2236), Color(0xFF121420))
                        )
                    )
                    .border(
                        BorderStroke(1.dp, DarkSurfaceCardBorder),
                        RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    // Date & Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(
                                        BorderStroke(1.5.dp, GoldPrimary),
                                        RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.dabasaba_logo),
                                    contentDescription = "Logo DabaSaba",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = todayDateFormatted,
                                    color = GoldLight.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "DabaSaba",
                                    color = TextLight,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 24.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // User Profile Switcher Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (currentUserRole == UserRole.GERANT) GoldContainer else DarkSurfaceElevated)
                                    .border(
                                        BorderStroke(1.dp, if (currentUserRole == UserRole.GERANT) GoldPrimary else DarkSurfaceCardBorder),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { showUserProfileDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (currentUserRole == UserRole.GERANT) "👑 Gérant" else "👤 Caissier",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentUserRole == UserRole.GERANT) GoldLight else TextLight
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Stock Alert Notification Bell with live count badge
                            Box {
                                IconButton(
                                    onClick = { showStockAlertCenterDialog = true },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (criticalProducts.isNotEmpty()) (if (criticalProducts.any { it.stockQuantity <= 0 }) StatusRedBg else StatusYellowBg) else DarkSurfaceElevated)
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (criticalProducts.isNotEmpty()) (if (criticalProducts.any { it.stockQuantity <= 0 }) StatusRed else StatusYellow) else DarkSurfaceCardBorder
                                            ),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (criticalProducts.isNotEmpty()) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                        contentDescription = "Alertes Stock",
                                        tint = if (criticalProducts.isNotEmpty()) (if (criticalProducts.any { it.stockQuantity <= 0 }) StatusRed else StatusYellow) else GoldLight,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                if (criticalProducts.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (criticalProducts.any { it.stockQuantity <= 0 }) StatusRed else StatusYellow)
                                            .border(BorderStroke(1.dp, DarkBackground), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${criticalProducts.size}",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (criticalProducts.any { it.stockQuantity <= 0 }) Color.White else TextDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Settings / Backup button
                            IconButton(
                                onClick = {
                                    requireManagerAccess {
                                        showSettingsBackupDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated)
                                    .border(BorderStroke(1.dp, DarkSurfaceCardBorder), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Sauvegarde & Paramètres",
                                    tint = GoldLight,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // VENTES DU JOUR Headline
                    Text(
                        text = "CHIFFRE D'AFFAIRES DU JOUR",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = ShopViewModel.formatNumber(stats.todaySalesAmount),
                            color = TextLight,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "FCFA",
                            color = GoldLight,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(StatusGreenBg)
                                .border(BorderStroke(1.dp, StatusGreen.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Actif",
                                color = StatusGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Connection Mode Selector Card (Gérant vs Caissier)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("connection_mode_selector_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(
                    1.dp,
                    if (currentUserRole == UserRole.GERANT) GoldPrimary.copy(alpha = 0.5f) else DarkSurfaceCardBorder
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (currentUserRole == UserRole.GERANT) GoldContainer else DarkSurfaceCard)
                                    .border(
                                        BorderStroke(1.dp, if (currentUserRole == UserRole.GERANT) GoldPrimary else DarkSurfaceCardBorder),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (currentUserRole == UserRole.GERANT) Icons.Default.Security else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (currentUserRole == UserRole.GERANT) GoldPrimary else TextLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Mode de Connexion",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight
                                )
                                Text(
                                    text = "Session active : $currentUserName",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        // Switch/Edit user button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceCard)
                                .border(BorderStroke(1.dp, DarkSurfaceCardBorder), RoundedCornerShape(12.dp))
                                .clickable { showUserProfileDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Changer d'utilisateur",
                                    tint = GoldLight,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Paramètres PIN",
                                    color = TextLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Two Mode Selection Buttons: [ 👑 Gérant ] and [ 👤 Caissier ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isGerant = currentUserRole == UserRole.GERANT
                        // Button Gérant
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isGerant) GoldPrimary else DarkSurfaceCard)
                                .border(
                                    BorderStroke(1.dp, if (isGerant) GoldPrimary else DarkSurfaceCardBorder),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    if (!isGerant) {
                                        requireManagerAccess {
                                            UserManager.setCurrentUser(context, UserRole.GERANT, "Mamadou (Gérant)")
                                            currentUserRole = UserRole.GERANT
                                            currentUserName = "Mamadou (Gérant)"
                                            Toast.makeText(context, "Connecté en Mode Gérant !", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                .testTag("select_manager_mode_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "👑 Gérant",
                                    fontSize = 13.sp,
                                    fontWeight = if (isGerant) FontWeight.Black else FontWeight.Bold,
                                    color = if (isGerant) TextDark else TextLight
                                )
                                if (isGerant) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = TextDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        val isCaissier = currentUserRole == UserRole.CAISSIER
                        // Button Caissier
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isCaissier) GoldPrimary else DarkSurfaceCard)
                                .border(
                                    BorderStroke(1.dp, if (isCaissier) GoldPrimary else DarkSurfaceCardBorder),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    if (!isCaissier) {
                                        UserManager.setCurrentUser(context, UserRole.CAISSIER, "Awa (Caissière)")
                                        currentUserRole = UserRole.CAISSIER
                                        currentUserName = "Awa (Caissière)"
                                        Toast.makeText(context, "Connecté en Mode Caissier !", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .testTag("select_cashier_mode_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "👤 Caissier",
                                    fontSize = 13.sp,
                                    fontWeight = if (isCaissier) FontWeight.Black else FontWeight.Bold,
                                    color = if (isCaissier) TextDark else TextLight
                                )
                                if (isCaissier) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = TextDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3 Stat Cards Row
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Bénéfice
                StatSummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.TrendingUp,
                    iconBg = RoseContainer,
                    iconTint = RoseAccent,
                    title = "Bénéfice Net",
                    mainValue = ShopViewModel.formatNumber(stats.todayProfitAmount),
                    unit = "FCFA",
                    badgeText = "Marge",
                    badgeBg = StatusGreenBg,
                    badgeTint = StatusGreen
                )

                // Card 2: Ventes
                StatSummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ShoppingCart,
                    iconBg = GoldContainer,
                    iconTint = GoldPrimary,
                    title = "Tickets Vente",
                    mainValue = "${stats.todayTransactionsCount}",
                    unit = "reçus",
                    badgeText = "Jour",
                    badgeBg = StatusGreenBg,
                    badgeTint = StatusGreen
                )

                // Card 3: Alertes Stock
                StatSummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Warning,
                    iconBg = if (criticalProducts.any { it.stockQuantity <= 0 }) StatusRedBg else StatusYellowBg,
                    iconTint = if (criticalProducts.any { it.stockQuantity <= 0 }) StatusRed else StatusYellow,
                    title = "Alertes Stock",
                    mainValue = "${if (criticalProducts.isNotEmpty()) criticalProducts.size else stats.stockAlertsCount}",
                    unit = "produits",
                    badgeText = if (criticalProducts.any { it.stockQuantity <= 0 }) "Rupture" else "Alerte",
                    badgeBg = if (criticalProducts.any { it.stockQuantity <= 0 }) StatusRedBg else StatusYellowBg,
                    badgeTint = if (criticalProducts.any { it.stockQuantity <= 0 }) StatusRed else StatusYellow,
                    onClick = { showStockAlertCenterDialog = true }
                )
            }
        }

        // "+ Nouvelle Vente" Primary Button
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Button(
                    onClick = onQuickNewSale,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("new_sale_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextDark
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(TextDark.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = TextDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Enregistrer une Vente (Caisse)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )
                    }
                }
            }
        }

        // Quick Actions Grid
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Caisse Z",
                        icon = Icons.Default.AccountBalanceWallet,
                        containerColor = DarkSurfaceElevated,
                        iconTint = GoldPrimary,
                        onClick = { showCashRegisterDialog = true }
                    )

                    QuickActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Fournisseurs",
                        icon = Icons.Default.Business,
                        containerColor = DarkSurfaceElevated,
                        iconTint = RoseAccent,
                        onClick = {
                            requireManagerAccess {
                                showSuppliersDialog = true
                            }
                        }
                    )

                    QuickActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Bilan & Export",
                        icon = Icons.Default.FileDownload,
                        containerColor = DarkSurfaceElevated,
                        iconTint = StatusGreen,
                        onClick = {
                            requireManagerAccess {
                                showAccountingReportDialog = true
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Articles Stock",
                        icon = Icons.Default.Inventory2,
                        containerColor = DarkSurfaceElevated,
                        iconTint = GoldLight,
                        onClick = { onNavigateToTab(2) }
                    )

                    QuickActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Carnet Crédit",
                        icon = Icons.Default.CreditCard,
                        containerColor = DarkSurfaceElevated,
                        iconTint = GoldPrimary,
                        onClick = { onNavigateToTab(3) }
                    )

                    QuickActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Tableau Graph.",
                        icon = Icons.Default.BarChart,
                        containerColor = DarkSurfaceElevated,
                        iconTint = GoldLight,
                        onClick = { showReportDialog = true }
                    )
                }
            }
        }

        // Activité Récente Header
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dernières Ventes & Reçus",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextLight
                    )
                    Text(
                        text = "Touchez pour imprimer ou envoyer ticket",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Text(
                    text = "Vente POS →",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    modifier = Modifier
                        .clickable { onNavigateToTab(1) }
                        .padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // List of Recent Sales with one-tap thermal ticket
        if (recentSales.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucune vente enregistrée pour le moment.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(recentSales, key = { it.id }) { sale ->
                RecentSaleCard(
                    sale = sale,
                    onClick = { selectedSaleForReceipt = sale }
                )
            }
        }
    }
}

@Composable
fun StatSummaryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    mainValue: String,
    unit: String,
    badgeText: String,
    badgeBg: Color,
    badgeTint: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(1.dp, DarkSurfaceCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = mainValue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = TextLight
            )

            Text(
                text = unit,
                fontSize = 10.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeTint
                )
            }
        }
    }
}

@Composable
fun QuickActionTile(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(1.dp, DarkSurfaceCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(containerColor)
                    .border(BorderStroke(1.dp, DarkSurfaceCardBorder), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight,
                maxLines = 1
            )
        }
    }
}

@Composable
fun RecentSaleCard(
    sale: SaleTransactionEntity,
    onClick: () -> Unit
) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date(sale.timestamp))
    val initials = if (sale.clientName.isNotBlank()) {
        sale.clientName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
    } else "CC"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("recent_sale_${sale.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(1.dp, DarkSurfaceCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Client Initials Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GoldContainer)
                        .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials.ifEmpty { "C" },
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = sale.clientName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Imprimer",
                            tint = GoldLight,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = sale.itemsSummary,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = ShopViewModel.formatCFAShort(sale.totalAmount),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldLight
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeStr,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (sale.paymentMethod == "ESPECES") Icons.Default.Money else Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = if (sale.paymentMethod == "ESPECES") StatusGreen else RoseAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
