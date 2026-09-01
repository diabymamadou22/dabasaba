package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.data.local.SaleTransactionEntity
import com.example.ui.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptPrintHelper {

    private const val PREFS_NAME = "dabasaba_print_prefs"
    private const val KEY_AUTO_PRINT = "key_auto_print_after_sale"
    private const val KEY_PAPER_SIZE = "key_paper_size" // "58mm" or "80mm"

    fun isAutoPrintEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_PRINT, true) // Enabled by default for direct printing
    }

    fun setAutoPrintEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_PRINT, enabled).apply()
    }

    fun getPaperSize(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PAPER_SIZE, "58mm") ?: "58mm"
    }

    fun setPaperSize(context: Context, paperSize: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PAPER_SIZE, paperSize).apply()
    }

    /**
     * Sends the formatted receipt directly to the Android Print Manager / Spooler,
     * which handles physical printers (USB, Bluetooth, WiFi, Mopria POS printers)
     * and virtual printers (PDF).
     */
    fun printDirectReceipt(
        context: Context,
        sale: SaleTransactionEntity,
        onComplete: (() -> Unit)? = null
    ) {
        Handler(Looper.getMainLooper()).post {
            try {
                val webView = WebView(context)
                val htmlContent = generateHtmlReceipt(context, sale)

                webView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

                    override fun onPageFinished(view: WebView?, url: String?) {
                        try {
                            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                            if (printManager != null) {
                                val jobName = "Ticket_DabaSaba_${sale.id}_${System.currentTimeMillis()}"
                                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                                val printAttributes = PrintAttributes.Builder()
                                    .setMediaSize(PrintAttributes.MediaSize.ISO_A6)
                                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                                    .build()

                                printManager.print(jobName, printAdapter, printAttributes)
                                Toast.makeText(context, "🖨️ Envoi vers l'imprimante...", Toast.LENGTH_SHORT).show()
                                onComplete?.invoke()
                            } else {
                                Toast.makeText(context, "Service d'impression non disponible", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erreur d'impression : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            } catch (e: Exception) {
                Toast.makeText(context, "Impossible de préparer l'impression : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Formats thermal monospace text ticket
     */
    fun generateEscPosText(context: Context, sale: SaleTransactionEntity): String {
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date(sale.timestamp))
        val ticketNumber = "TKT-${sale.id.toString().padStart(5, '0')}"
        val storeName = StoreSettingsHelper.getStoreName(context).uppercase(Locale.ROOT)
        val storeSlogan = StoreSettingsHelper.getStoreSlogan(context)
        val storeAddress = StoreSettingsHelper.getStoreAddress(context)
        val storePhone = "${StoreSettingsHelper.getStorePhone1(context)} / ${StoreSettingsHelper.getStorePhone2(context)}"
        val nif = StoreSettingsHelper.getNifNumber(context)
        val rccm = StoreSettingsHelper.getRccmNumber(context)
        val isVat = StoreSettingsHelper.isVatEnabled(context)
        val vatRate = StoreSettingsHelper.getVatRate(context)
        val returnPolicy = StoreSettingsHelper.getReturnPolicy(context)

        val methodLabel = when (sale.paymentMethod) {
            "ORANGE_MONEY" -> "ORANGE MONEY"
            "WAVE" -> "WAVE"
            "MTN_MOMO" -> "MTN MOMO"
            "MOOV_MONEY" -> "MOOV MONEY"
            "CARTE_BANCAIRE" -> "CARTE BANCAIRE (TPE)"
            "PAIEMENT_MIXTE" -> "PAIEMENT MIXTE (SPLIT)"
            "CREDIT" -> "A CREDIT (CARNET)"
            else -> "ESPECES (CASH)"
        }

        // VAT Calculation
        val totalNet = sale.totalAmount
        val totalHt = if (isVat) totalNet / (1.0 + (vatRate / 100.0)) else totalNet
        val totalVat = totalNet - totalHt

        return buildString {
            // Optional ESC/POS Cash drawer open command: ESC p 0 25 250
            if (StoreSettingsHelper.isOpenDrawerOnSaleEnabled(context)) {
                append("\u001B\u0070\u0000\u0019\u00FA")
            }
            appendLine("================================")
            appendLine(storeName.center(32))
            appendLine(storeSlogan.center(32))
            appendLine(storeAddress.center(32))
            appendLine("Tél: $storePhone".center(32))
            if (nif.isNotBlank() || rccm.isNotBlank()) {
                appendLine("NIF: $nif | RCCM: $rccm".center(32))
            }
            appendLine("================================")
            appendLine("Ticket N° : $ticketNumber")
            appendLine("Date      : $dateStr")
            appendLine("Client    : ${sale.clientName}")
            if (sale.clientPhone.isNotBlank()) appendLine("Tél       : ${sale.clientPhone}")
            appendLine("Paiement  : $methodLabel")
            appendLine("Statut    : ${if (sale.isCredit) "CREDIT EN COURS" else "PAYE COMPTANT"}")
            appendLine("--------------------------------")
            appendLine("QTE  DESIGNATION        PRIX    ")
            appendLine("--------------------------------")
            sale.itemsSummary.split(", ").forEach { item ->
                appendLine(item)
            }
            if (sale.discountAmount > 0) {
                appendLine("--------------------------------")
                appendLine("REMISE ACCORDEE: -${ShopViewModel.formatCFA(sale.discountAmount)}")
            }
            appendLine("--------------------------------")
            if (isVat) {
                appendLine("Total Hors Taxe (HT) : ${ShopViewModel.formatCFA(totalHt)}")
                appendLine("TVA (${vatRate.toInt()}%)            : ${ShopViewModel.formatCFA(totalVat)}")
            }
            appendLine("TOTAL TTC (NET A PAYER) : ${ShopViewModel.formatCFA(totalNet)}")
            appendLine("================================")
            appendLine(returnPolicy.take(64))
            appendLine("   MERCI DE VOTRE CONFIANCE !   ")
            appendLine("    DabaSaba POS Retail Mall    ")
            appendLine("================================")
            appendLine("")
            appendLine("")
        }
    }

    private fun String.center(width: Int): String {
        if (length >= width) return this.take(width)
        val pad = (width - length) / 2
        return " ".repeat(pad) + this + " ".repeat(width - length - pad)
    }

    /**
     * Direct print via ESC/POS thermal printer service or RawBT intent
     */
    fun printViaEscPosIntent(context: Context, sale: SaleTransactionEntity) {
        val rawText = generateEscPosText(context, sale)

        // Try RawBT / ESC-POS printer direct intent
        val rawBtIntent = Intent("ru.a402d.rawbtprinter.action.PRINT_RAW").apply {
            putExtra("ru.a402d.rawbtprinter.extra.PRINT_RAW", rawText.toByteArray(Charsets.UTF_8))
        }

        try {
            context.startActivity(rawBtIntent)
            Toast.makeText(context, "Impression thermique envoyée à RawBT", Toast.LENGTH_SHORT).show()
        } catch (e: ActivityNotFoundException) {
            // Fallback to standard text share / print intent
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, rawText)
            }
            try {
                context.startActivity(Intent.createChooser(sendIntent, "Imprimer avec application thermique"))
            } catch (e2: Exception) {
                Toast.makeText(context, "Aucune application d'impression trouvée", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Generates a styled HTML ticket optimized for 58mm and 80mm POS printers
     */
    fun generateHtmlReceipt(context: Context, sale: SaleTransactionEntity): String {
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRANCE).format(Date(sale.timestamp))
        val ticketNumber = "TKT-${sale.id.toString().padStart(5, '0')}"
        val paperWidth = getPaperSize(context)
        val maxWidth = if (paperWidth == "80mm") "300px" else "220px"

        val storeName = StoreSettingsHelper.getStoreName(context)
        val storeSlogan = StoreSettingsHelper.getStoreSlogan(context)
        val storeAddress = StoreSettingsHelper.getStoreAddress(context)
        val storePhone = "${StoreSettingsHelper.getStorePhone1(context)} / ${StoreSettingsHelper.getStorePhone2(context)}"
        val nif = StoreSettingsHelper.getNifNumber(context)
        val rccm = StoreSettingsHelper.getRccmNumber(context)
        val isVat = StoreSettingsHelper.isVatEnabled(context)
        val vatRate = StoreSettingsHelper.getVatRate(context)
        val returnPolicy = StoreSettingsHelper.getReturnPolicy(context)

        val methodLabel = when (sale.paymentMethod) {
            "ORANGE_MONEY" -> "Orange Money"
            "WAVE" -> "Wave"
            "MTN_MOMO" -> "MTN MoMo"
            "MOOV_MONEY" -> "Moov Money"
            "CARTE_BANCAIRE" -> "Carte Bancaire (TPE)"
            "PAIEMENT_MIXTE" -> "Paiement Mixte (Split)"
            "CREDIT" -> "À Crédit"
            else -> "Espèces (Cash)"
        }

        val totalNet = sale.totalAmount
        val totalHt = if (isVat) totalNet / (1.0 + (vatRate / 100.0)) else totalNet
        val totalVat = totalNet - totalHt

        val itemsHtml = buildString {
            sale.itemsSummary.split(", ").forEach { item ->
                append(
                    """
                    <tr>
                        <td style="padding: 3px 0; border-bottom: 1px dotted #ccc; font-size: 11px; font-weight: 500;">
                            $item
                        </td>
                    </tr>
                    """.trimIndent()
                )
            }
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Ticket $storeName #$ticketNumber</title>
                <style>
                    @page {
                        margin: 0;
                        size: auto;
                    }
                    body {
                        font-family: 'Courier New', Courier, monospace, sans-serif;
                        color: #000;
                        background: #fff;
                        margin: 0;
                        padding: 10px;
                        width: $maxWidth;
                        margin: 0 auto;
                        font-size: 11px;
                        line-height: 1.3;
                    }
                    .center { text-align: center; }
                    .bold { font-weight: bold; }
                    .header-title { font-size: 15px; font-weight: 900; letter-spacing: 0.5px; margin: 0; }
                    .divider { border-top: 1px dashed #000; margin: 6px 0; }
                    .divider-double { border-top: 2px dashed #000; margin: 8px 0; }
                    .meta-row { display: flex; justify-content: space-between; margin-bottom: 2px; font-size: 10px; }
                    .total-box {
                        border: 1.5px solid #000;
                        padding: 6px;
                        margin: 8px 0;
                        text-align: center;
                        font-size: 14px;
                        font-weight: 900;
                        background: #f8f8f8;
                    }
                    .footer { font-size: 9px; text-align: center; color: #444; margin-top: 8px; }
                    table { width: 100%; border-collapse: collapse; }
                </style>
            </head>
            <body>
                <div class="center">
                    <h2 class="header-title">$storeName</h2>
                    <div style="font-size: 10px; font-weight: 600;">$storeSlogan</div>
                    <div style="font-size: 9px; color: #444;">$storeAddress</div>
                    <div style="font-size: 9px; color: #444;">Tél: $storePhone</div>
                    ${if (nif.isNotBlank() || rccm.isNotBlank()) "<div style='font-size: 8px; color: #666;'>NIF: $nif | RCCM: $rccm</div>" else ""}
                </div>

                <div class="divider-double"></div>

                <div class="meta-row">
                    <span class="bold">Ticket N°:</span>
                    <span>$ticketNumber</span>
                </div>
                <div class="meta-row">
                    <span class="bold">Date & Heure:</span>
                    <span>$dateStr</span>
                </div>
                <div class="meta-row">
                    <span class="bold">Client:</span>
                    <span>${sale.clientName}</span>
                </div>
                ${if (sale.clientPhone.isNotBlank()) "<div class='meta-row'><span class='bold'>Tél Client:</span><span>${sale.clientPhone}</span></div>" else ""}
                <div class="meta-row">
                    <span class="bold">Mode Paiement:</span>
                    <span>$methodLabel</span>
                </div>
                <div class="meta-row">
                    <span class="bold">Statut:</span>
                    <span class="bold">${if (sale.isCredit) "CREDIT EN COURS" else "PAYE COMPTANT"}</span>
                </div>

                <div class="divider"></div>
                <div style="font-size: 10px; font-weight: bold; margin-bottom: 4px;">ARTICLES ACHETES :</div>

                <table>
                    $itemsHtml
                </table>

                ${if (sale.discountAmount > 0) """
                <div class="meta-row" style="color: #c00; font-weight: bold; margin-top: 4px;">
                    <span>REMISE COMMERCIALE:</span>
                    <span>-${ShopViewModel.formatCFA(sale.discountAmount)}</span>
                </div>
                """ else ""}

                <div class="divider"></div>
                ${if (isVat) """
                <div class="meta-row">
                    <span>Total HT:</span>
                    <span>${ShopViewModel.formatCFA(totalHt)}</span>
                </div>
                <div class="meta-row">
                    <span>TVA (${vatRate.toInt()}%):</span>
                    <span>${ShopViewModel.formatCFA(totalVat)}</span>
                </div>
                """ else ""}

                <div class="total-box">
                    TOTAL NET : ${ShopViewModel.formatCFA(totalNet)}
                </div>

                <div class="center" style="font-size: 10px; font-weight: bold; margin-top: 4px;">
                    MERCI DE VOTRE VISITE !
                </div>
                <div class="footer">
                    $returnPolicy<br>
                    <strong>DabaSaba POS Retail & Mall Edition</strong>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
