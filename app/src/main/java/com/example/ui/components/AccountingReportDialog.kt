package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.CashRegisterSessionEntity
import com.example.data.local.ProductEntity
import com.example.data.local.SaleTransactionEntity
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.ShopStats
import com.example.util.AccountingExportHelper
import com.example.util.UserManager
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AccountingReportDialog(
    stats: ShopStats,
    sales: List<SaleTransactionEntity>,
    products: List<ProductEntity>,
    cashSessions: List<CashRegisterSessionEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isManager = UserManager.isManager(context)
    var selectedPeriod by remember { mutableStateOf("TOUT") } // "AUJOURDHUI", "SEMAINE", "TOUT"

    val numberFormat = remember {
        NumberFormat.getNumberInstance(Locale.FRENCH).apply { maximumFractionDigits = 0 }
    }

    val now = System.currentTimeMillis()
    val filteredSales = remember(sales, selectedPeriod) {
        when (selectedPeriod) {
            "AUJOURDHUI" -> {
                val startOfDay = now - (now % (24 * 3600 * 1000L))
                sales.filter { it.timestamp >= startOfDay }
            }
            "SEMAINE" -> {
                val sevenDaysAgo = now - (7 * 24 * 3600 * 1000L)
                sales.filter { it.timestamp >= sevenDaysAgo }
            }
            else -> sales
        }
    }

    val periodSalesAmount = filteredSales.sumOf { it.totalAmount }
    val periodProfitAmount = filteredSales.sumOf { it.profitAmount }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("accounting_report_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurfaceElevated,
            border = BorderStroke(1.dp, DarkSurfaceCardBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoldContainer)
                                .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = GoldLight, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Bilan Comptable & Exports", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            Text("Rapports financiers, Excel & PDF", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Period Filter Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceCard)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        "AUJOURDHUI" to "Aujourd'hui",
                        "SEMAINE" to "7 Jours",
                        "TOUT" to "Tout l'historique"
                    ).forEach { (key, label) ->
                        val isSelected = selectedPeriod == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GoldPrimary else Color.Transparent)
                                .clickable { selectedPeriod = key }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TextDark else TextLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Financial KPIs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Chiffre d'Affaires", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${numberFormat.format(periodSalesAmount)} F",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                            Text("${filteredSales.size} ventes", fontSize = 10.sp, color = TextMuted)
                        }
                    }

                    if (isManager) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = StatusGreenBg.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Bénéfice Net", fontSize = 10.sp, color = StatusGreen, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "+${numberFormat.format(periodProfitAmount)} F",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusGreen
                                )
                                val marginPercent = if (periodSalesAmount > 0) (periodProfitAmount * 100 / periodSalesAmount).toInt() else 0
                                Text("Marge : $marginPercent%", fontSize = 10.sp, color = StatusGreen.copy(alpha = 0.8f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action 1: Print / PDF Accounting Report
                Button(
                    onClick = {
                        AccountingExportHelper.printAccountingReportPdf(
                            context = context,
                            stats = stats,
                            sales = filteredSales,
                            products = products,
                            isManager = isManager
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Imprimer / Enregistrer en PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = DarkSurfaceCardBorder)

                // CSV / Excel Export Section
                Text("Exports Tableur (Excel / CSV)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                Spacer(modifier = Modifier.height(8.dp))

                // Export 1: Sales CSV
                OutlinedButton(
                    onClick = {
                        AccountingExportHelper.exportSalesToCsv(context, filteredSales, isManager)
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = GoldLight, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exporter les Ventes (${filteredSales.size}) vers Excel", fontSize = 11.sp, color = TextLight)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Export 2: Inventory CSV
                OutlinedButton(
                    onClick = {
                        AccountingExportHelper.exportInventoryToCsv(context, products, isManager)
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                ) {
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = GoldLight, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exporter l'Inventaire Stock (${products.size} articles)", fontSize = 11.sp, color = TextLight)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Export 3: Cash Sessions CSV
                OutlinedButton(
                    onClick = {
                        AccountingExportHelper.exportCashSessionsToCsv(context, cashSessions)
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null, tint = GoldLight, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exporter l'Historique de Caisse (${cashSessions.size} sessions)", fontSize = 11.sp, color = TextLight)
                }
            }
        }
    }
}
