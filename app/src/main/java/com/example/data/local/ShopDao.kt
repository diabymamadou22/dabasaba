package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {

    // --- PRODUCTS ---
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET stockQuantity = stockQuantity - :quantity WHERE id = :productId")
    suspend fun decreaseProductStock(productId: Int, quantity: Int)

    @Query("UPDATE products SET stockQuantity = stockQuantity + :quantity WHERE id = :productId")
    suspend fun increaseProductStock(productId: Int, quantity: Int)

    // --- STOCK MOVEMENTS ---
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllStockMovements(): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProduct(productId: Int): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStockMovements(movements: List<StockMovementEntity>)

    // --- SALES ---
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleTransactionEntity>>

    @Query("SELECT * FROM sales ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSales(limit: Int): Flow<List<SaleTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSales(sales: List<SaleTransactionEntity>)

    // --- CLIENT CREDITS ---
    @Query("SELECT * FROM client_credits WHERE isFullySettled = 0 ORDER BY totalDue DESC")
    fun getActiveCredits(): Flow<List<ClientCreditEntity>>

    @Query("SELECT * FROM client_credits ORDER BY totalDue DESC")
    fun getAllCredits(): Flow<List<ClientCreditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredit(credit: ClientCreditEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCredits(credits: List<ClientCreditEntity>)

    @Update
    suspend fun updateCredit(credit: ClientCreditEntity)

    @Delete
    suspend fun deleteCredit(credit: ClientCreditEntity)

    @Query("UPDATE client_credits SET totalDue = totalDue - :amount WHERE id = :creditId")
    suspend fun reduceCreditDue(creditId: Int, amount: Double)

    // --- PAYMENT LOGS ---
    @Query("SELECT * FROM credit_payments ORDER BY timestamp DESC")
    fun getAllCreditPayments(): Flow<List<CreditPaymentLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditPayment(payment: CreditPaymentLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCreditPayments(payments: List<CreditPaymentLogEntity>)

    // --- CASH REGISTER SESSIONS (CLÔTURE DE CAISSE) ---
    @Query("SELECT * FROM cash_register_sessions ORDER BY openedAt DESC")
    fun getAllCashSessions(): Flow<List<CashRegisterSessionEntity>>

    @Query("SELECT * FROM cash_register_sessions WHERE isOpen = 1 ORDER BY openedAt DESC LIMIT 1")
    fun getActiveCashSession(): Flow<CashRegisterSessionEntity?>

    @Query("SELECT * FROM cash_register_sessions WHERE isOpen = 1 ORDER BY openedAt DESC LIMIT 1")
    suspend fun getActiveCashSessionSync(): CashRegisterSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashSession(session: CashRegisterSessionEntity): Long

    @Update
    suspend fun updateCashSession(session: CashRegisterSessionEntity)

    // --- SUPPLIERS ---
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSuppliers(suppliers: List<SupplierEntity>)

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    @Query("UPDATE suppliers SET totalDebtToSupplier = totalDebtToSupplier - :amount WHERE id = :supplierId")
    suspend fun reduceSupplierDebt(supplierId: Int, amount: Double)

    @Query("UPDATE suppliers SET totalDebtToSupplier = totalDebtToSupplier + :amount WHERE id = :supplierId")
    suspend fun increaseSupplierDebt(supplierId: Int, amount: Double)

    // --- PURCHASE ORDERS (COMMANDES FOURNISSEURS) ---
    @Query("SELECT * FROM purchase_orders ORDER BY timestamp DESC")
    fun getAllPurchaseOrders(): Flow<List<PurchaseOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseOrder(order: PurchaseOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPurchaseOrders(orders: List<PurchaseOrderEntity>)

    // --- BACKUP & RESTORE UTILITIES ---
    @Query("DELETE FROM products")
    suspend fun clearAllProducts()

    @Query("DELETE FROM sales")
    suspend fun clearAllSales()

    @Query("DELETE FROM client_credits")
    suspend fun clearAllCredits()

    @Query("DELETE FROM credit_payments")
    suspend fun clearAllCreditPayments()

    @Query("DELETE FROM stock_movements")
    suspend fun clearAllStockMovements()

    @Query("DELETE FROM suppliers")
    suspend fun clearAllSuppliers()

    @Query("DELETE FROM purchase_orders")
    suspend fun clearAllPurchaseOrders()

    @Query("DELETE FROM cash_register_sessions")
    suspend fun clearAllCashSessions()

    @Query("DELETE FROM sqlite_sequence")
    suspend fun resetAllSequences()

    @androidx.room.Transaction
    suspend fun clearAllDatabaseData() {
        clearAllProducts()
        clearAllSales()
        clearAllCredits()
        clearAllCreditPayments()
        clearAllStockMovements()
        clearAllCashSessions()
        clearAllSuppliers()
        clearAllPurchaseOrders()
        try {
            resetAllSequences()
        } catch (_: Exception) {}
    }

    @Query("SELECT * FROM products")
    suspend fun getAllProductsSync(): List<ProductEntity>

    @Query("SELECT * FROM sales")
    suspend fun getAllSalesSync(): List<SaleTransactionEntity>

    @Query("SELECT * FROM client_credits")
    suspend fun getAllCreditsSync(): List<ClientCreditEntity>

    @Query("SELECT * FROM credit_payments")
    suspend fun getAllCreditPaymentsSync(): List<CreditPaymentLogEntity>

    @Query("SELECT * FROM stock_movements")
    suspend fun getAllStockMovementsSync(): List<StockMovementEntity>

    @Query("SELECT * FROM suppliers")
    suspend fun getAllSuppliersSync(): List<SupplierEntity>

    @Query("SELECT * FROM purchase_orders")
    suspend fun getAllPurchaseOrdersSync(): List<PurchaseOrderEntity>
}
