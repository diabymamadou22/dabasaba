package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.ReceiptPrintHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptPrinterDialog(
    sale: SaleTransactionEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRANCE).format(Date(sale.timestamp))
    val ticketNumber = "TKT-${sale.id.toString().padStart(5, '0')}"

    var selectedPaperSize by remember { mutableStateOf(ReceiptPrintHelper.getPaperSize(context)) }
    var autoPrintActive by remember { mutableStateOf(ReceiptPrintHelper.isAutoPrintEnabled(context)) }

    val thermalText = remember(sale, selectedPaperSize) {
        ReceiptPrintHelper.generateEscPosText(context, sale)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("thermal_receipt_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurfaceElevated,
            border = BorderStroke(1.dp, DarkSurfaceCardBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(GoldContainer)
                                .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = GoldLight, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Impression Reçu de Caisse", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            Text("Imprimante Thermique & POS", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Paper width and Print mode bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceCard)
                        .border(BorderStroke(1.dp, DarkSurfaceCardBorder), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Format Papier :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("58mm", "80mm").forEach { size ->
                            val isSelected = selectedPaperSize == size
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GoldPrimary else DarkSurfaceElevated)
                                    .border(BorderStroke(1.dp, if (isSelected) GoldPrimary else DarkSurfaceCardBorder), RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedPaperSize = size
                                        ReceiptPrintHelper.setPaperSize(context, size)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = size,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextDark else TextLight
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Realistic Paper Receipt View (Off-white textured background)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFA)),
                    border = BorderStroke(1.dp, Color(0xFFDCD6CD))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = thermalText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = Color(0xFF1E1E1E),
                            lineHeight = 14.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto-print toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceCard)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Impression directe automatique", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        Text("Lancer l'impression dès validation de vente", fontSize = 10.sp, color = TextMuted)
                    }
                    Switch(
                        checked = autoPrintActive,
                        onCheckedChange = {
                            autoPrintActive = it
                            ReceiptPrintHelper.setAutoPrintEnabled(context, it)
                            Toast.makeText(
                                context,
                                if (it) "Impression automatique activée" else "Impression automatique désactivée",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextDark,
                            checkedTrackColor = GoldPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkSurfaceElevated
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Direct Print Button (Direct System & ESC/POS Print Spooler)
                Button(
                    onClick = {
                        ReceiptPrintHelper.printDirectReceipt(context, sale)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("direct_print_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Imprimer sur Imprimante (Direct)", fontSize = 13.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Action Buttons: ESC/POS / Bluetooth Print and WhatsApp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ESC/POS Bluetooth / RawBT
                    OutlinedButton(
                        onClick = {
                            ReceiptPrintHelper.printViaEscPosIntent(context, sale)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                    ) {
                        Text("ESC/POS (RawBT)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GoldLight)
                    }

                    // Share WhatsApp Button
                    Button(
                        onClick = {
                            val cleanPhone = sale.clientPhone.replace(" ", "").replace("+", "")
                            val waText = Uri.encode("🧾 *REÇU DABASABA*\nTicket: $ticketNumber\nDate: $dateStr\nArticles: ${sale.itemsSummary}\n*Total Payé: ${ShopViewModel.formatCFA(sale.totalAmount)}*\nMerci de votre fidélité !")
                            val url = if (cleanPhone.isNotBlank()) {
                                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$waText"
                            } else {
                                "https://api.whatsapp.com/send?text=$waText"
                            }
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, thermalText)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Partager"))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = Color.White)
                    ) {
                        Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Copy Text Button
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Ticket DabaSaba", thermalText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Ticket copié dans le presse-papier !", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp), tint = TextLight)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copier le texte du ticket", fontSize = 11.sp, color = TextLight)
                }
            }
        }
    }
}
