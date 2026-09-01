package com.example.ui.components

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
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
import com.example.util.UserManager
import com.example.util.UserRole

@Composable
fun UserProfileSwitchDialog(
    currentRole: UserRole,
    currentName: String,
    onRoleChanged: (UserRole, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showPinCheckForManager by remember { mutableStateOf(false) }
    var showChangePinSection by remember { mutableStateOf(false) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var customUserName by remember { mutableStateOf(currentName) }

    if (showPinCheckForManager) {
        PinAuthDialog(
            actionTitle = "Connexion Mode Gérant",
            actionDescription = "Veuillez entrer votre code PIN pour activer le profil Gérant",
            onSuccess = {
                showPinCheckForManager = false
                UserManager.setCurrentUser(context, UserRole.GERANT, customUserName.ifBlank { "Mamadou (Gérant)" })
                onRoleChanged(UserRole.GERANT, customUserName.ifBlank { "Mamadou (Gérant)" })
                Toast.makeText(context, "Profil Gérant activé !", Toast.LENGTH_SHORT).show()
                onDismiss()
            },
            onDismiss = { showPinCheckForManager = false }
        )
        return
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .testTag("user_profile_dialog"),
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(GoldContainer)
                                .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = GoldLight, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Profil & Droits d'Accès", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            Text("Sécurité & Rôles de caisse", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Profile Selection Cards
                Text("Choisir le profil actif :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                Spacer(modifier = Modifier.height(8.dp))

                // Option 1: GERANT
                val isGerantActive = currentRole == UserRole.GERANT
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            if (!isGerantActive) {
                                showPinCheckForManager = true
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isGerantActive) GoldContainer.copy(alpha = 0.6f) else DarkSurfaceCard),
                    border = BorderStroke(1.dp, if (isGerantActive) GoldPrimary else DarkSurfaceCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text("👑", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Gérant (Administrateur)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                Text("Accès complet : Bénéfices, Marges, Prix d'achat, Suppression", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                        if (isGerantActive) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = TextDark, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: CAISSIER
                val isCaissierActive = currentRole == UserRole.CAISSIER
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            if (!isCaissierActive) {
                                val name = if (customUserName.contains("Gérant")) "Awa (Caissière)" else customUserName
                                UserManager.setCurrentUser(context, UserRole.CAISSIER, name)
                                onRoleChanged(UserRole.CAISSIER, name)
                                Toast.makeText(context, "Profil Caissier activé (mode restreint) !", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isCaissierActive) GoldContainer.copy(alpha = 0.6f) else DarkSurfaceCard),
                    border = BorderStroke(1.dp, if (isCaissierActive) GoldPrimary else DarkSurfaceCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text("👤", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Caissier (Vendeur)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                Text("Mode sécurisé : Ventes et encaissements seuls (Marges masquées)", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                        if (isCaissierActive) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = TextDark, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = DarkSurfaceCardBorder)

                // Change PIN section
                if (!showChangePinSection) {
                    OutlinedButton(
                        onClick = { showChangePinSection = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Modifier le code PIN Gérant", fontSize = 12.sp, color = GoldLight)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurfaceCard)
                            .padding(12.dp)
                    ) {
                        Text("Changer le code PIN Gérant", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = oldPin,
                            onValueChange = { if (it.length <= 4) oldPin = it },
                            placeholder = { Text("Ancien PIN (ex: 1234)", fontSize = 12.sp, color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { if (it.length <= 4) newPin = it },
                            placeholder = { Text("Nouveau PIN (4 chiffres)", fontSize = 12.sp, color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showChangePinSection = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Annuler", fontSize = 11.sp, color = TextMuted)
                            }

                            Button(
                                onClick = {
                                    if (!UserManager.verifyManagerPin(context, oldPin)) {
                                        Toast.makeText(context, "Ancien PIN incorrect !", Toast.LENGTH_SHORT).show()
                                    } else if (newPin.length != 4) {
                                        Toast.makeText(context, "Le nouveau PIN doit faire 4 chiffres", Toast.LENGTH_SHORT).show()
                                    } else {
                                        UserManager.setManagerPin(context, newPin)
                                        Toast.makeText(context, "Code PIN mis à jour avec succès !", Toast.LENGTH_LONG).show()
                                        showChangePinSection = false
                                        oldPin = ""
                                        newPin = ""
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark)
                            ) {
                                Text("Enregistrer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
