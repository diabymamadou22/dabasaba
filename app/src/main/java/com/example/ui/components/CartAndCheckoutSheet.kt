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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.CartItem
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.ReceiptPrintHelper

import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.SwapHoriz
import com.example.util.StoreSettingsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartAndCheckoutSheet(
    cartItems: Map<Int, CartItem>,
    sheetState: SheetState,
    onAddToCart: (com.example.data.local.ProductEntity) -> Unit,
    onRemoveFromCart: (com.example.data.local.ProductEntity) -> Unit,
    onClearCart: () -> Unit,
    onCompleteSale: (String, String, String, Boolean, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("ESPECES") } // ESPECES, WAVE, ORANGE_MONEY, MTN_MOMO, MOOV_MONEY, CARTE_BANCAIRE, PAIEMENT_MIXTE, CREDIT
    var autoPrintDirect by remember { mutableStateOf(ReceiptPrintHelper.isAutoPrintEnabled(context)) }

    // Wholesale mode toggle
    var isWholesaleMode by remember { mutableStateOf(false) }

    // Cash received & change calculation
    var cashReceivedText by remember { mutableStateOf("") }
    var splitCashText by remember { mutableStateOf("") }
    var splitMobileText by remember { mutableStateOf("") }

    // Discount state
    var showDiscountSection by remember { mutableStateOf(false) }
    var discountText by remember { mutableStateOf("") }
    var discountNote by remember { mutableStateOf("") }

    val itemsList = cartItems.values.toList()
    val baseGrossTotal = itemsList.sumOf { it.product.sellPrice * it.quantity }
    // Wholesale discount applies 10% reduction on base price if enabled
    val wholesaleMultiplier = if (isWholesaleMode) 0.90 else 1.0
    val grossTotalAmount = baseGrossTotal * wholesaleMultiplier

    val customDiscountAmount = (discountText.toDoubleOrNull() ?: 0.0).coerceIn(0.0, grossTotalAmount)
    val totalDiscount = if (isWholesaleMode) (baseGrossTotal - grossTotalAmount) + customDiscountAmount else customDiscountAmount
    val netTotalAmount = (grossTotalAmount - customDiscountAmount).coerceAtLeast(0.0)

    val isVat = StoreSettingsHelper.isVatEnabled(context)
    val vatRate = StoreSettingsHelper.getVatRate(context)
    val totalHt = if (isVat) netTotalAmount / (1.0 + (vatRate / 100.0)) else netTotalAmount
    val totalVat = netTotalAmount - totalHt

    val cashReceived = cashReceivedText.toDoubleOrNull() ?: 0.0
    val changeToReturn = (cashReceived - netTotalAmount).coerceAtLeast(0.0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Panier (${itemsList.sumOf { it.quantity }} articles)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                }

                if (itemsList.isNotEmpty()) {
                    Text(
                        text = "Vider",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusRed,
                        modifier = Modifier
                            .clickable { onClearCart() }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (itemsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Le panier est vide.\nSélectionnez des produits pour vendre.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                // Cart Items List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    items(itemsList, key = { it.product.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ProductVisualIcon(
                                        iconKey = item.product.iconKey,
                                        size = 40.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = item.product.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextLight
                                        )
                                        Text(
                                            text = "${ShopViewModel.formatNumber(item.product.sellPrice)} FCFA / u",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                // Stepper Controls
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onRemoveFromCart(item.product) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(DarkSurfaceElevated)
                                            .border(BorderStroke(1.dp, DarkSurfaceCardBorder), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                            contentDescription = "Moins",
                                            modifier = Modifier.size(16.dp),
                                            tint = if (item.quantity == 1) StatusRed else TextLight
                                        )
                                    }

                                    Text(
                                        text = "${item.quantity}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextLight,
                                        modifier = Modifier.padding(horizontal = 10.dp)
                                    )

                                    IconButton(
                                        onClick = { onAddToCart(item.product) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(GoldPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Plus",
                                            modifier = Modifier.size(16.dp),
                                            tint = TextDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DarkSurfaceCardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Client Inputs
                Text(
                    text = "Informations Client (Optionnel)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Nom du client", fontSize = 12.sp, color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldLight,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Numéro Tel", fontSize = 12.sp, color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            focusedLabelColor = GoldLight,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Wholesale mode toggle (Tarif Gros vs Détail)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isWholesaleMode) GoldContainer.copy(alpha = 0.6f) else DarkSurfaceCard)
                        .border(
                            BorderStroke(1.dp, if (isWholesaleMode) GoldPrimary else DarkSurfaceCardBorder),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { isWholesaleMode = !isWholesaleMode }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = if (isWholesaleMode) GoldPrimary else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isWholesaleMode) "Tarif Gros Actif (-10% Grossiste)" else "Tarif Détail Standard",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isWholesaleMode) GoldLight else TextLight
                            )
                            Text(
                                text = if (isWholesaleMode) "Prix de gros appliqué à tout le panier" else "Appuyez pour basculer en prix de gros",
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Switch(
                        checked = isWholesaleMode,
                        onCheckedChange = { isWholesaleMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextDark,
                            checkedTrackColor = GoldPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkSurfaceElevated
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Payment Mode Selector (Grid of 8 commercial methods)
                Text(
                    text = "Mode de Paiement & Règlement",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                val methods = listOf(
                    Triple("ESPECES", "💵 Espèces", Icons.Default.Money),
                    Triple("WAVE", "🌊 Wave", Icons.Default.PhoneAndroid),
                    Triple("ORANGE_MONEY", "📱 Orange M.", Icons.Default.PhoneAndroid),
                    Triple("MTN_MOMO", "🟡 MTN MoMo", Icons.Default.PhoneAndroid),
                    Triple("MOOV_MONEY", "🟣 Moov M.", Icons.Default.PhoneAndroid),
                    Triple("CARTE_BANCAIRE", "💳 Carte / TPE", Icons.Default.CreditCard),
                    Triple("PAIEMENT_MIXTE", "🔀 Mixte (Split)", Icons.Default.Calculate),
                    Triple("CREDIT", "📒 Carnet Crédit", Icons.Default.AccountBalance)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        methods.take(4).forEach { (key, label, icon) ->
                            val isSelected = selectedPaymentMethod == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) GoldPrimary else DarkSurfaceCard)
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isSelected) GoldPrimary else DarkSurfaceCardBorder
                                        ),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedPaymentMethod = key }
                                    .padding(vertical = 8.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) TextDark else TextLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) TextDark else TextLight,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        methods.drop(4).forEach { (key, label, icon) ->
                            val isSelected = selectedPaymentMethod == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) GoldPrimary else DarkSurfaceCard)
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isSelected) GoldPrimary else DarkSurfaceCardBorder
                                        ),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedPaymentMethod = key }
                                    .padding(vertical = 8.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) TextDark else TextLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) TextDark else TextLight,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Cash Monnaie Rendu Calculator when paying with Cash
                if (selectedPaymentMethod == "ESPECES") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Calculateur Monnaie Reçue",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight
                                )
                                if (cashReceived > 0) {
                                    Text(
                                        text = if (changeToReturn > 0) "Monnaie à rendre : ${ShopViewModel.formatCFA(changeToReturn)}" else "Montant exact",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (changeToReturn > 0) StatusGreen else GoldLight
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    "Exact" to netTotalAmount,
                                    "2 000 F" to 2000.0,
                                    "5 000 F" to 5000.0,
                                    "10 000 F" to 10000.0
                                ).forEach { (lbl, amt) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(GoldContainer)
                                            .clickable { cashReceivedText = amt.toInt().toString() }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(lbl, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = cashReceivedText,
                                onValueChange = { cashReceivedText = it.filter { c -> c.isDigit() } },
                                placeholder = { Text("Montant donné par le client (ex: 10000)", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                )
                            )
                        }
                    }
                }

                // Split Payment details (Mixte)
                if (selectedPaymentMethod == "PAIEMENT_MIXTE") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Répartition Paiement Mixte",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = splitCashText,
                                    onValueChange = { splitCashText = it.filter { c -> c.isDigit() } },
                                    label = { Text("Part Espèces (F)", fontSize = 9.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        focusedTextColor = TextLight,
                                        unfocusedTextColor = TextLight
                                    )
                                )
                                OutlinedTextField(
                                    value = splitMobileText,
                                    onValueChange = { splitMobileText = it.filter { c -> c.isDigit() } },
                                    label = { Text("Part Mobile / TPE (F)", fontSize = 9.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        focusedTextColor = TextLight,
                                        unfocusedTextColor = TextLight
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Direct print toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceCard)
                        .clickable {
                            autoPrintDirect = !autoPrintDirect
                            ReceiptPrintHelper.setAutoPrintEnabled(context, autoPrintDirect)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = if (autoPrintDirect) GoldPrimary else TextMuted,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Impression directe après vente",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (autoPrintDirect) TextLight else TextMuted
                        )
                    }
                    Switch(
                        checked = autoPrintDirect,
                        onCheckedChange = {
                            autoPrintDirect = it
                            ReceiptPrintHelper.setAutoPrintEnabled(context, it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextDark,
                            checkedTrackColor = GoldPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkSurfaceElevated
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Discount / Remise Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (totalDiscount > 0) GoldContainer.copy(alpha = 0.5f) else DarkSurfaceCard)
                        .clickable { showDiscountSection = !showDiscountSection }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏷️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (totalDiscount > 0) "Remise appliquée : -${ShopViewModel.formatCFA(totalDiscount)}" else "+ Accorder une remise client",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (totalDiscount > 0) GoldLight else TextLight
                        )
                    }
                    Text(
                        text = if (showDiscountSection) "Masquer" else "Modifier",
                        fontSize = 10.sp,
                        color = GoldLight,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showDiscountSection) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceCard)
                            .padding(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "500 F" to 500.0,
                                "1 000 F" to 1000.0,
                                "5%" to (grossTotalAmount * 0.05),
                                "10%" to (grossTotalAmount * 0.10)
                            ).forEach { (label, amt) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GoldContainer)
                                        .clickable { discountText = amt.toInt().toString() }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                                }
                            }
                            if (customDiscountAmount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurfaceElevated)
                                        .clickable { discountText = "" }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Annuler", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = discountText,
                            onValueChange = { discountText = it.filter { c -> c.isDigit() } },
                            placeholder = { Text("Montant remise personnalisé (F CFA)", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Total and Submit Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (totalDiscount > 0) {
                            Text(
                                text = "Brut: ${ShopViewModel.formatCFA(baseGrossTotal)}",
                                fontSize = 10.sp,
                                color = TextMuted,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }
                        if (isVat) {
                            Text(
                                text = "HT: ${ShopViewModel.formatCFA(totalHt)} | TVA (${vatRate.toInt()}%): ${ShopViewModel.formatCFA(totalVat)}",
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                        Text(text = "Total net à payer", fontSize = 12.sp, color = TextMuted)
                        Text(
                            text = ShopViewModel.formatCFA(netTotalAmount),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldLight
                        )
                    }

                    Button(
                        onClick = {
                            val isCredit = selectedPaymentMethod == "CREDIT"
                            val finalClientName = if (isCredit && clientName.isBlank()) "Client Crédit" else clientName
                            val noteBuilder = buildString {
                                if (isWholesaleMode) append("[VENTE EN GROS] ")
                                if (selectedPaymentMethod == "PAIEMENT_MIXTE" && splitCashText.isNotBlank()) {
                                    append("Mixte: Cash $splitCashText F + Mobile $splitMobileText F ")
                                }
                                if (discountNote.isNotBlank()) append(discountNote)
                            }
                            onCompleteSale(finalClientName, clientPhone, selectedPaymentMethod, isCredit, totalDiscount, noteBuilder)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = TextDark
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text = "Valider la Vente",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                }
            }
        }
    }
}

