package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String, // "Boissons", "Alimentation", "Hygiène", "Cosmétique", "Divers"
    val buyPrice: Double,
    val sellPrice: Double,
    val stockQuantity: Int,
    val minStockAlert: Int = 5,
    val iconKey: String = "box", // "drink", "milk", "rice", "soap", "oil", "perfume", "sugar", "tea", "coffee", "hygiene"
    val barcode: String = ""
)

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val productName: String,
    val deltaQuantity: Int, // e.g. +20 for restock, -2 for sale or damage
    val remainingStock: Int,
    val reason: String, // "VENTE", "APPROVISIONNEMENT", "AJUSTEMENT_MANUEL", "PERTE"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "sales")
data class SaleTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val clientName: String,
    val clientPhone: String = "",
    val itemsSummary: String,
    val totalAmount: Double,
    val profitAmount: Double,
    val discountAmount: Double = 0.0,
    val discountNote: String = "",
    val paymentMethod: String, // "ESPECES", "ORANGE_MONEY", "WAVE", "MOOV_MONEY", "CREDIT"
    val timestamp: Long = System.currentTimeMillis(),
    val paymentStatus: String = "PAID", // "PAID", "CREDIT_PENDING", "PARTIALLY_PAID", "SETTLED", "CANCELLED"
    val isCredit: Boolean = false,
    val isSettled: Boolean = false
)

typealias SaleEntity = SaleTransactionEntity

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val address: String = "",
    val category: String = "Général", // e.g. "Boissons", "Alimentation", "Grossiste"
    val totalDebtToSupplier: Double = 0.0, // Montant dû au fournisseur
    val notes: String = "",
    val lastOrderDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchase_orders")
data class PurchaseOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val supplierId: Int,
    val supplierName: String,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val unitCost: Double,
    val totalCost: Double,
    val isPaid: Boolean = true, // true = Payé comptant, false = Facture en attente (dette fournisseur)
    val paymentMethod: String = "ESPECES", // "ESPECES", "ORANGE_MONEY", "WAVE", "VIREMENT"
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "client_credits")
data class ClientCreditEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val clientName: String,
    val phoneNumber: String,
    val totalDue: Double,
    val purchaseCount: Int = 1,
    val lastPurchaseDate: Long = System.currentTimeMillis(),
    val accentColor: String = "#D4AF37",
    val notes: String = "",
    val isFullySettled: Boolean = false
)

@Entity(tableName = "credit_payments")
data class CreditPaymentLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val clientId: Int,
    val clientName: String,
    val amountPaid: Double,
    val paymentMethod: String, // "ESPECES", "ORANGE_MONEY", "WAVE"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "cash_register_sessions")
data class CashRegisterSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cashierName: String = "Mamadou (Gérant)",
    val openedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val openingCash: Double = 25000.0, // Fond de caisse initial
    val expectedCash: Double = 0.0,
    val actualCash: Double = 0.0,
    val cashDifference: Double = 0.0, // actualCash - expectedCash (Surplus / Manquant)
    val totalCashSales: Double = 0.0,
    val totalMobileSales: Double = 0.0,
    val totalCreditSales: Double = 0.0,
    val totalCreditsRecovered: Double = 0.0,
    val isOpen: Boolean = true,
    val notes: String = ""
)

