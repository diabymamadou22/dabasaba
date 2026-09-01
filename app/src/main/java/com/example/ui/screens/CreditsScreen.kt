package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ClientCreditEntity
import com.example.data.local.CreditPaymentLogEntity
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.ShopStats
import com.example.ui.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreditsScreen(
    credits: List<ClientCreditEntity>,
    paymentLogs: List<CreditPaymentLogEntity> = emptyList(),
    stats: ShopStats,
    onSettleDebt: (ClientCreditEntity, Double, String, String) -> Unit,
    onCloseDebtCompletely: (ClientCreditEntity, String, String) -> Unit = { c, m, n -> onSettleDebt(c, c.totalDue, m, n) },
    onEditCredit: (ClientCreditEntity, String, String, Double, String) -> Unit = { _, _, _, _, _ -> },
    onDeleteCredit: (ClientCreditEntity) -> Unit = {},
    onAddNewCredit: (name: String, phone: String, amount: Double, notes: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf("EN_COURS") } // "EN_COURS", "SOLDEES", "TOUTES", "REGLEMENTS"
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state holders
    var showAddCreditDialog by remember { mutableStateOf(false) }
    var creditToSettle by remember { mutableStateOf<ClientCreditEntity?>(null) }
    var creditToEdit by remember { mutableStateOf<ClientCreditEntity?>(null) }
    var creditToDelete by remember { mutableStateOf<ClientCreditEntity?>(null) }
    var creditReceiptToShow by remember { mutableStateOf<ClientCreditEntity?>(null) }

    // Calculations
    val activeCredits = credits.filter { !it.isFullySettled && it.totalDue > 0 }
    val settledCredits = credits.filter { it.isFullySettled || it.totalDue <= 0 }
    val totalDueSum = activeCredits.sumOf { it.totalDue }
    val totalRecoveredFromLogs = paymentLogs.sumOf { it.amountPaid }

    // Filtered lists based on search
    val filteredActive = activeCredits.filter {
        it.clientName.contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery, ignoreCase = true) ||
                it.notes.contains(searchQuery, ignoreCase = true)
    }

    val filteredSettled = settledCredits.filter {
        it.clientName.contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery, ignoreCase = true) ||
                it.notes.contains(searchQuery, ignoreCase = true)
    }

    val filteredAll = credits.filter {
        it.clientName.contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery, ignoreCase = true) ||
                it.notes.contains(searchQuery, ignoreCase = true)
    }

    val filteredLogs = paymentLogs.filter {
        it.clientName.contains(searchQuery, ignoreCase = true) ||
                it.paymentMethod.contains(searchQuery, ignoreCase = true) ||
                it.note.contains(searchQuery, ignoreCase = true)
    }

    // Settlement / Clôture dialog
    if (creditToSettle != null) {
        SettleOrCloseDebtDialog(
            credit = creditToSettle!!,
            onDismiss = { creditToSettle = null },
            onSettlePartial = { amount, method, note ->
                onSettleDebt(creditToSettle!!, amount, method, note)
                creditToSettle = null
            },
            onCloseCompletely = { method, note ->
                onCloseDebtCompletely(creditToSettle!!, method, note)
                creditToSettle = null
            }
        )
    }

    // Edit Debt Dialog
    if (creditToEdit != null) {
        EditCreditDialog(
            credit = creditToEdit!!,
            onDismiss = { creditToEdit = null },
            onConfirm = { name, phone, amount, note ->
                onEditCredit(creditToEdit!!, name, phone, amount, note)
                creditToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (creditToDelete != null) {
        AlertDialog(
            onDismissRequest = { creditToDelete = null },
            containerColor = DarkSurfaceElevated,
            title = {
                Text(
                    text = "Supprimer la dette ?",
                    color = TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Voulez-vous vraiment supprimer le dossier de crédit de ${creditToDelete!!.clientName} d'un montant de ${ShopViewModel.formatCFA(creditToDelete!!.totalDue)} ? Cette action est irréversible.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCredit(creditToDelete!!)
                        creditToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Supprimer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { creditToDelete = null }) {
                    Text("Annuler", color = TextMuted)
                }
            }
        )
    }

    // Add New Credit Dialog
    if (showAddCreditDialog) {
        AddNewCreditDialog(
            onDismiss = { showAddCreditDialog = false },
            onConfirm = { name, phone, amount, note ->
                onAddNewCredit(name, phone, amount, note)
                showAddCreditDialog = false
            }
        )
    }

    // Debt Statement / Quittance Dialog
    if (creditReceiptToShow != null) {
        CreditStatementDialog(
            credit = creditReceiptToShow!!,
            onDismiss = { creditReceiptToShow = null }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("credits_screen_container")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // Obsidian Header & Financial Dashboard
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1D2236), Color(0xFF121420))
                            )
                        )
                        .border(
                            BorderStroke(1.dp, DarkSurfaceCardBorder),
                            RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Column {
                        // Title + Count Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Carnet de Crédits",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextLight
                                )
                                Text(
                                    text = "Suivi des dettes & recouvrements",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(GoldContainer)
                                    .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${activeCredits.size} en cours",
                                    color = GoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Total à Recouvrer Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TOTAL RESTANT DÛ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${settledCredits.size} soldés",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = ShopViewModel.formatNumber(if (totalDueSum > 0) totalDueSum else stats.totalCreditDue),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (totalDueSum > 0) StatusRed else StatusGreen
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "FCFA",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val totalClientsCount = credits.size.coerceAtLeast(1)
                                val settledRatio = ((settledCredits.size.toFloat() / totalClientsCount) * 100).toInt()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Clients clôturés : ${settledCredits.size} / $totalClientsCount",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "$settledRatio% réglé",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { (settledRatio / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = GoldPrimary,
                                    trackColor = DarkSurfaceCardBorder
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Rechercher un client, téléphone, note...", fontSize = 12.sp, color = TextMuted) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Effacer", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("credit_search_field"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkSurfaceCardBorder,
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight
                            )
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Category Filter Tabs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("EN_COURS", "En cours (${activeCredits.size})", Icons.Default.MoneyOff),
                        Triple("SOLDEES", "Soldées (${settledCredits.size})", Icons.Default.CheckCircle),
                        Triple("TOUTES", "Toutes (${credits.size})", Icons.Default.ReceiptLong),
                        Triple("REGLEMENTS", "Historique (${paymentLogs.size})", Icons.Default.History)
                    ).forEach { (tabKey, label, icon) ->
                        val isSelected = selectedTab == tabKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) GoldPrimary else DarkSurfaceCard)
                                .border(
                                    BorderStroke(1.dp, if (isSelected) GoldPrimary else DarkSurfaceCardBorder),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedTab = tabKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) TextDark else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TextDark else TextLight,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Tab Content
            when (selectedTab) {
                "EN_COURS" -> {
                    if (filteredActive.isEmpty()) {
                        item {
                            EmptyCreditCardState(
                                title = if (searchQuery.isNotBlank()) "Aucun client trouvé pour \"$searchQuery\"" else "Aucun crédit en cours !",
                                subtitle = "Tous vos clients sont à jour de paiement. Utilisez le bouton (+) ci-dessous pour accorder un nouveau crédit."
                            )
                        }
                    } else {
                        items(filteredActive, key = { it.id }) { credit ->
                            CreditItemCard(
                                credit = credit,
                                onSettle = { creditToSettle = credit },
                                onEdit = { creditToEdit = credit },
                                onDelete = { creditToDelete = credit },
                                onShowReceipt = { creditReceiptToShow = credit }
                            )
                        }
                    }
                }

                "SOLDEES" -> {
                    if (filteredSettled.isEmpty()) {
                        item {
                            EmptyCreditCardState(
                                title = "Aucune dette clôturée",
                                subtitle = "Les dettes entièrement réglées apparaîtront ici avec leur reçu de solde."
                            )
                        }
                    } else {
                        items(filteredSettled, key = { it.id }) { credit ->
                            CreditItemCard(
                                credit = credit,
                                onSettle = { creditToSettle = credit },
                                onEdit = { creditToEdit = credit },
                                onDelete = { creditToDelete = credit },
                                onShowReceipt = { creditReceiptToShow = credit }
                            )
                        }
                    }
                }

                "TOUTES" -> {
                    if (filteredAll.isEmpty()) {
                        item {
                            EmptyCreditCardState(
                                title = "Aucune donnée de crédit",
                                subtitle = "Cliquez sur le bouton (+) pour ajouter votre première dette client."
                            )
                        }
                    } else {
                        items(filteredAll, key = { it.id }) { credit ->
                            CreditItemCard(
                                credit = credit,
                                onSettle = { creditToSettle = credit },
                                onEdit = { creditToEdit = credit },
                                onDelete = { creditToDelete = credit },
                                onShowReceipt = { creditReceiptToShow = credit }
                            )
                        }
                    }
                }

                "REGLEMENTS" -> {
                    if (filteredLogs.isEmpty()) {
                        item {
                            EmptyCreditCardState(
                                title = "Aucun règlement enregistré",
                                subtitle = "Chaque encaissement ou versement de dette effectué sera répertorié ici."
                            )
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Total des encaissements enregistrés :",
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = ShopViewModel.formatCFA(totalRecoveredFromLogs),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusGreen
                                    )
                                }
                            }
                        }

                        items(filteredLogs, key = { it.id }) { log ->
                            PaymentLogItemCard(log = log)
                        }
                    }
                }
            }
        }

        // Floating Action Button (+) for Adding New Debt
        FloatingActionButton(
            onClick = { showAddCreditDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 90.dp)
                .testTag("add_credit_fab"),
            containerColor = GoldPrimary,
            contentColor = TextDark,
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter une dette",
                    tint = TextDark,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Nouvelle Dette",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextDark
                )
            }
        }
    }
}

