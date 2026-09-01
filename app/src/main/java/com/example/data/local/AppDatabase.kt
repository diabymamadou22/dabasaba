package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        StockMovementEntity::class,
        SaleTransactionEntity::class,
        ClientCreditEntity::class,
        CreditPaymentLogEntity::class,
        CashRegisterSessionEntity::class,
        SupplierEntity::class,
        PurchaseOrderEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun shopDao(): ShopDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dabasaba_boutique_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.shopDao())
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.shopDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: ShopDao) {
                // 1. Initial Products
                val initialProducts = listOf(
                    ProductEntity(
                        name = "Youki Orange 33cl",
                        category = "Boissons",
                        buyPrice = 300.0,
                        sellPrice = 500.0,
                        stockQuantity = 48,
                        minStockAlert = 10,
                        iconKey = "drink",
                        barcode = "600123456789"
                    ),
                    ProductEntity(
                        name = "Lait Nido 400g",
                        category = "Alimentation",
                        buyPrice = 2200.0,
                        sellPrice = 2750.0,
                        stockQuantity = 15,
                        minStockAlert = 10,
                        iconKey = "milk",
                        barcode = "761303512345"
                    ),
                    ProductEntity(
                        name = "Riz Parfumé 5kg",
                        category = "Alimentation",
                        buyPrice = 3500.0,
                        sellPrice = 4500.0,
                        stockQuantity = 32,
                        minStockAlert = 10,
                        iconKey = "rice",
                        barcode = "893500123456"
                    ),
                    ProductEntity(
                        name = "Savon Omo 200g",
                        category = "Hygiène",
                        buyPrice = 450.0,
                        sellPrice = 650.0,
                        stockQuantity = 4,
                        minStockAlert = 10,
                        iconKey = "soap",
                        barcode = "871256123456"
                    ),
                    ProductEntity(
                        name = "Huile Végétale 1L",
                        category = "Alimentation",
                        buyPrice = 1400.0,
                        sellPrice = 1800.0,
                        stockQuantity = 0,
                        minStockAlert = 5,
                        iconKey = "oil",
                        barcode = "619123456789"
                    ),
                    ProductEntity(
                        name = "Parfum Brume 100ml",
                        category = "Cosmétique",
                        buyPrice = 2500.0,
                        sellPrice = 3500.0,
                        stockQuantity = 12,
                        minStockAlert = 5,
                        iconKey = "perfume",
                        barcode = "334890123456"
                    ),
                    ProductEntity(
                        name = "Sucre Blanc 1kg",
                        category = "Alimentation",
                        buyPrice = 600.0,
                        sellPrice = 750.0,
                        stockQuantity = 55,
                        minStockAlert = 15,
                        iconKey = "sugar",
                        barcode = "600987654321"
                    ),
                    ProductEntity(
                        name = "Thé Vert de Chine 25g",
                        category = "Boissons",
                        buyPrice = 800.0,
                        sellPrice = 1000.0,
                        stockQuantity = 22,
                        minStockAlert = 8,
                        iconKey = "tea",
                        barcode = "690123456789"
                    ),
                    ProductEntity(
                        name = "Pâte Dentifrice 75ml",
                        category = "Hygiène",
                        buyPrice = 400.0,
                        sellPrice = 600.0,
                        stockQuantity = 3,
                        minStockAlert = 6,
                        iconKey = "hygiene",
                        barcode = "871895123456"
                    ),
                    ProductEntity(
                        name = "Café Touba Sachet",
                        category = "Boissons",
                        buyPrice = 100.0,
                        sellPrice = 150.0,
                        stockQuantity = 0,
                        minStockAlert = 20,
                        iconKey = "coffee",
                        barcode = "604123456789"
                    )
                )
                dao.insertAllProducts(initialProducts)

                // 2. Initial Client Credits
                val initialCredits = listOf(
                    ClientCreditEntity(
                        clientName = "Mariam Coulibaly",
                        phoneNumber = "+223 76 12 34 56",
                        totalDue = 47500.0,
                        purchaseCount = 3,
                        lastPurchaseDate = System.currentTimeMillis() - 3 * 86400000L,
                        accentColor = "#0C6B58",
                        notes = "Cliente fidèle de Lafiabougou"
                    ),
                    ClientCreditEntity(
                        clientName = "Boubacar Traoré",
                        phoneNumber = "+223 65 98 77 21",
                        totalDue = 18200.0,
                        purchaseCount = 2,
                        lastPurchaseDate = System.currentTimeMillis() - 6 * 86400000L,
                        accentColor = "#7C3AED",
                        notes = "Paiement promis fin de semaine"
                    ),
                    ClientCreditEntity(
                        clientName = "Fatoumata Diallo",
                        phoneNumber = "+223 79 44 55 03",
                        totalDue = 85000.0,
                        purchaseCount = 5,
                        lastPurchaseDate = System.currentTimeMillis() - 8 * 86400000L,
                        accentColor = "#EF4444",
                        notes = "Gérante du salon voisin"
                    ),
                    ClientCreditEntity(
                        clientName = "Moussa Diarra",
                        phoneNumber = "+223 70 11 22 33",
                        totalDue = 28750.0,
                        purchaseCount = 2,
                        lastPurchaseDate = System.currentTimeMillis() - 10 * 86400000L,
                        accentColor = "#F59E0B",
                        notes = "Voisin de quartier"
                    ),
                    ClientCreditEntity(
                        clientName = "Aminata Traoré",
                        phoneNumber = "+223 66 54 32 10",
                        totalDue = 15000.0,
                        purchaseCount = 1,
                        lastPurchaseDate = System.currentTimeMillis() - 13 * 86400000L,
                        accentColor = "#3B82F6",
                        notes = "Avance reçue"
                    )
                )
                dao.insertAllCredits(initialCredits)

                // 3. Initial Sales Transactions
                val now = System.currentTimeMillis()
                val initialSales = listOf(
                    SaleTransactionEntity(
                        clientName = "Mariam Coulibaly",
                        clientPhone = "+223 76 12 34 56",
                        itemsSummary = "Lait Nido × 3, Savon × 2",
                        totalAmount = 8750.0,
                        profitAmount = 2050.0,
                        paymentMethod = "ESPECES",
                        paymentStatus = "PAID",
                        timestamp = now - 28 * 60 * 1000L,
                        isCredit = false,
                        isSettled = true
                    ),
                    SaleTransactionEntity(
                        clientName = "Boubacar Traoré",
                        clientPhone = "+223 65 98 77 21",
                        itemsSummary = "Boisson Youki × 6",
                        totalAmount = 3600.0,
                        profitAmount = 1200.0,
                        paymentMethod = "ORANGE_MONEY",
                        paymentStatus = "PAID",
                        timestamp = now - 105 * 60 * 1000L,
                        isCredit = false,
                        isSettled = true
                    ),
                    SaleTransactionEntity(
                        clientName = "Client Comptant",
                        clientPhone = "",
                        itemsSummary = "Riz Parfumé 5kg × 1, Huile 1L × 2",
                        totalAmount = 8100.0,
                        profitAmount = 1800.0,
                        paymentMethod = "WAVE",
                        paymentStatus = "PAID",
                        timestamp = now - 240 * 60 * 1000L,
                        isCredit = false,
                        isSettled = true
                    ),
                    SaleTransactionEntity(
                        clientName = "Fatoumata Diallo",
                        clientPhone = "+223 79 44 55 03",
                        itemsSummary = "Parfum Brume × 2",
                        totalAmount = 7000.0,
                        profitAmount = 2000.0,
                        paymentMethod = "CREDIT",
                        paymentStatus = "CREDIT_PENDING",
                        timestamp = now - 320 * 60 * 1000L,
                        isCredit = true,
                        isSettled = false
                    ),
                    SaleTransactionEntity(
                        clientName = "Ousmane Koné",
                        clientPhone = "+223 72 33 44 55",
                        itemsSummary = "Sucre 1kg × 4, Thé Vert × 2",
                        totalAmount = 5000.0,
                        profitAmount = 1000.0,
                        paymentMethod = "ESPECES",
                        paymentStatus = "PAID",
                        timestamp = now - 450 * 60 * 1000L,
                        isCredit = false,
                        isSettled = true
                    )
                )
                dao.insertAllSales(initialSales)

                // 4. Initial Stock Movements
                val initialMovements = listOf(
                    StockMovementEntity(
                        productId = 1,
                        productName = "Youki Orange 33cl",
                        deltaQuantity = 50,
                        remainingStock = 50,
                        reason = "APPROVISIONNEMENT",
                        timestamp = now - 24 * 3600 * 1000L,
                        note = "Arrivage Brasserie"
                    ),
                    StockMovementEntity(
                        productId = 2,
                        productName = "Lait Nido 400g",
                        deltaQuantity = 20,
                        remainingStock = 20,
                        reason = "APPROVISIONNEMENT",
                        timestamp = now - 20 * 3600 * 1000L,
                        note = "Livraison grossiste"
                    ),
                    StockMovementEntity(
                        productId = 2,
                        productName = "Lait Nido 400g",
                        deltaQuantity = -3,
                        remainingStock = 17,
                        reason = "VENTE",
                        timestamp = now - 28 * 60 * 1000L,
                        note = "Vente client: Mariam Coulibaly"
                    ),
                    StockMovementEntity(
                        productId = 5,
                        productName = "Huile Végétale 1L",
                        deltaQuantity = -2,
                        remainingStock = 0,
                        reason = "VENTE",
                        timestamp = now - 240 * 60 * 1000L,
                        note = "Rupture de stock suite à vente"
                    ),
                    StockMovementEntity(
                        productId = 4,
                        productName = "Savon Omo 200g",
                        deltaQuantity = -1,
                        remainingStock = 4,
                        reason = "PERTE",
                        timestamp = now - 5 * 3600 * 1000L,
                        note = "Emballage endommagé / perte"
                    )
                )
                dao.insertAllStockMovements(initialMovements)

                // 5. Initial Cash Register Session (Ouverture du jour)
                val initialSession = CashRegisterSessionEntity(
                    cashierName = "Mamadou (Gérant)",
                    openedAt = now - 8 * 3600 * 1000L,
                    openingCash = 25000.0,
                    expectedCash = 25000.0 + 8750.0 + 5000.0, // 38750.0
                    actualCash = 0.0,
                    cashDifference = 0.0,
                    totalCashSales = 13750.0,
                    totalMobileSales = 11700.0,
                    totalCreditSales = 7000.0,
                    totalCreditsRecovered = 0.0,
                    isOpen = true,
                    notes = "Session matinale ouverte avec fond de caisse standard"
                )
                dao.insertCashSession(initialSession)

                // 6. Initial Suppliers
                val initialSuppliers = listOf(
                    SupplierEntity(
                        name = "Grossiste Bramali Sogoniko",
                        phoneNumber = "+223 76 12 34 56",
                        address = "Zone Industrielle Sogoniko, Bamako",
                        category = "Boissons & Jus",
                        totalDebtToSupplier = 0.0,
                        notes = "Fournisseur officiel boissons gazeuses et eau minérale"
                    ),
                    SupplierEntity(
                        name = "Établissements Diarra & Frères",
                        phoneNumber = "+223 66 98 76 54",
                        address = "Grand Marché Rue 24, Bamako",
                        category = "Alimentation Générale",
                        totalDebtToSupplier = 35000.0,
                        notes = "Riz, sucre, huile végétale, farine de blé"
                    ),
                    SupplierEntity(
                        name = "Comptoir Sahel Hygiène",
                        phoneNumber = "+223 70 44 55 66",
                        address = "Lafiabougou ACI 2000",
                        category = "Hygiène & Entretien",
                        totalDebtToSupplier = 0.0,
                        notes = "Savons, détergents, dentifrice et cosmétiques"
                    )
                )
                dao.insertAllSuppliers(initialSuppliers)
            }
        }
    }
}
