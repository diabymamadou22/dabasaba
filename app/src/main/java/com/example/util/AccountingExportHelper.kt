package com.example.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.data.local.CashRegisterSessionEntity
import com.example.data.local.ProductEntity
import com.example.data.local.SaleTransactionEntity
import com.example.ui.viewmodel.ShopStats
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AccountingExportHelper {

    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.FRENCH).apply {
        maximumFractionDigits = 0
    }

    private fun formatAmount(amount: Double): String {
        return "${numberFormat.format(amount)} F CFA"
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH)
        return sdf.format(Date(timestamp))
    }

    private fun formatDateOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)
        return sdf.format(Date(timestamp))
    }

    /**
     * Exporte les ventes au format CSV compatible Excel (avec BOM UTF-8 et séparateurs point-virgule)
     */
    fun exportSalesToCsv(
        context: Context,
        sales: List<SaleTransactionEntity>,
        isManager: Boolean
    ) {
        try {
            val sb = StringBuilder()
            // UTF-8 BOM so Excel opens accents without weird characters
            sb.append("\uFEFF")

            // Header line
            if (isManager) {
                sb.append("N° Vente;Date & Heure;Client;Téléphone;Articles Vendus;Mode Paiement;Statut;Montant Vente (F CFA);Remise (F CFA);Bénéfice Net (F CFA)\n")
            } else {
                sb.append("N° Vente;Date & Heure;Client;Téléphone;Articles Vendus;Mode Paiement;Statut;Montant Vente (F CFA);Remise (F CFA)\n")
            }

            for (s in sales) {
                val cleanItems = s.itemsSummary.replace(";", ",").replace("\n", " | ")
                val cleanClient = s.clientName.replace(";", ",")
                val cleanPhone = s.clientPhone.replace(";", ",")

                sb.append("${s.id};")
                sb.append("${formatDate(s.timestamp)};")
                sb.append("\"$cleanClient\";")
                sb.append("\"$cleanPhone\";")
                sb.append("\"$cleanItems\";")
                sb.append("${s.paymentMethod};")
                sb.append("${if (s.isCredit) "CRÉDIT" else "COMPTANT"};")
                sb.append("${s.totalAmount.toLong()};")
                sb.append("${s.discountAmount.toLong()};")
                if (isManager) {
                    sb.append("${s.profitAmount.toLong()}\n")
                } else {
                    sb.append("\n")
                }
            }

            shareCsvFile(context, "Ventes_DabaSaba_${System.currentTimeMillis()}.csv", sb.toString(), "Export des Ventes DabaSaba (CSV / Excel)")
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur export CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Exporte l'inventaire complet du stock en CSV compatible Excel
     */
    fun exportInventoryToCsv(
        context: Context,
        products: List<ProductEntity>,
        isManager: Boolean
    ) {
        try {
            val sb = StringBuilder()
            sb.append("\uFEFF")

            if (isManager) {
                sb.append("ID;Code-barres;Désignation Produit;Catégorie;Quantité Stock;Seuil Alerte;Prix d'Achat (F CFA);Prix de Vente (F CFA);Valeur Stock Achat (F CFA);Valeur Stock Vente (F CFA);Marge Unitaire (F CFA)\n")
            } else {
                sb.append("ID;Code-barres;Désignation Produit;Catégorie;Quantité Stock;Seuil Alerte;Prix de Vente (F CFA)\n")
            }

            for (p in products) {
                val cleanName = p.name.replace(";", ",")
                val cleanCat = p.category.replace(";", ",")
                val cleanBar = p.barcode.replace(";", ",")

                sb.append("${p.id};")
                sb.append("\"$cleanBar\";")
                sb.append("\"$cleanName\";")
                sb.append("\"$cleanCat\";")
                sb.append("${p.stockQuantity};")
                sb.append("${p.minStockAlert};")

                if (isManager) {
                    val stockAchat = (p.buyPrice * p.stockQuantity).toLong()
                    val stockVente = (p.sellPrice * p.stockQuantity).toLong()
                    val margeUnit = (p.sellPrice - p.buyPrice).toLong()

                    sb.append("${p.buyPrice.toLong()};")
                    sb.append("${p.sellPrice.toLong()};")
                    sb.append("$stockAchat;")
                    sb.append("$stockVente;")
                    sb.append("$margeUnit\n")
                } else {
                    sb.append("${p.sellPrice.toLong()}\n")
                }
            }

            shareCsvFile(context, "Inventaire_Stock_DabaSaba_${System.currentTimeMillis()}.csv", sb.toString(), "Export Inventaire de Stock (CSV / Excel)")
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur export inventaire: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Exporte les clôtures et sessions de caisse en CSV
     */
    fun exportCashSessionsToCsv(
        context: Context,
        sessions: List<CashRegisterSessionEntity>
    ) {
        try {
            val sb = StringBuilder()
            sb.append("\uFEFF")
            sb.append("ID Session;Caissier;Date Ouverture;Date Clôture;Fond de Caisse (F);Ventes Espèces (F);Ventes Mobile (F);Ventes Crédit (F);Espèces Attendues (F);Espèces Réelles (F);Écart / Différence (F);Statut;Notes\n")

            for (s in sessions) {
                val openDate = formatDate(s.openedAt)
                val closeDate = if (s.closedAt != null) formatDate(s.closedAt) else "EN COURS"
                val statut = if (s.isOpen) "OUVERTE" else "CLÔTURÉE"
                val notes = s.notes.replace(";", ",").replace("\n", " ")

                sb.append("${s.id};")
                sb.append("\"${s.cashierName}\";")
                sb.append("$openDate;")
                sb.append("$closeDate;")
                sb.append("${s.openingCash.toLong()};")
                sb.append("${s.totalCashSales.toLong()};")
                sb.append("${s.totalMobileSales.toLong()};")
                sb.append("${s.totalCreditSales.toLong()};")
                sb.append("${s.expectedCash.toLong()};")
                sb.append("${s.actualCash.toLong()};")
                sb.append("${s.cashDifference.toLong()};")
                sb.append("$statut;")
                sb.append("\"$notes\"\n")
            }

            shareCsvFile(context, "Sessions_Caisse_DabaSaba_${System.currentTimeMillis()}.csv", sb.toString(), "Export Sessions de Caisse (CSV / Excel)")
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur export sessions: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareCsvFile(context: Context, fileName: String, content: String, title: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/comma-separated-values"
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(sendIntent, title))
    }

    /**
     * Imprime un Rapport Comptable Bilan officiel au format PDF / Imprimante
     */
    fun printAccountingReportPdf(
        context: Context,
        stats: ShopStats,
        sales: List<SaleTransactionEntity>,
        products: List<ProductEntity>,
        isManager: Boolean
    ) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Service d'impression non disponible", Toast.LENGTH_SHORT).show()
                return
            }

            val totalStockValueCost = products.sumOf { it.buyPrice * it.stockQuantity }
            val totalStockValueSale = products.sumOf { it.sellPrice * it.stockQuantity }
            val totalSalesPeriod = sales.sumOf { it.totalAmount }
            val totalProfitPeriod = sales.sumOf { it.profitAmount }
            val totalDiscountPeriod = sales.sumOf { it.discountAmount }

            val cashSales = sales.filter { it.paymentMethod == "ESPECES" }.sumOf { it.totalAmount }
            val omSales = sales.filter { it.paymentMethod == "ORANGE_MONEY" }.sumOf { it.totalAmount }
            val waveSales = sales.filter { it.paymentMethod == "WAVE" }.sumOf { it.totalAmount }
            val moovSales = sales.filter { it.paymentMethod == "MOOV_MONEY" }.sumOf { it.totalAmount }
            val creditSales = sales.filter { it.isCredit }.sumOf { it.totalAmount }

            val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Bilan Comptable DabaSaba</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        margin: 20px;
                        color: #1e293b;
                        background: #ffffff;
                        font-size: 13px;
                    }
                    .header {
                        border-bottom: 2px solid #D4AF37;
                        padding-bottom: 12px;
                        margin-bottom: 20px;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                    }
                    .title {
                        font-size: 22px;
                        font-weight: bold;
                        color: #0f172a;
                        margin: 0;
                    }
                    .subtitle {
                        color: #64748b;
                        font-size: 12px;
                        margin-top: 4px;
                    }
                    .kpi-container {
                        display: flex;
                        gap: 15px;
                        margin-bottom: 25px;
                    }
                    .kpi-card {
                        flex: 1;
                        background: #f8fafc;
                        border: 1px solid #e2e8f0;
                        border-radius: 8px;
                        padding: 12px;
                    }
                    .kpi-label {
                        font-size: 11px;
                        color: #64748b;
                        text-transform: uppercase;
                        font-weight: 600;
                    }
                    .kpi-value {
                        font-size: 18px;
                        font-weight: bold;
                        color: #0f172a;
                        margin-top: 4px;
                    }
                    .kpi-profit {
                        color: #16a34a;
                    }
                    h3 {
                        font-size: 15px;
                        color: #0f172a;
                        border-left: 4px solid #D4AF37;
                        padding-left: 8px;
                        margin-top: 20px;
                        margin-bottom: 10px;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-bottom: 20px;
                    }
                    th {
                        background: #f1f5f9;
                        color: #475569;
                        text-align: left;
                        padding: 8px;
                        font-size: 11px;
                        text-transform: uppercase;
                        border-bottom: 1px solid #cbd5e1;
                    }
                    td {
                        padding: 8px;
                        border-bottom: 1px solid #f1f5f9;
                        font-size: 12px;
                    }
                    .text-right {
                        text-align: right;
                    }
                    .badge {
                        padding: 2px 6px;
                        border-radius: 4px;
                        font-size: 10px;
                        font-weight: bold;
                    }
                    .badge-cash { background: #dcfce7; color: #166534; }
                    .badge-om { background: #ffedd5; color: #9a3412; }
                    .badge-wave { background: #e0f2fe; color: #075985; }
                    .badge-credit { background: #fee2e2; color: #991b1b; }
                    .footer {
                        margin-top: 30px;
                        border-top: 1px solid #e2e8f0;
                        padding-top: 10px;
                        text-align: center;
                        font-size: 10px;
                        color: #94a3b8;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <div>
                        <h1 class="title">BOUTIQUE DABASABA</h1>
                        <div class="subtitle">Commerce Général & Détail · Bamako, Mali</div>
                        <div class="subtitle">Date du rapport : ${formatDate(System.currentTimeMillis())}</div>
                    </div>
                    <div style="text-align: right;">
                        <div style="font-size: 16px; font-weight: bold; color: #D4AF37;">BILAN COMPTABLE & FINANCIER</div>
                        <div style="font-size: 11px; color: #64748b;">Généré par DabaSaba POS</div>
                    </div>
                </div>

                <div class="kpi-container">
                    <div class="kpi-card">
                        <div class="kpi-label">Chiffre d'Affaires Total</div>
                        <div class="kpi-value">${formatAmount(totalSalesPeriod)}</div>
                    </div>
                    ${if (isManager) """
                    <div class="kpi-card">
                        <div class="kpi-label">Bénéfice Net Réalisé</div>
                        <div class="kpi-value kpi-profit">+ ${formatAmount(totalProfitPeriod)}</div>
                    </div>
                    """ else ""}
                    <div class="kpi-card">
                        <div class="kpi-label">Crédits Clients en Cours</div>
                        <div class="kpi-value" style="color: #dc2626;">${formatAmount(stats.totalCreditDue)}</div>
                    </div>
                    <div class="kpi-card">
                        <div class="kpi-label">Valeur Vente du Stock</div>
                        <div class="kpi-value">${formatAmount(totalStockValueSale)}</div>
                    </div>
                </div>

                <h3>Ventilation des Encaissements</h3>
                <table>
                    <thead>
                        <tr>
                            <th>Mode de Règlement</th>
                            <th class="text-right">Montant Total</th>
                            <th class="text-right">Part du C.A.</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><span class="badge badge-cash">Espèces (Caisse)</span></td>
                            <td class="text-right">${formatAmount(cashSales)}</td>
                            <td class="text-right">${if (totalSalesPeriod > 0) "${(cashSales * 100 / totalSalesPeriod).toInt()}%" else "0%"}</td>
                        </tr>
                        <tr>
                            <td><span class="badge badge-om">Orange Money</span></td>
                            <td class="text-right">${formatAmount(omSales)}</td>
                            <td class="text-right">${if (totalSalesPeriod > 0) "${(omSales * 100 / totalSalesPeriod).toInt()}%" else "0%"}</td>
                        </tr>
                        <tr>
                            <td><span class="badge badge-wave">Wave</span></td>
                            <td class="text-right">${formatAmount(waveSales)}</td>
                            <td class="text-right">${if (totalSalesPeriod > 0) "${(waveSales * 100 / totalSalesPeriod).toInt()}%" else "0%"}</td>
                        </tr>
                        <tr>
                            <td><span class="badge" style="background:#f3e8ff; color:#6b21a8;">Moov Money</span></td>
                            <td class="text-right">${formatAmount(moovSales)}</td>
                            <td class="text-right">${if (totalSalesPeriod > 0) "${(moovSales * 100 / totalSalesPeriod).toInt()}%" else "0%"}</td>
                        </tr>
                        <tr>
                            <td><span class="badge badge-credit">Ventes à Crédit</span></td>
                            <td class="text-right">${formatAmount(creditSales)}</td>
                            <td class="text-right">${if (totalSalesPeriod > 0) "${(creditSales * 100 / totalSalesPeriod).toInt()}%" else "0%"}</td>
                        </tr>
                    </tbody>
                </table>

                <h3>Dernières Transactions Enregistrées (${sales.take(15).size})</h3>
                <table>
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Client</th>
                            <th>Articles</th>
                            <th>Paiement</th>
                            <th class="text-right">Montant</th>
                            ${if (isManager) """<th class="text-right">Bénéfice</th>""" else ""}
                        </tr>
                    </thead>
                    <tbody>
                        ${sales.take(15).joinToString("") { sale ->
                            """
                            <tr>
                                <td>${formatDate(sale.timestamp)}</td>
                                <td><strong>${sale.clientName}</strong></td>
                                <td>${sale.itemsSummary}</td>
                                <td>${sale.paymentMethod}</td>
                                <td class="text-right"><strong>${formatAmount(sale.totalAmount)}</strong></td>
                                ${if (isManager) """<td class="text-right" style="color:#16a34a;">+${formatAmount(sale.profitAmount)}</td>""" else ""}
                            </tr>
                            """
                        }}
                    </tbody>
                </table>

                <div class="footer">
                    Document édité électroniquement par DabaSaba POS · Système de gestion commerciale certifié · Bamako, Mali
                </div>
            </body>
            </html>
            """.trimIndent()

            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printAdapter = webView.createPrintDocumentAdapter("Bilan_Comptable_DabaSaba")
                    val attributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("id", "rpt", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                    printManager.print("Bilan Comptable DabaSaba", printAdapter, attributes)
                }
            }
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)

        } catch (e: Exception) {
            Toast.makeText(context, "Erreur impression rapport: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
