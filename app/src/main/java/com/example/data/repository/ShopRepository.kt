package com.example.data.repository

import com.example.data.local.ClientCreditEntity
import com.example.data.local.CreditPaymentLogEntity
import com.example.data.local.ProductEntity
import com.example.data.local.PurchaseOrderEntity
import com.example.data.local.SaleTransactionEntity
import com.example.data.local.ShopDao
import com.example.data.local.StockMovementEntity
import com.example.data.local.SupplierEntity
import kotlinx.coroutines.flow.Flow

class ShopRepository(private val dao: ShopDao) {

    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    val allSales: Flow<List<SaleTransactionEntity>> = dao.getAllSales()
    val recentSales: Flow<List<SaleTransactionEntity>> = dao.getRecentSales(15)
    val activeCredits: Flow<List<ClientCreditEntity>> = dao.getActiveCredits()
    val allCredits: Flow<List<ClientCreditEntity>> = dao.getAllCredits()
    val paymentLogs: Flow<List<CreditPaymentLogEntity>> = dao.getAllCreditPayments()
    val allStockMovements: Flow<List<StockMovementEntity>> = dao.getAllStockMovements()
    val allCashSessions: Flow<List<com.example.data.local.CashRegisterSessionEntity>> = dao.getAllCashSessions()
    val activeCashSession: Flow<com.example.data.local.CashRegisterSessionEntity?> = dao.getActiveCashSession()
    val allSuppliers: Flow<List<SupplierEntity>> = dao.getAllSuppliers()
    val allPurchaseOrders: Flow<List<PurchaseOrderEntity>> = dao.getAllPurchaseOrders()

    suspend fun addProduct(product: ProductEntity): Long {
        val id = dao.insertProduct(product)
        if (product.stockQuantity > 0) {
            dao.insertStockMovement(
                StockMovementEntity(
                    productId = id.toInt(),
                    productName = product.name,
                    deltaQuantity = product.stockQuantity,
                    remainingStock = product.stockQuantity,
                    reason = "APPROVISIONNEMENT",
                    note = "Stock initial lors de la création"
                )
            )
        }
        return id
    }

    suspend fun updateProduct(product: ProductEntity) = dao.updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = dao.deleteProduct(product)

    suspend fun adjustStock(product: ProductEntity, delta: Int, reason: String = "AJUSTEMENT_MANUEL") {
        if (delta > 0) {
            dao.increaseProductStock(product.id, delta)
        } else if (delta < 0) {
            dao.decreaseProductStock(product.id, -delta)
        }
        val newStock = (product.stockQuantity + delta).coerceAtLeast(0)
        dao.insertStockMovement(
            StockMovementEntity(
                productId = product.id,
                productName = product.name,
                deltaQuantity = delta,
                remainingStock = newStock,
                reason = reason,
                note = if (delta > 0) "Ajout de $delta unité(s)" else "Retrait de ${-delta} unité(s)"
            )
        )
    }

    suspend fun recordSale(
        sale: SaleTransactionEntity,
        purchasedItems: List<Pair<ProductEntity, Int>>
    ) {
        dao.insertSale(sale)
        for ((product, qty) in purchasedItems) {
            dao.decreaseProductStock(product.id, qty)
            val newStock = (product.stockQuantity - qty).coerceAtLeast(0)
            dao.insertStockMovement(
                StockMovementEntity(
                    productId = product.id,
                    productName = product.name,
                    deltaQuantity = -qty,
                    remainingStock = newStock,
                    reason = "VENTE",
                    note = "Vente client: ${sale.clientName}"
                )
            )
        }

        // Update active session counters
        val activeSession = dao.getActiveCashSessionSync()
        if (activeSession != null) {
            val updated = when {
                sale.isCredit -> activeSession.copy(
                    totalCreditSales = activeSession.totalCreditSales + sale.totalAmount
                )
                sale.paymentMethod == "ESPECES" -> activeSession.copy(
                    totalCashSales = activeSession.totalCashSales + sale.totalAmount,
                    expectedCash = activeSession.expectedCash + sale.totalAmount
                )
                else -> activeSession.copy(
                    totalMobileSales = activeSession.totalMobileSales + sale.totalAmount
                )
            }
            dao.updateCashSession(updated)
        }
    }

    suspend fun addOrUpdateCredit(credit: ClientCreditEntity) = dao.insertCredit(credit)
    suspend fun updateCredit(credit: ClientCreditEntity) = dao.updateCredit(credit)
    suspend fun deleteCredit(credit: ClientCreditEntity) = dao.deleteCredit(credit)

