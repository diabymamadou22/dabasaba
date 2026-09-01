package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.util.LicenseManager
import com.example.util.ReceiptPrintHelper
import com.example.util.StoreSettingsHelper

@Composable
fun SettingsAndBackupDialog(
    onExportBackup: ((String) -> Unit) -> Unit,
    onImportBackup: (String, (Boolean, String) -> Unit) -> Unit,
    onFactoryReset: ((() -> Unit) -> Unit) = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf("STORE") } // STORE, HARDWARE, LICENSE, BACKUP, DANGER

    // Store settings states
    var storeName by remember { mutableStateOf(StoreSettingsHelper.getStoreName(context)) }
    var storeSlogan by remember { mutableStateOf(StoreSettingsHelper.getStoreSlogan(context)) }
    var storeAddress by remember { mutableStateOf(StoreSettingsHelper.getStoreAddress(context)) }
    var storePhone1 by remember { mutableStateOf(StoreSettingsHelper.getStorePhone1(context)) }
    var storePhone2 by remember { mutableStateOf(StoreSettingsHelper.getStorePhone2(context)) }
    var nifNumber by remember { mutableStateOf(StoreSettingsHelper.getNifNumber(context)) }
    var rccmNumber by remember { mutableStateOf(StoreSettingsHelper.getRccmNumber(context)) }
    var isVatEnabled by remember { mutableStateOf(StoreSettingsHelper.isVatEnabled(context)) }
    var vatRateText by remember { mutableStateOf(StoreSettingsHelper.getVatRate(context).toInt().toString()) }
    var returnPolicy by remember { mutableStateOf(StoreSettingsHelper.getReturnPolicy(context)) }

    // Hardware states
    var autoPrintActive by remember { mutableStateOf(ReceiptPrintHelper.isAutoPrintEnabled(context)) }
    var paperSize by remember { mutableStateOf(ReceiptPrintHelper.getPaperSize(context)) }
    var openDrawerOnSale by remember { mutableStateOf(StoreSettingsHelper.isOpenDrawerOnSaleEnabled(context)) }
    var scannerBeep by remember { mutableStateOf(StoreSettingsHelper.isScannerBeepEnabled(context)) }

    // License states
    var licenseKeyInput by remember { mutableStateOf("") }
    var isLicenseActive by remember { mutableStateOf(LicenseManager.isActivated(context)) }
    var licensePlan by remember { mutableStateOf(LicenseManager.getLicensePlan(context)) }
    var licenseMessage by remember { mutableStateOf<String?>(null) }

    // Backup states
    var exportedJson by remember { mutableStateOf("") }
    var importJsonText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(true) }

    // Factory reset states
    var showFactoryResetDialog by remember { mutableStateOf(false) }
    var resetPinInput by remember { mutableStateOf("") }
    var resetPinError by remember { mutableStateOf<String?>(null) }

    if (showFactoryResetDialog) {
        Dialog(onDismissRequest = { showFactoryResetDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("factory_reset_confirmation_dialog"),
                shape = RoundedCornerShape(22.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.5.dp, StatusRed.copy(alpha = 0.6f)),
                tonalElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(StatusRedBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = StatusRed,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Réinitialisation Totale",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Cette action va SUPPRIMER DÉFINITIVEMENT toutes les données de l'application sans laisser aucune trace dans la base de données (produits, ventes, crédits, fournisseurs, caisse, mouvements).",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(StatusRedBg.copy(alpha = 0.5f))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pour confirmer, veuillez saisir le code PIN : 0000",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusRed
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = resetPinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                resetPinInput = it
                                resetPinError = null
                            }
                        },
                        placeholder = { Text("Code PIN (0000)", fontSize = 13.sp, color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = if (resetPinError != null) StatusRed else GoldLight)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = resetPinError != null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("factory_reset_pin_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StatusRed,
                            unfocusedBorderColor = DarkSurfaceCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    if (resetPinError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = resetPinError!!,
                            color = StatusRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showFactoryResetDialog = false
                                resetPinInput = ""
                                resetPinError = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                        ) {
                            Text("Annuler", color = TextLight, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (resetPinInput == "0000") {
                                    onFactoryReset {
                                        Toast.makeText(context, "Application réinitialisée à zéro avec succès !", Toast.LENGTH_LONG).show()
                                        showFactoryResetDialog = false
                                        resetPinInput = ""
                                        onDismiss()
                                    }
                                } else {
                                    resetPinError = "Code PIN incorrect (Code requis: 0000)"
                                }
                            },
                            enabled = resetPinInput.length == 4,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("confirm_factory_reset_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusRed,
                                contentColor = Color.White,
                                disabledContainerColor = StatusRed.copy(alpha = 0.3f),
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            )
                        ) {
                            Text("TOUT EFFACER", fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("settings_and_backup_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurfaceElevated,
            border = BorderStroke(1.dp, DarkSurfaceCardBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Top Header
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
                                .background(GoldContainer)
                                .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = GoldLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Administration & POS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            Text("Boutiques · Malls · Fiscalité · Caisse", fontSize = 10.sp, color = TextMuted)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs
                val sections = listOf(
                    "STORE" to "🏪 Magasin",
                    "HARDWARE" to "🖨️ Matériel",
                    "LICENSE" to "🔑 Licence",
                    "BACKUP" to "💾 Données",
                    "DANGER" to "⚠️ Reset"
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(sections) { (key, label) ->
                        val isSelected = selectedSection == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) GoldPrimary else DarkSurfaceCard)
                                .border(BorderStroke(1.dp, if (isSelected) GoldPrimary else DarkSurfaceCardBorder), RoundedCornerShape(10.dp))
                                .clickable { selectedSection = key }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextDark else TextLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 1: STORE & FISCAL
                if (selectedSection == "STORE") {
                    Text("Informations Établissement & Fiscalité", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = storeName,
                                onValueChange = { storeName = it },
                                label = { Text("Nom de l'enseigne / Boutique", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = storeSlogan,
                                onValueChange = { storeSlogan = it },
                                label = { Text("Activité / Slogan", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = storeAddress,
                                onValueChange = { storeAddress = it },
                                label = { Text("Adresse physique", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = storePhone1,
                                    onValueChange = { storePhone1 = it },
                                    label = { Text("Téléphone 1", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                                )
                                OutlinedTextField(
                                    value = storePhone2,
                                    onValueChange = { storePhone2 = it },
                                    label = { Text("Téléphone 2", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkSurfaceCardBorder)

                            Text("Fiscalité & Mentions Légales", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = nifNumber,
                                    onValueChange = { nifNumber = it },
                                    label = { Text("NIF (Identifiant Fiscal)", fontSize = 9.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                                )
                                OutlinedTextField(
                                    value = rccmNumber,
                                    onValueChange = { rccmNumber = it },
                                    label = { Text("RCCM (Registre Comm.)", fontSize = 9.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Application de la TVA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                    Text("Calcul automatique HT / TVA / TTC sur le ticket", fontSize = 9.sp, color = TextMuted)
                                }
                                Switch(
                                    checked = isVatEnabled,
                                    onCheckedChange = { isVatEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = TextDark, checkedTrackColor = GoldPrimary)
                                )
                            }

                            if (isVatEnabled) {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = vatRateText,
                                    onValueChange = { vatRateText = it.filter { c -> c.isDigit() } },
                                    label = { Text("Taux de TVA (%)", fontSize = 9.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = returnPolicy,
                                onValueChange = { returnPolicy = it },
                                label = { Text("Politique de retour (bas du ticket)", fontSize = 9.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    StoreSettingsHelper.setStoreName(context, storeName)
                                    StoreSettingsHelper.setStoreSlogan(context, storeSlogan)
                                    StoreSettingsHelper.setStoreAddress(context, storeAddress)
                                    StoreSettingsHelper.setStorePhone1(context, storePhone1)
                                    StoreSettingsHelper.setStorePhone2(context, storePhone2)
                                    StoreSettingsHelper.setNifNumber(context, nifNumber)
                                    StoreSettingsHelper.setRccmNumber(context, rccmNumber)
                                    StoreSettingsHelper.setVatEnabled(context, isVatEnabled)
                                    val rate = vatRateText.toFloatOrNull() ?: 18.0f
                                    StoreSettingsHelper.setVatRate(context, rate)
                                    StoreSettingsHelper.setReturnPolicy(context, returnPolicy)
                                    Toast.makeText(context, "Paramètres d'établissement enregistrés !", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enregistrer les Coordonnées", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // SECTION 2: HARDWARE & PERIPHERALS
                if (selectedSection == "HARDWARE") {
                    Text("Configuration Matériel & Périphériques POS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Paper format
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Format du Rouleau Papier", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                    Text("58mm standard / 80mm large", fontSize = 9.sp, color = TextMuted)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("58mm", "80mm").forEach { size ->
                                        val isSelected = paperSize == size
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) GoldPrimary else DarkSurfaceElevated)
                                                .border(BorderStroke(1.dp, if (isSelected) GoldPrimary else DarkSurfaceCardBorder), RoundedCornerShape(8.dp))
                                                .clickable {
                                                    paperSize = size
                                                    ReceiptPrintHelper.setPaperSize(context, size)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(size, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) TextDark else TextLight)
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkSurfaceCardBorder)

                            // Auto print switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Impression Automatique", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                    Text("Déclencher l'impression immédiatement après encaissement", fontSize = 9.sp, color = TextMuted)
                                }
                                Switch(
                                    checked = autoPrintActive,
                                    onCheckedChange = {
                                        autoPrintActive = it
                                        ReceiptPrintHelper.setAutoPrintEnabled(context, it)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = TextDark, checkedTrackColor = GoldPrimary)
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkSurfaceCardBorder)

                            // Drawer kick
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ouverture Tiroir-Caisse RJ11", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                    Text("Signal d'éjection automatique ESC/POS à la validation de la vente", fontSize = 9.sp, color = TextMuted)
                                }
                                Switch(
                                    checked = openDrawerOnSale,
                                    onCheckedChange = {
                                        openDrawerOnSale = it
                                        StoreSettingsHelper.setOpenDrawerOnSaleEnabled(context, it)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = TextDark, checkedTrackColor = GoldPrimary)
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkSurfaceCardBorder)

                            // Scanner HID
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Lecteur Code-Barres (Douchette)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                    Text("Mode USB/Bluetooth HID avec retour sonore", fontSize = 9.sp, color = TextMuted)
                                }
                                Switch(
                                    checked = scannerBeep,
                                    onCheckedChange = {
                                        scannerBeep = it
                                        StoreSettingsHelper.setScannerBeepEnabled(context, it)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = TextDark, checkedTrackColor = GoldPrimary)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Test print button
                            OutlinedButton(
                                onClick = {
                                    val dummySale = SaleTransactionEntity(
                                        id = 9999,
                                        clientName = "Client Test",
                                        clientPhone = "+223 76 00 00 00",
                                        paymentMethod = "ESPECES",
                                        totalAmount = 5000.0,
                                        profitAmount = 1000.0,
                                        isCredit = false,
                                        timestamp = System.currentTimeMillis(),
                                        itemsSummary = "2x Riz Parfumé 25kg (5.000 F)"
                                    )
                                    ReceiptPrintHelper.printDirectReceipt(context, dummySale)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldLight)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Imprimer Reçu de Test", fontSize = 11.sp, color = GoldLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // SECTION 3: COMMERCIAL LICENSING & ACTIVATION
                if (selectedSection == "LICENSE") {
                    Text("Licence & Commercialisation du Système", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        border = BorderStroke(1.dp, if (isLicenseActive) StatusGreen.copy(alpha = 0.5f) else DarkSurfaceCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Statut du Terminal : ACTIVÉ", fontSize = 12.sp, fontWeight = FontWeight.Black, color = StatusGreen)
                                    Text(licensePlan, fontSize = 10.sp, color = TextMuted)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("ID Matériel de ce Terminal :", fontSize = 9.sp, color = TextMuted)
                                    Text(LicenseManager.getDeviceId(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = licenseKeyInput,
                                onValueChange = { licenseKeyInput = it },
                                placeholder = { Text("Entrer une clé de licence (ex: DABA-PRO-...)", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                            )

                            if (licenseMessage != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(licenseMessage!!, fontSize = 10.sp, color = GoldLight)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    val (success, msg) = LicenseManager.activateLicense(context, licenseKeyInput)
                                    licenseMessage = msg
                                    if (success) {
                                        isLicenseActive = true
                                        licensePlan = LicenseManager.getLicensePlan(context)
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Activer / Mettre à Jour la Licence", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkSurfaceCardBorder)

                            Text(
                                text = "💼 Déploiement Centres Commerciaux & Boutiques : Système 100% prêt à la vente commerciale avec gestion des caissiers, imprimantes thermiques, tiroirs-caisses et sécurité PIN.",
                                fontSize = 10.sp,
                                color = TextMuted,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // SECTION 4: BACKUP & RESTORE
                if (selectedSection == "BACKUP") {
                    Text("Sauvegarde & Restauration des Données", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            onExportBackup { json ->
                                exportedJson = json
                                if (json.isNotBlank()) {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_TEXT, json)
                                        putExtra(Intent.EXTRA_SUBJECT, "Sauvegarde DabaSaba POS - ${System.currentTimeMillis()}.json")
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Exporter la sauvegarde JSON"))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Générer Sauvegarde Complète (JSON)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (exportedJson.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Sauvegarde DabaSaba", exportedJson)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Sauvegarde copiée dans le presse-papiers !", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextLight)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copier le texte JSON", fontSize = 11.sp, color = TextLight)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkSurfaceCardBorder)

                    Text("Restaurer depuis un fichier ou texte JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("Coller le contenu du fichier de sauvegarde JSON ici...", fontSize = 10.sp, color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedTextColor = TextLight, unfocusedTextColor = TextLight)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (importJsonText.isNotBlank()) {
                                onImportBackup(importJsonText.trim()) { success, msg ->
                                    isSuccess = success
                                    statusMessage = msg
                                    if (success) {
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restaurer les Données", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (statusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSuccess) StatusGreenBg else Color(0x33EF4444))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = statusMessage!!,
                                color = if (isSuccess) StatusGreen else Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // SECTION 5: DANGER ZONE
                if (selectedSection == "DANGER") {
                    Text("Zone de Danger & Remise à Zéro", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusRed)
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = StatusRedBg.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Remise à Zéro Totale de la Base", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusRed)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Efface définitivement tous les articles, ventes, dettes, fournisseurs et sessions sans laisser aucune trace dans la base de données. Idéal pour configurer une nouvelle boutique propre pour un client.",
                                fontSize = 10.sp,
                                color = TextLight.copy(alpha = 0.8f),
                                lineHeight = 14.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    resetPinInput = ""
                                    resetPinError = null
                                    showFactoryResetDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("open_factory_reset_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Réinitialiser avec Code PIN (0000)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