@Composable
fun CreditItemCard(
    credit: ClientCreditEntity,
    onSettle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowReceipt: () -> Unit
) {
    val context = LocalContext.current
    val isSettled = credit.isFullySettled || credit.totalDue <= 0.0

    val initials = if (credit.clientName.isNotBlank()) {
        credit.clientName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
    } else "C"

    val dateFormatted = SimpleDateFormat("dd MMM yyyy", Locale.FRANCE).format(Date(credit.lastPurchaseDate))
    val accentColorParsed = try {
        Color(android.graphics.Color.parseColor(credit.accentColor))
    } catch (e: Exception) {
        GoldPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("credit_card_${credit.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(1.dp, if (isSettled) StatusGreen.copy(alpha = 0.4f) else DarkSurfaceCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Colored Top Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(if (isSettled) StatusGreen else accentColorParsed)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Top Row: Avatar + Name + Phone + Status / Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                                .border(
                                    BorderStroke(1.5.dp, if (isSettled) StatusGreen else accentColorParsed),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = if (isSettled) StatusGreen else accentColorParsed,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = credit.clientName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextLight
                                )
                                if (isSettled) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(StatusGreenBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Soldé",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusGreen
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = credit.phoneNumber.ifBlank { "Non renseigné" },
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    // Amount Due
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isSettled) "0 FCFA" else ShopViewModel.formatNumber(credit.totalDue),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSettled) StatusGreen else StatusRed
                        )
                        Text(
                            text = if (isSettled) "Dette clôturée" else "FCFA restant",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSettled) StatusGreen else StatusRed
                        )
                    }
                }

                // Notes / Articles description if present
                if (credit.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "📝 ${credit.notes}",
                            fontSize = 11.sp,
                            color = TextLight.copy(alpha = 0.85f),
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date & Purchases Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dateFormatted,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceElevated)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${credit.purchaseCount} passage(s)",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    // Action buttons row (Quittance, Modifier, Supprimer, Régler/Clôturer)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Reçu / Quittance
                        IconButton(
                            onClick = onShowReceipt,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "Quittance",
                                tint = GoldLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Edit Button
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier",
                                tint = TextLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Delete Button
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = RoseAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Settle / Close Button
                        Button(
                            onClick = onSettle,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSettled) DarkSurfaceElevated else GoldPrimary,
                                contentColor = if (isSettled) GoldLight else TextDark
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSettled) Icons.Default.Paid else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (isSettled) GoldLight else TextDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSettled) "Réajuster" else "Clôturer / Régler",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSettled) GoldLight else TextDark
                                )
                            }
                        }
                    }
                }

                // Quick Communication Strip (WhatsApp, SMS, Appel)
                if (!isSettled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceElevated)
                            .border(BorderStroke(1.dp, DarkSurfaceCardBorder), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Relances rapides :",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // WhatsApp Reminder
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusGreenBg)
                                    .border(BorderStroke(1.dp, StatusGreen.copy(alpha = 0.4f)), RoundedCornerShape(6.dp))
                                    .clickable {
                                        val cleanPhone = credit.phoneNumber.replace(" ", "").replace("+", "")
                                        val reminderMsg = "Bonjour ${credit.clientName},\nLa Boutique DabaSaba vous informe que votre solde de crédit en cours est de ${ShopViewModel.formatCFA(credit.totalDue)}.\nMerci pour votre confiance !"
                                        val waUrl = if (cleanPhone.isNotBlank()) {
                                            "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(reminderMsg)}"
                                        } else {
                                            "https://api.whatsapp.com/send?text=${Uri.encode(reminderMsg)}"
                                        }
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, reminderMsg)
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Relance Client"))
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("💬 WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
                            }

                            // SMS Reminder
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceCard)
                                    .border(BorderStroke(1.dp, DarkSurfaceCardBorder), RoundedCornerShape(6.dp))
                                    .clickable {
                                        val cleanPhone = credit.phoneNumber.replace(" ", "")
                                        val reminderMsg = "Boutique DabaSaba: Solde de crédit ${ShopViewModel.formatCFA(credit.totalDue)}. Merci !"
                                        val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("sms:${if (cleanPhone.isNotBlank()) cleanPhone else ""}")
                                            putExtra("sms_body", reminderMsg)
                                        }
                                        try {
                                            context.startActivity(smsIntent)
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("✉️ SMS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            }

                            // Call direct
                            if (credit.phoneNumber.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurfaceCard)
                                        .border(BorderStroke(1.dp, DarkSurfaceCardBorder), RoundedCornerShape(6.dp))
                                    .clickable {
                                        try {
                                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${credit.phoneNumber.replace(" ", "")}")
                                            }
                                            context.startActivity(dialIntent)
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("📞 Appel", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun PaymentLogItemCard(log: CreditPaymentLogEntity) {
    val dateFormatted = SimpleDateFormat("dd MMMM yyyy à HH:mm", Locale.FRANCE).format(Date(log.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(1.dp, DarkSurfaceCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(StatusGreenBg)
                        .border(BorderStroke(1.dp, StatusGreen.copy(alpha = 0.4f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = StatusGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = log.clientName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    Text(
                        text = dateFormatted,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    if (log.note.isNotBlank()) {
                        Text(
                            text = "Note: ${log.note}",
                            fontSize = 10.sp,
                            color = GoldLight
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+ ${ShopViewModel.formatCFA(log.amountPaid)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusGreen
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (log.paymentMethod) {
                            "ORANGE_MONEY" -> "Orange Money"
                            "WAVE" -> "Wave"
                            else -> "Espèces"
                        },
                        fontSize = 9.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCreditCardState(title: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(1.dp, DarkSurfaceCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(GoldContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GoldLight,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                color = TextLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

// --- DIALOGS: ADD, EDIT, SETTLE/CLOSE, AND STATEMENT ---

@Composable
fun AddNewCreditDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, amount: Double, note: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurfaceElevated,
            border = BorderStroke(1.dp, DarkSurfaceCardBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Nouveau Crédit Client",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                        Text(
                            text = "Enregistrer une créance",
                            fontSize = 12.sp,
                            color = GoldLight
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom complet du client", fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        focusedLabelColor = GoldLight,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Téléphone (+223)", fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        focusedLabelColor = GoldLight,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Montant de la dette (FCFA)", fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Money, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        focusedLabelColor = GoldLight,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Motif ou liste des marchandises prises", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        focusedLabelColor = GoldLight,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank() && amount > 0) {
                            onConfirm(name.trim(), phone.trim(), amount, note.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextDark
                    )
                ) {
                    Text("Enregistrer la Dette", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                }
            }
        }
    }
}

@Composable
fun EditCreditDialog(
    credit: ClientCreditEntity,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, amount: Double, note: String) -> Unit
) {
    var name by remember { mutableStateOf(credit.clientName) }
    var phone by remember { mutableStateOf(credit.phoneNumber) }
    var amountStr by remember { mutableStateOf(credit.totalDue.toInt().toString()) }
    var note by remember { mutableStateOf(credit.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurfaceElevated,
            border = BorderStroke(1.dp, DarkSurfaceCardBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Modifier la Dette",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                        Text(
                            text = "Mettre à jour les informations",
                            fontSize = 12.sp,
                            color = GoldLight
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du client", fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        focusedLabelColor = GoldLight,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Téléphone", fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        focusedLabelColor = GoldLight,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Montant restant dû (FCFA)", fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Money, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        focusedLabelColor = GoldLight,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notes / Marchandises", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        focusedLabelColor = GoldLight,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank()) {
                            onConfirm(name.trim(), phone.trim(), amount, note.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextDark
                    )
                ) {
                    Text("Sauvegarder les modifications", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                }
            }
        }
    }
}

@Composable
fun SettleOrCloseDebtDialog(
    credit: ClientCreditEntity,
    onDismiss: () -> Unit,
    onSettlePartial: (amount: Double, method: String, note: String) -> Unit,
    onCloseCompletely: (method: String, note: String) -> Unit
) {
    var amountStr by remember { mutableStateOf(credit.totalDue.toInt().toString()) }
    var selectedMethod by remember { mutableStateOf("ESPECES") } // ESPECES, ORANGE_MONEY, WAVE
    var note by remember { mutableStateOf("") }

    val amountValue = amountStr.toDoubleOrNull() ?: 0.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurfaceElevated,
            border = BorderStroke(1.dp, DarkSurfaceCardBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Régler / Clôturer la Dette",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                        Text(
                            text = credit.clientName,
                            fontSize = 14.sp,
                            color = GoldLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Debt summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Montant Dû Actuel :",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RoseAccent
                        )
                        Text(
                            text = ShopViewModel.formatCFA(credit.totalDue),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StatusRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick amount shortcuts
                Text("Montant versé", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val full = credit.totalDue
                    val half = (credit.totalDue / 2).toInt()
                    val quarter = (credit.totalDue / 4).toInt()

                    listOf(
                        "Total (${ShopViewModel.formatNumber(full)})" to full.toInt().toString(),
                        "50% (${ShopViewModel.formatNumber(half.toDouble())})" to half.toString(),
                        "25% (${ShopViewModel.formatNumber(quarter.toDouble())})" to quarter.toString()
                    ).forEach { (label, value) ->
                        val isSelected = amountStr == value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GoldPrimary else DarkSurfaceCard)
                                .border(
                                    BorderStroke(1.dp, if (isSelected) GoldPrimary else DarkSurfaceCardBorder),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { amountStr = value }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextDark else TextLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Montant reçu (FCFA)", fontSize = 12.sp, color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        focusedLabelColor = GoldLight,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Payment method selector
                Text("Moyen d'encaissement", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("ESPECES", "Espèces", Icons.Default.Money),
                        Triple("ORANGE_MONEY", "Orange Money", Icons.Default.PhoneAndroid),
                        Triple("WAVE", "Wave", Icons.Default.PhoneAndroid)
                    ).forEach { (method, label, icon) ->
                        val isSelected = selectedMethod == method
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) GoldPrimary else DarkSurfaceCard)
                                .border(
                                    BorderStroke(1.dp, if (isSelected) GoldPrimary else DarkSurfaceCardBorder),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedMethod = method }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) TextDark else TextLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextDark else TextLight
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note ou reçu (Optionnel)", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        focusedLabelColor = GoldLight,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action 1: Encaisser le montant saisi
                Button(
                    onClick = {
                        if (amountValue >= credit.totalDue) {
                            onCloseCompletely(selectedMethod, note)
                        } else if (amountValue > 0) {
                            onSettlePartial(amountValue, selectedMethod, note)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextDark
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (amountValue >= credit.totalDue) "Solder et Clôturer la Dette" else "Encaisser ${ShopViewModel.formatCFA(amountValue)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextDark
                    )
                }

                // Action 2: Bouton raccourci de clôture totale si le montant saisi était inférieur
                if (amountValue < credit.totalDue) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            onCloseCompletely(selectedMethod, if (note.isBlank()) "Clôture intégrale du solde" else note)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, StatusGreen)
                    ) {
                        Text(
                            text = "Clôturer Intégralement (${ShopViewModel.formatCFA(credit.totalDue)})",
                            color = StatusGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreditStatementDialog(
    credit: ClientCreditEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFormatted = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE).format(Date(credit.lastPurchaseDate))
    val isSettled = credit.isFullySettled || credit.totalDue <= 0

    val statementText = """
        ==============================
             BOUTIQUE DABASABA
          QUITTANCE / CARNET CRÉDIT
        ==============================
        Date: $dateFormatted
        Client: ${credit.clientName}
        Téléphone: ${credit.phoneNumber}
        Nombre d'achats: ${credit.purchaseCount}
        ------------------------------
        Marchandises / Motif:
        ${credit.notes.ifBlank { "Achats divers magasin" }}
        ------------------------------
        STATUT: ${if (isSettled) "Dette Intégralement Soldée ✓" else "En cours de remboursement"}
        RESTE DÛ: ${ShopViewModel.formatCFA(credit.totalDue)}
        ==============================
        Merci pour votre fidélité !
    """.trimIndent()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurfaceElevated,
            border = BorderStroke(1.dp, DarkSurfaceCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quittance de Crédit",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                ) {
                    Text(
                        text = statementText,
                        color = TextLight,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, statementText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Partager la quittance"))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextDark
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = TextDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Partager la Quittance", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                }
            }
        }
    }
}
