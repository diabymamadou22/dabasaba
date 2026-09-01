package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.R
import com.example.ui.components.PinAuthDialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.util.UserManager
import com.example.util.UserRole
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    val scale = remember { Animatable(0.4f) }
    val alpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }
    var showFeatures by remember { mutableStateOf(false) }
    var isProgressCompleted by remember { mutableStateOf(false) }
    var showPinDialogForGerant by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_halo")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    LaunchedEffect(Unit) {
        // Animation sequence
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
        scale.animateTo(
            targetValue = 1.05f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(250, easing = LinearEasing)
        )

        showFeatures = true

        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1500, easing = LinearEasing)
        )

        isProgressCompleted = true
    }

    if (showPinDialogForGerant) {
        PinAuthDialog(
            actionTitle = "Connexion Mode Gérant",
            actionDescription = "Saisissez le code PIN Gérant (par défaut : 1234)",
            onSuccess = {
                showPinDialogForGerant = false
                UserManager.setCurrentUser(context, UserRole.GERANT, "Mamadou (Gérant)")
                Toast.makeText(context, "Connecté en Mode Gérant !", Toast.LENGTH_SHORT).show()
                onSplashFinished()
            },
            onDismiss = {
                showPinDialogForGerant = false
            }
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen"),
        color = DarkBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E2838),
                            Color(0xFF0F1520),
                            DarkBackground
                        ),
                        radius = 1200f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Animated Glowing Emblem with the Logo
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(160.dp)
                ) {
                    // Outer Gold Glow Halo
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(haloPulse)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        GoldPrimary.copy(alpha = haloAlpha * 0.45f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Logo Container
                    Box(
                        modifier = Modifier
                            .size(125.dp)
                            .scale(scale.value)
                            .shadow(20.dp, RoundedCornerShape(28.dp), spotColor = GoldPrimary)
                            .clip(RoundedCornerShape(28.dp))
                            .background(DarkSurfaceElevated)
                            .border(
                                BorderStroke(2.dp, Brush.linearGradient(listOf(GoldLight, GoldPrimary, Color(0xFF8B5E1E)))),
                                RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.dabasaba_logo),
                            contentDescription = "Logo DabaSaba",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Brand Name & Tagline
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.scale(scale.value)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DABA",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldLight,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "SABA",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = TextLight,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "GESTION COMMERCIALE & CAISSE POS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Ventes • Stocks • Carnet de Crédits • 100% Offline",
                        fontSize = 11.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Bar & Status
                Column(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { progress.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = GoldPrimary,
                        trackColor = DarkSurfaceCardBorder
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (progress.value < 0.4f) "Initialisation de la base de données..."
                        else if (progress.value < 0.85f) "Chargement du catalogue & des crédits..."
                        else "Système prêt. Choisissez votre mode de connexion.",
                        fontSize = 11.sp,
                        color = if (isProgressCompleted) GoldLight else TextMuted,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Connection Mode Selector Box (Mode Gérant vs Mode Caissier)
                AnimatedVisibility(
                    visible = showFeatures || isProgressCompleted,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 30 }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurfaceElevated.copy(alpha = 0.95f))
                            .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)), RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CHOISISSEZ VOTRE MODE DE CONNEXION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldLight,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Option 1: 👑 GÉRANT
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPinDialogForGerant = true
                                }
                                .testTag("splash_login_gerant_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(GoldContainer)
                                        .border(BorderStroke(1.dp, GoldPrimary), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "👑", fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Gérant / Propriétaire",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(GoldPrimary.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "PIN Requis",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldLight
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Accès total : Stocks, Bilans PDF/Excel, Fournisseurs, Remise à zéro",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        lineHeight = 14.sp
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = GoldLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Option 2: 👤 CAISSIER
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    UserManager.setCurrentUser(context, UserRole.CAISSIER, "Awa (Caissière)")
                                    Toast.makeText(context, "Connecté en Mode Caissier !", Toast.LENGTH_SHORT).show()
                                    onSplashFinished()
                                }
                                .testTag("splash_login_caissier_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            border = BorderStroke(1.dp, DarkSurfaceCardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurfaceElevated)
                                        .border(BorderStroke(1.dp, DarkSurfaceCardBorder), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "👤", fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Caissier / Vendeur",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(StatusGreen.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Direct",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = StatusGreen
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Ventes au comptoir, Scan code-barres, Encaissement & Tickets",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        lineHeight = 14.sp
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = TextLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
