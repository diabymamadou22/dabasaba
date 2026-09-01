package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CartAndCheckoutSheet
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.components.UserProfileSwitchDialog
import com.example.util.UserManager
import com.example.util.UserRole
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StockScreen
import com.example.ui.screens.VenteScreen
import com.example.ui.theme.DabaSabaTheme
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.ReceiptPrintHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DabaSabaTheme {
                var showSplash by rememberSaveable { mutableStateOf(true) }

                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "splash_transition"
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen(
                            onSplashFinished = { showSplash = false }
                        )
                    } else {
                        DabaSabaApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
    val tabIndex: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DabaSabaApp(viewModel: ShopViewModel) {
    val context = LocalContext.current
    var currentUserRole by rememberSaveable { mutableStateOf(UserManager.getCurrentRole(context)) }
    var currentUserName by rememberSaveable { mutableStateOf(UserManager.getCurrentUserName(context)) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(if (currentUserRole == UserRole.CAISSIER) 1 else 0) }
    var isCartOpen by rememberSaveable { mutableStateOf(false) }

    // If role switches to CAISSIER, enforce tab to Vente (1)
    LaunchedEffect(currentUserRole) {
        if (currentUserRole == UserRole.CAISSIER) {
            selectedTabIndex = 1
        }
    }

    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val allSales by viewModel.allSales.collectAsStateWithLifecycle()
    val recentSales by viewModel.recentSales.collectAsStateWithLifecycle()
    val credits by viewModel.allCredits.collectAsStateWithLifecycle()
    val paymentLogs by viewModel.paymentLogs.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedProductCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.productSearchQuery.collectAsStateWithLifecycle()
    val stockFilterTab by viewModel.stockFilterTab.collectAsStateWithLifecycle()
    val lastCompletedSale by viewModel.lastCompletedSale.collectAsStateWithLifecycle()
    val stockMovements by viewModel.allStockMovements.collectAsStateWithLifecycle()
    val activeCashSession by viewModel.activeCashSession.collectAsStateWithLifecycle()
    val pastCashSessions by viewModel.allCashSessions.collectAsStateWithLifecycle()
    val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
    val purchaseOrders by viewModel.allPurchaseOrders.collectAsStateWithLifecycle()
    val activeStockAlert by viewModel.activeStockAlert.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val cartSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val allNavItems = listOf(
        BottomNavItem("Accueil", Icons.Filled.Home, Icons.Outlined.Home, "tab_accueil", 0),
        BottomNavItem("Vente", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, "tab_vente", 1),
        BottomNavItem("Stock", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "tab_stock", 2),
        BottomNavItem("Crédits", Icons.Filled.CreditCard, Icons.Outlined.CreditCard, "tab_credits", 3)
    )

    // For Caissier, only show the Vente tab
    val navItems = if (currentUserRole == UserRole.GERANT) {
        allNavItems
    } else {
        listOf(
            BottomNavItem("Vente POS", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, "tab_vente", 1)
        )
    }

    // Thermal Receipt / Ticket Dialog after completing sale
    lastCompletedSale?.let { sale ->
        ReceiptPrinterDialog(
            sale = sale,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }

    // POS Cart Sheet
    if (isCartOpen) {
        CartAndCheckoutSheet(
            cartItems = cartItems,
            sheetState = cartSheetState,
            onAddToCart = { viewModel.addToCart(it) },
            onRemoveFromCart = { viewModel.removeFromCart(it) },
            onClearCart = { viewModel.clearCart() },
            onCompleteSale = { clientName, clientPhone, method, isCredit, discountAmount, discountNote ->
                viewModel.completeSale(clientName, clientPhone, method, isCredit, discountAmount, discountNote) { completedSale ->
                    if (ReceiptPrintHelper.isAutoPrintEnabled(context)) {
                        ReceiptPrintHelper.printDirectReceipt(context, completedSale)
                    }
                    coroutineScope.launch { cartSheetState.hide() }.invokeOnCompletion {
                        isCartOpen = false
                    }
                }
            },
            onDismiss = {
                coroutineScope.launch { cartSheetState.hide() }.invokeOnCompletion {
                    isCartOpen = false
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .shadow(12.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = DarkSurfaceElevated,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = selectedTabIndex == item.tabIndex
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = item.tabIndex },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TextDark,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTabIndex) {
                0 -> HomeScreen(
                    stats = stats,
                    recentSales = recentSales,
                    allSales = allSales,
                    allProducts = products,
                    suppliers = suppliers,
                    purchaseOrders = purchaseOrders,
                    activeCashSession = activeCashSession,
                    pastCashSessions = pastCashSessions,
                    criticalProducts = lowStockProducts,
                    onRestockProduct = { product, qty ->
                        viewModel.quickRestockFromAlert(product, qty)
                    },
                    onUpdateProductThreshold = { product, thresh ->
                        viewModel.updateProductThreshold(product, thresh)
                    },
                    onOpenCashSession = { cashier, floatAmt, note ->
                        viewModel.openCashSession(cashier, floatAmt, note)
                    },
                    onCloseCashSession = { session, actual, note ->
                        viewModel.closeCashSession(session, actual, note)
                    },
                    onAddSupplier = { name, phone, addr, cat, notes ->
                        viewModel.addSupplier(name, phone, addr, cat, notes)
                    },
                    onDeleteSupplier = { supplier ->
                        viewModel.deleteSupplier(supplier)
                    },
                    onSettleSupplierDebt = { supplier, amt ->
                        viewModel.settleSupplierDebt(supplier, amt)
                    },
                    onCreatePurchaseOrder = { supplier, product, qty, cost, isPaid, method ->
                        viewModel.createPurchaseOrder(supplier, product, qty, cost, isPaid, method)
                    },
                    onExportBackup = { callback ->
                        viewModel.exportDatabaseJson(callback)
                    },
                    onImportBackup = { json, callback ->
                        viewModel.importDatabaseJson(json, callback)
                    },
                    onFactoryReset = { callback ->
                        viewModel.factoryResetEntireDatabase {
                            callback()
                        }
                    },
                    onNavigateToTab = { selectedTabIndex = it },
                    onQuickNewSale = { selectedTabIndex = 1 }
                )

                1 -> VenteScreen(
                    products = products,
                    cartItems = cartItems,
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery,
                    onCategorySelected = { viewModel.setProductCategory(it) },
                    onSearchQueryChanged = { viewModel.setProductSearchQuery(it) },
                    onAddToCart = { viewModel.addToCart(it) },
                    onOpenCart = { isCartOpen = true }
                )

                2 -> StockScreen(
                    products = products,
                    stockMovements = stockMovements,
                    stats = stats,
                    selectedFilterTab = stockFilterTab,
                    onFilterTabSelected = { viewModel.setStockFilterTab(it) },
                    onSaveProduct = { id, name, cat, buyP, sellP, stock, alert, icon, bar ->
                        viewModel.saveProduct(id, name, cat, buyP, sellP, stock, alert, icon, bar)
                    },
                    onDeleteProduct = { viewModel.deleteProduct(it) },
                    onAdjustStock = { product, delta -> viewModel.adjustStock(product, delta) },
                    onRestock = { product, qty, note -> viewModel.restockProduct(product, qty, note) },
                    onDeclareLoss = { product, qty, note -> viewModel.recordStockLoss(product, qty, note) }
                )

                3 -> CreditsScreen(
                    credits = credits,
                    paymentLogs = paymentLogs,
                    stats = stats,
                    onSettleDebt = { credit, amount, method, note ->
                        viewModel.settleDebt(credit, amount, method, note)
                    },
                    onCloseDebtCompletely = { credit, method, note ->
                        viewModel.closeDebtCompletely(credit, method, note)
                    },
                    onEditCredit = { credit, name, phone, amount, note ->
                        viewModel.editCredit(credit, name, phone, amount, note)
                    },
                    onDeleteCredit = { credit ->
                        viewModel.deleteCredit(credit)
                    },
                    onAddNewCredit = { name, phone, amount, note ->
                        viewModel.addNewCredit(name, phone, amount, note)
                    }
                )
            }

            // Top-anchored In-App Stock Alert Banner
            StockAlertBanner(
                notification = activeStockAlert,
                onDismiss = { viewModel.dismissStockAlert() },
                onNavigateToStock = {
                    viewModel.setStockFilterTab("Stock Faible")
                    selectedTabIndex = 2
                },
                onQuickRestock = { product, qty ->
                    viewModel.quickRestockFromAlert(product, qty)
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .zIndex(20f)
            )
        }
    }
}
