package hu.szujo.hucallblocker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun HomeScreen(state: ScreenState, enableRole: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 540.dp
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = if (compact) 9.dp else 17.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 17.dp)
        ) {
            PageHeading("Áttekintés", "A telefonod hívásvédelme")
            ProtectionHero(state, enableRole, compact)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeMetric(
                    "NAPLÓZOTT TILTÁS", state.calls.size.toString(),
                    Icons.Filled.CallEnd, Modifier.weight(1f)
                )
                HomeMetric(
                    "AKTÍV KIVÉTEL",
                    (state.allowedCountries.size + state.allowedNumbers.size).toString(),
                    Icons.Filled.CheckCircle, Modifier.weight(1f)
                )
            }
            GroupHeading("Védelmi áttekintés")
            val ruleTitle = if (state.enabled) "Nemzetközi számok szűrése" else "A szűrés szünetel"
            FeatureRow(
                Icons.Filled.Security, "Aktív szabály", ruleTitle
            )
            if (state.calls.isNotEmpty()) {
                val latest = state.calls.first()
                val format = remember {
                    SimpleDateFormat("MM. dd.  HH:mm", Locale.forLanguageTag("hu-HU"))
                }
                FeatureRow(
                    Icons.Filled.History,
                    "Legutóbbi blokkolás",
                    "${CountryDialing.label(latest.number, latest.countryIso)} · " +
                        format.format(Date(latest.timestamp))
                )
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun ProtectionHero(state: ScreenState, enableRole: () -> Unit, compact: Boolean) {
    val stateLabel = when {
        !state.roleAvailable -> "NEM TÁMOGATOTT"
        !state.roleHeld -> "ENGEDÉLY SZÜKSÉGES"
        !state.enabled -> "SZÜNETEL"
        else -> "VÉDELEM AKTÍV"
    }
    val title = when {
        !state.roleAvailable -> "Nem elérhető"
        !state.roleHeld -> "Kapcsold be a védelmet"
        !state.enabled -> "Védelem szünetel"
        else -> "Minden rendben."
    }
    val hint = when {
        !state.roleAvailable -> "Ez a telefon nem támogatja a hivatalos Android-hívásszűrést."
        !state.roleHeld -> "Engedélyezd a szűrési jogosultságot a telefonodon."
        !state.enabled -> "A beállított szabályok most nem blokkolnak hívásokat."
        else -> "A külföldi hívásokat a beállított szabályok szerint kezeljük."
    }
    val motion = rememberInfiniteTransition(label = "pajzsfény")
    val breathing by motion.animateFloat(
        initialValue = 0.99f, targetValue = 1.045f,
        animationSpec = infiniteRepeatable(tween(2100), RepeatMode.Reverse),
        label = "pajzslélegzés"
    )
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(visible, enter = fadeIn(tween(250))) {
        Surface(shape = RoundedCornerShape(29.dp), color = ShieldColors.heroMint) {
            Box(
                Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(ShieldColors.heroPale, ShieldColors.heroMint))
                )
            ) {
                Canvas(Modifier.matchParentSize()) {
                    val radius = size.minDimension * 0.53f
                    val center = androidx.compose.ui.geometry.Offset(size.width * 0.89f, size.height * 0.12f)
                    drawCircle(ShieldColors.heroInk.copy(alpha = 0.06f), radius, center)
                    drawCircle(ShieldColors.heroInk.copy(alpha = 0.12f), radius * 0.63f,
                        center, style = Stroke(width = 1.dp.toPx()))
                }
                Column(
                    Modifier.fillMaxWidth().padding(if (compact) 17.dp else 23.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(
                            if (state.active) ShieldColors.heroInk else ShieldColors.heroWarning, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(stateLabel, fontSize = 11.sp, letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.active) ShieldColors.heroInk else ShieldColors.heroWarning)
                    }
                    Spacer(Modifier.height(if (compact) 3.dp else 8.dp))
                    Box(Modifier.size(if (compact) 102.dp else 139.dp),
                        contentAlignment = Alignment.Center) {
                        Canvas(Modifier.fillMaxSize()) {
                            val center = androidx.compose.ui.geometry.Offset(size.width/2, size.height/2)
                            drawCircle(ShieldColors.heroInk.copy(alpha = 0.06f),
                                size.minDimension * 0.48f, center)
                            drawCircle(ShieldColors.heroInk.copy(alpha = 0.25f),
                                size.minDimension * 0.41f, center,
                                style = Stroke(width = 1.5.dp.toPx()))
                            drawCircle(ShieldColors.heroInk.copy(alpha = 0.15f),
                                size.minDimension * 0.34f, center,
                                style = Stroke(width = 1.dp.toPx()))
                        }
                        Image(
                            painterResource(R.drawable.ic_shield), "HU védelem",
                            Modifier.size(if (compact) 78.dp else 94.dp)
                                .scale(if (state.active) breathing else 1f)
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(title, style = MaterialTheme.typography.headlineMedium, color = ShieldColors.navy)
                    Spacer(Modifier.height(7.dp))
                    Text(hint, style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF294D45),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    if (state.roleAvailable && !state.roleHeld) {
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = enableRole,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ShieldColors.heroInk,
                                contentColor = Color.White
                            )
                        ) { Text("Hívásszűrés engedélyezése") }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeMetric(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(modifier, shape = tileShape, color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp))
            Text(value, style = MaterialTheme.typography.headlineLarge)
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