    suspend fun settleCreditPayment(
        credit: ClientCreditEntity,
        amountPaid: Double,
        paymentMethod: String,
        note: String
    ) {
        val newDue = (credit.totalDue - amountPaid).coerceAtLeast(0.0)
        val updatedCredit = credit.copy(
            totalDue = newDue,
            isFullySettled = newDue <= 0.0
        )
        dao.updateCredit(updatedCredit)

        val paymentLog = CreditPaymentLogEntity(
            clientId = credit.id,
            clientName = credit.clientName,
            amountPaid = amountPaid,
            paymentMethod = paymentMethod,
            timestamp = System.currentTimeMillis(),
            note = note
        )
        dao.insertCreditPayment(paymentLog)

        // Update active session if cash payment
        val activeSession = dao.getActiveCashSessionSync()
        if (activeSession != null) {
            val updated = activeSession.copy(
                totalCreditsRecovered = activeSession.totalCreditsRecovered + amountPaid,
                expectedCash = if (paymentMethod == "ESPECES") activeSession.expectedCash + amountPaid else activeSession.expectedCash
            )
            dao.updateCashSession(updated)
        }
    }

    // --- CASH SESSIONS ---
    suspend fun openCashSession(cashierName: String, openingCash: Double, notes: String = ""): Long {
        val active = dao.getActiveCashSessionSync()
        if (active != null) {
            // Close old active session
            dao.updateCashSession(active.copy(isOpen = false, closedAt = System.currentTimeMillis()))
        }
        val newSession = com.example.data.local.CashRegisterSessionEntity(
            cashierName = cashierName,
            openedAt = System.currentTimeMillis(),
            openingCash = openingCash,
            expectedCash = openingCash,
            isOpen = true,
            notes = notes
        )
        return dao.insertCashSession(newSession)
    }

    suspend fun closeCashSession(session: com.example.data.local.CashRegisterSessionEntity, actualCash: Double, notes: String) {
        val diff = actualCash - session.expectedCash
        val updated = session.copy(
            actualCash = actualCash,
            cashDifference = diff,
            closedAt = System.currentTimeMillis(),
            isOpen = false,
            notes = notes
        )
        dao.updateCashSession(updated)
    }

    // --- SUPPLIERS & PURCHASE ORDERS ---
    suspend fun addSupplier(supplier: SupplierEntity): Long = dao.insertSupplier(supplier)
    suspend fun updateSupplier(supplier: SupplierEntity) = dao.updateSupplier(supplier)
    suspend fun deleteSupplier(supplier: SupplierEntity) = dao.deleteSupplier(supplier)

    suspend fun settleSupplierDebt(supplierId: Int, amount: Double) {
        dao.reduceSupplierDebt(supplierId, amount)
    }

    suspend fun recordPurchaseOrder(
        order: PurchaseOrderEntity,
        updateStock: Boolean = true
    ): Long {
        val orderId = dao.insertPurchaseOrder(order)
        if (updateStock && order.productId > 0) {
            val product = dao.getProductById(order.productId)
            if (product != null) {
                dao.increaseProductStock(product.id, order.quantity)
                val newStock = product.stockQuantity + order.quantity
                dao.insertStockMovement(
                    StockMovementEntity(
                        productId = product.id,
                        productName = product.name,
                        deltaQuantity = order.quantity,
                        remainingStock = newStock,
                        reason = "APPROVISIONNEMENT",
                        note = "Achat Fournisseur: ${order.supplierName} (${if (order.isPaid) "Payé" else "Dette"})"
                    )
                )
            }
        }
        if (!order.isPaid) {
            dao.increaseSupplierDebt(order.supplierId, order.totalCost)
        }
        return orderId
    }

    // --- BACKUP & RESTORE ---
    suspend fun getAllProductsSync() = dao.getAllProductsSync()
    suspend fun getAllSalesSync() = dao.getAllSalesSync()
    suspend fun getAllCreditsSync() = dao.getAllCreditsSync()
    suspend fun getAllCreditPaymentsSync() = dao.getAllCreditPaymentsSync()
    suspend fun getAllStockMovementsSync() = dao.getAllStockMovementsSync()
    suspend fun getAllSuppliersSync() = dao.getAllSuppliersSync()
    suspend fun getAllPurchaseOrdersSync() = dao.getAllPurchaseOrdersSync()

    suspend fun restoreDatabase(
        products: List<ProductEntity>,
        sales: List<SaleTransactionEntity>,
        credits: List<ClientCreditEntity>,
        payments: List<CreditPaymentLogEntity>,
        movements: List<StockMovementEntity>,
        suppliers: List<SupplierEntity> = emptyList(),
        orders: List<PurchaseOrderEntity> = emptyList()
    ) {
        dao.clearAllProducts()
        dao.clearAllSales()
        dao.clearAllCredits()
        dao.clearAllCreditPayments()
        dao.clearAllStockMovements()
        dao.clearAllSuppliers()
        dao.clearAllPurchaseOrders()

        if (products.isNotEmpty()) dao.insertAllProducts(products)
        if (sales.isNotEmpty()) dao.insertAllSales(sales)
        if (credits.isNotEmpty()) dao.insertAllCredits(credits)
        if (payments.isNotEmpty()) dao.insertAllCreditPayments(payments)
        if (movements.isNotEmpty()) dao.insertAllStockMovements(movements)
        if (suppliers.isNotEmpty()) dao.insertAllSuppliers(suppliers)
        if (orders.isNotEmpty()) dao.insertAllPurchaseOrders(orders)
    }

    suspend fun clearEntireDatabase() {
        dao.clearAllDatabaseData()
    }
}

