package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ProductEntity
import com.example.data.local.PurchaseOrderEntity
import com.example.data.local.SupplierEntity
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
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersManagementDialog(
    suppliers: List<SupplierEntity>,
    products: List<ProductEntity>,
    purchaseOrders: List<PurchaseOrderEntity>,
    onAddSupplier: (String, String, String, String, String) -> Unit,
    onDeleteSupplier: (SupplierEntity) -> Unit,
    onSettleSupplierDebt: (SupplierEntity, Double) -> Unit,
    onCreatePurchaseOrder: (SupplierEntity, ProductEntity, Int, Double, Boolean, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("LISTE") } // "LISTE", "COMMANDE", "NOUVEAU"

    // New Supplier form state
    var newSupplierName by remember { mutableStateOf("") }
    var newSupplierPhone by remember { mutableStateOf("") }
    var newSupplierAddress by remember { mutableStateOf("") }
    var newSupplierCategory by remember { mutableStateOf("Alimentation") }
    var newSupplierNotes by remember { mutableStateOf("") }

    // New Purchase Order form state
    var selectedSupplierForOrder by remember { mutableStateOf(suppliers.firstOrNull()) }
    var selectedProductForOrder by remember { mutableStateOf(products.firstOrNull()) }
    var orderQuantityText by remember { mutableStateOf("10") }
    var orderUnitCostText by remember { mutableStateOf(selectedProductForOrder?.buyPrice?.toInt()?.toString() ?: "0") }
    var isOrderPaid by remember { mutableStateOf(true) }
    var orderPaymentMethod by remember { mutableStateOf("ESPECES") }
    var supplierDropdownExpanded by remember { mutableStateOf(false) }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    // Settle debt dialog state
    var debtSupplierToSettle by remember { mutableStateOf<SupplierEntity?>(null) }
    var settleAmountText by remember { mutableStateOf("") }

    val numberFormat = remember {
        NumberFormat.getNumberInstance(Locale.FRENCH).apply { maximumFractionDigits = 0 }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("suppliers_management_dialog"),
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoldContainer)
                                .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = GoldLight, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Fournisseurs & Achats", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            Text("${suppliers.size} grossistes partenaires", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceCard)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        "LISTE" to "Fournisseurs",
                        "COMMANDE" to "+ Approvisionner",
                        "NOUVEAU" to "+ Nouveau"
                    ).forEach { (key, label) ->
                        val isSelected = selectedTab == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GoldPrimary else Color.Transparent)
                                .clickable { selectedTab = key }
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

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    "LISTE" -> {
                        if (suppliers.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Aucun fournisseur enregistré.", color = TextMuted, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.height(360.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(suppliers) { supplier ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                        border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(supplier.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                                    Text("Spécialité : ${supplier.category}", fontSize = 11.sp, color = GoldLight)
                                                }
                                                if (supplier.phoneNumber.isNotBlank()) {
                                                    IconButton(
                                                        onClick = {
                                                            try {
                                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${supplier.phoneNumber}"))
                                                                context.startActivity(intent)
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Impossible d'ouvrir le composeur", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.Phone, contentDescription = "Appeler", tint = StatusGreen, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }

                                            if (supplier.address.isNotBlank()) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Place, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(supplier.address, fontSize = 11.sp, color = TextMuted)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = DarkSurfaceCardBorder)
                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("Dette Fournisseur :", fontSize = 10.sp, color = TextMuted)
                                                    Text(
                                                        text = "${numberFormat.format(supplier.totalDebtToSupplier)} F CFA",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (supplier.totalDebtToSupplier > 0) Color(0xFFEF4444) else StatusGreen
                                                    )
                                                }

                                                if (supplier.totalDebtToSupplier > 0) {
                                                    Button(
                                                        onClick = {
                                                            debtSupplierToSettle = supplier
                                                            settleAmountText = supplier.totalDebtToSupplier.toInt().toString()
                                                        },
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                                                        modifier = Modifier.height(32.dp)
                                                    ) {
                                                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Régler", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "COMMANDE" -> {
                        // Create purchase order / stock reception
                        Column(modifier = Modifier.height(360.dp)) {
                            Text("Sélectionnez le Fournisseur :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
                            Spacer(modifier = Modifier.height(4.dp))

                            ExposedDropdownMenuBox(
                                expanded = supplierDropdownExpanded,
                                onExpandedChange = { supplierDropdownExpanded = !supplierDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedSupplierForOrder?.name ?: "Choisir un fournisseur",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierDropdownExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        focusedTextColor = TextLight,
                                        unfocusedTextColor = TextLight
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = supplierDropdownExpanded,
                                    onDismissRequest = { supplierDropdownExpanded = false }
                                ) {
                                    suppliers.forEach { sup ->
                                        DropdownMenuItem(
                                            text = { Text(sup.name, color = TextLight) },
                                            onClick = {
                                                selectedSupplierForOrder = sup
                                                supplierDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Produit à approvisionner :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
                            Spacer(modifier = Modifier.height(4.dp))

                            ExposedDropdownMenuBox(
                                expanded = productDropdownExpanded,
                                onExpandedChange = { productDropdownExpanded = !productDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedProductForOrder?.name ?: "Choisir un article",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        focusedTextColor = TextLight,
                                        unfocusedTextColor = TextLight
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = productDropdownExpanded,
                                    onDismissRequest = { productDropdownExpanded = false }
                                ) {
                                    products.forEach { prod ->
                                        DropdownMenuItem(
                                            text = { Text("${prod.name} (Stock actuel: ${prod.stockQuantity})", color = TextLight) },
                                            onClick = {
                                                selectedProductForOrder = prod
                                                orderUnitCostText = prod.buyPrice.toInt().toString()
                                                productDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = orderQuantityText,
                                    onValueChange = { orderQuantityText = it.filter { c -> c.isDigit() } },
                                    label = { Text("Quantité", fontSize = 10.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = orderUnitCostText,
                                    onValueChange = { orderUnitCostText = it.filter { c -> c.isDigit() } },
                                    label = { Text("Prix Achat Unitaire (F)", fontSize = 10.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            val qty = orderQuantityText.toIntOrNull() ?: 0
                            val unitCost = orderUnitCostText.toDoubleOrNull() ?: 0.0
                            val totalCost = qty * unitCost

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isOrderPaid = !isOrderPaid }
                            ) {
                                Checkbox(
                                    checked = isOrderPaid,
                                    onCheckedChange = { isOrderPaid = it },
                                    colors = CheckboxDefaults.colors(checkedColor = GoldPrimary)
                                )
                                Text(
                                    text = if (isOrderPaid) "Payé comptant (Caisse)" else "Achat à Crédit (Dette Fournisseur)",
                                    fontSize = 11.sp,
                                    color = if (isOrderPaid) StatusGreen else Color(0xFFEF4444),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    val sup = selectedSupplierForOrder
                                    val prod = selectedProductForOrder
                                    if (sup != null && prod != null && qty > 0) {
                                        onCreatePurchaseOrder(sup, prod, qty, unitCost, isOrderPaid, orderPaymentMethod)
                                        Toast.makeText(context, "Stock approvisionné (+ $qty ${prod.name}) !", Toast.LENGTH_LONG).show()
                                        selectedTab = "LISTE"
                                    } else {
                                        Toast.makeText(context, "Veuillez vérifier les champs", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
                            ) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Valider l'Approvisionnement (${numberFormat.format(totalCost)} F)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    "NOUVEAU" -> {
                        Column(modifier = Modifier.height(360.dp)) {
                            OutlinedTextField(
                                value = newSupplierName,
                                onValueChange = { newSupplierName = it },
                                label = { Text("Nom du Fournisseur / Société *") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newSupplierPhone,
                                onValueChange = { newSupplierPhone = it },
                                label = { Text("Numéro Téléphone") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newSupplierAddress,
                                onValueChange = { newSupplierAddress = it },
                                label = { Text("Adresse / Emplacement (ex: Grand Marché)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newSupplierCategory,
                                onValueChange = { newSupplierCategory = it },
                                label = { Text("Catégorie Produits Fournis") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    if (newSupplierName.isNotBlank()) {
                                        onAddSupplier(newSupplierName.trim(), newSupplierPhone.trim(), newSupplierAddress.trim(), newSupplierCategory.trim(), newSupplierNotes.trim())
                                        Toast.makeText(context, "Fournisseur ajouté avec succès !", Toast.LENGTH_SHORT).show()
                                        newSupplierName = ""
                                        newSupplierPhone = ""
                                        newSupplierAddress = ""
                                        selectedTab = "LISTE"
                                    } else {
                                        Toast.makeText(context, "Le nom du fournisseur est obligatoire", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enregistrer le Fournisseur", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to settle debt
    if (debtSupplierToSettle != null) {
        val s = debtSupplierToSettle!!
        Dialog(onDismissRequest = { debtSupplierToSettle = null }) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                shape = RoundedCornerShape(20.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, DarkSurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Règlement Dette Fournisseur", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Fournisseur : ${s.name}", fontSize = 12.sp, color = GoldLight)
                    Text("Dette actuelle : ${numberFormat.format(s.totalDebtToSupplier)} F CFA", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = settleAmountText,
                        onValueChange = { settleAmountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Montant payé (F CFA)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { debtSupplierToSettle = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Annuler", fontSize = 11.sp, color = TextMuted)
                        }

                        Button(
                            onClick = {
                                val amt = settleAmountText.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    onSettleSupplierDebt(s, amt)
                                    Toast.makeText(context, "Règlement de ${numberFormat.format(amt)} F enregistré !", Toast.LENGTH_SHORT).show()
                                    debtSupplierToSettle = null
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = Color.White)
                        ) {
                            Text("Confirmer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
