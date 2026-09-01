package com.example.ui.components

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.CashRegisterSessionEntity
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CashRegisterDialog(
    activeSession: CashRegisterSessionEntity?,
    pastSessions: List<CashRegisterSessionEntity>,
    onOpenSession: (cashierName: String, openingCash: Double, note: String) -> Unit,
    onCloseSession: (session: CashRegisterSessionEntity, actualCash: Double, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Active Session / Clôture, 1: Historique Z
    var actualCashStr by remember { mutableStateOf("") }
    var closeNotes by remember { mutableStateOf("") }

    var showNewSessionForm by remember { mutableStateOf(activeSession == null) }
    var newCashierName by remember { mutableStateOf("Mamadou (Gérant)") }
    var newOpeningCashStr by remember { mutableStateOf("25000") }
    var newSessionNotes by remember { mutableStateOf("Fond de caisse standard") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("cash_register_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurfaceElevated,
            border = BorderStroke(1.dp, DarkSurfaceCardBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(GoldContainer)
                                .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = GoldLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Gestion de Caisse & Z", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            Text("Session & Clôture journalière", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkSurfaceCard,
                    contentColor = GoldLight,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GoldPrimary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Caisse Active",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) GoldLight else TextMuted
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Historique Z (${pastSessions.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) GoldLight else TextMuted
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    if (activeSession != null && !showNewSessionForm) {
                        // Active Session View
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                            item {
                                val openDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date(activeSession.openedAt))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                    border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(activeSession.cashierName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                                Text("Ouverte le $openDate", fontSize = 11.sp, color = TextMuted)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(StatusGreenBg)
                                                    .border(BorderStroke(1.dp, StatusGreen.copy(alpha = 0.4f)), RoundedCornerShape(20.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("EN COURS", color = StatusGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkSurfaceCardBorder)

                                        // Financial breakdown
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Fond de caisse initial :", fontSize = 12.sp, color = TextMuted)
                                            Text(ShopViewModel.formatCFA(activeSession.openingCash), fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.SemiBold)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Ventes Espèces :", fontSize = 12.sp, color = TextMuted)
                                            Text(ShopViewModel.formatCFA(activeSession.totalCashSales), fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.SemiBold)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Ventes Mobile (OM/Wave) :", fontSize = 12.sp, color = TextMuted)
                                            Text(ShopViewModel.formatCFA(activeSession.totalMobileSales), fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.SemiBold)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Crédits accordés :", fontSize = 12.sp, color = TextMuted)
                                            Text(ShopViewModel.formatCFA(activeSession.totalCreditSales), fontSize = 12.sp, color = StatusRed, fontWeight = FontWeight.SemiBold)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Recouvrements Crédit :", fontSize = 12.sp, color = TextMuted)
                                            Text(ShopViewModel.formatCFA(activeSession.totalCreditsRecovered), fontSize = 12.sp, color = StatusGreen, fontWeight = FontWeight.SemiBold)
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkSurfaceCardBorder)

                                        // Expected Cash In Drawer
                                        val expectedCashTotal = activeSession.openingCash + activeSession.totalCashSales + activeSession.totalCreditsRecovered
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("TOTAL ESPÈCES ATTENDU :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                                            Text(ShopViewModel.formatCFA(expectedCashTotal), fontSize = 15.sp, fontWeight = FontWeight.Black, color = GoldLight)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Counting & Closing Section
                                Text("Comptage physique & Clôture (Z) :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = actualCashStr,
                                    onValueChange = { actualCashStr = it },
                                    label = { Text("Montant physique compté (FCFA)", fontSize = 12.sp, color = TextMuted) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().testTag("actual_cash_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        focusedLabelColor = GoldLight,
                                        focusedTextColor = TextLight,
                                        unfocusedTextColor = TextLight
                                    )
                                )

                                val actualCashVal = actualCashStr.toDoubleOrNull()
                                if (actualCashVal != null) {
                                    val expectedCashTotal = activeSession.openingCash + activeSession.totalCashSales + activeSession.totalCreditsRecovered
                                    val diff = actualCashVal - expectedCashTotal
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (diff >= 0) StatusGreenBg else StatusRedBg)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (diff == 0.0) "Caisse parfaitement équilibrée" else if (diff > 0) "Surplus de caisse :" else "Manquant de caisse :",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (diff >= 0) StatusGreen else StatusRed
                                        )
                                        Text(
                                            text = ShopViewModel.formatCFA(diff),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (diff >= 0) StatusGreen else StatusRed
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            val actual = actualCashStr.toDoubleOrNull() ?: (activeSession.openingCash + activeSession.totalCashSales + activeSession.totalCreditsRecovered)
                                            onCloseSession(activeSession, actual, closeNotes)
                                            Toast.makeText(context, "Session clôturée avec succès ! Z généré.", Toast.LENGTH_LONG).show()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                                        modifier = Modifier.weight(1f).height(46.dp)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Clôturer la Caisse (Z)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { showNewSessionForm = true },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard, contentColor = TextLight),
                                        modifier = Modifier.height(46.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        // Open New Session Form
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Ouvrir une nouvelle session", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = newCashierName,
                                onValueChange = { newCashierName = it },
                                label = { Text("Nom du caissier", fontSize = 12.sp, color = TextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newOpeningCashStr,
                                onValueChange = { newOpeningCashStr = it },
                                label = { Text("Fond de caisse initial (FCFA)", fontSize = 12.sp, color = TextMuted) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newSessionNotes,
                                onValueChange = { newSessionNotes = it },
                                label = { Text("Note d'ouverture", fontSize = 12.sp, color = TextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val amount = newOpeningCashStr.toDoubleOrNull() ?: 25000.0
                                    onOpenSession(newCashierName.trim(), amount, newSessionNotes.trim())
                                    showNewSessionForm = false
                                    Toast.makeText(context, "Caisse ouverte avec ${ShopViewModel.formatCFA(amount)} !", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Valider l'Ouverture de Caisse", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    // History of Z Reports
                    if (pastSessions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                            Text("Aucune ancienne clôture de caisse enregistrée.", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                            items(pastSessions) { session ->
                                val dateOpened = SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE).format(Date(session.openedAt))
                                val dateClosed = if (session.closedAt != null) SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE).format(Date(session.closedAt)) else "En cours"

                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                    border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(session.cashierName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                            Text(if (session.isOpen) "EN COURS" else "CLÔTURÉ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (session.isOpen) StatusGreen else GoldLight)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("$dateOpened → $dateClosed", fontSize = 11.sp, color = TextMuted)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Ventes: ${ShopViewModel.formatCFA(session.totalCashSales + session.totalMobileSales)}", fontSize = 11.sp, color = TextLight)
                                            if (!session.isOpen) {
                                                Text(
                                                    text = "Écart: ${ShopViewModel.formatCFA(session.cashDifference)}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (session.cashDifference >= 0) StatusGreen else StatusRed
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
