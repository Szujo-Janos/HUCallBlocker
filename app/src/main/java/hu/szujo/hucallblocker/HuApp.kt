package hu.szujo.hucallblocker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val mint = Color(0xFF00B98E)
private val forest = Color(0xFF087D69)
private val ink = Color(0xFF142332)
private val grey = Color(0xFF697C8C)
private val light = Color(0xFFF5F8FC)
private val red = Color(0xFFE75D64)
private val warning = Color(0xFFC18B30)
private val scheme = lightColorScheme(
    primary = forest, onPrimary = Color.White, secondary = mint,
    background = light, onBackground = ink,
    surface = Color.White, onSurface = ink
)

@Composable
internal fun HuApp(
    state: ScreenState,
    enableRole: () -> Unit,
    setEnabled: (Boolean) -> Unit,
    setContacts: (Boolean) -> Unit,
    requestNotifications: () -> Unit,
    openPrivateBlocking: () -> Unit,
    clearCalls: () -> Unit,
    refresh: () -> Unit
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            containerColor = light,
            topBar = { Header(state.active) },
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    val tabs = listOf(
                        Triple("Főoldal", Icons.Filled.Home, 0),
                        Triple("Szűrés", Icons.Filled.Tune, 1),
                        Triple("Napló", Icons.Filled.History, 2),
                        Triple("Opciók", Icons.Filled.Settings, 3)
                    )
                    tabs.forEach { (title, icon, index) ->
                        NavigationBarItem(
                            selected = page == index,
                            onClick = { page = index },
                            icon = { Icon(icon, contentDescription = title) },
                            label = { Text(title, fontSize = 10.sp) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        ) { padding ->
            Crossfade(targetState = page, animationSpec = tween(260), label = "tabFade") { selected ->
                when (selected) {
                    0 -> Home(state, padding, enableRole, refresh)
                    1 -> Filters(state, padding, setEnabled, setContacts, openPrivateBlocking)
                    2 -> LogPage(state, padding, clearCalls, refresh)
                    else -> Options(state, padding, enableRole, requestNotifications, openPrivateBlocking)
                }
            }
        }
    }
}

@Composable
private fun Header(active: Boolean) {
    Row(
        Modifier.fillMaxWidth().background(light)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painterResource(R.drawable.ic_shield), "HU pajzs", Modifier.size(51.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("HU CALL BLOCKER", color = ink, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Text("CALL PROTECTION", color = grey, fontSize = 10.sp, letterSpacing = 1.6.sp)
        }
        Box(Modifier.size(10.dp).background(if (active) mint else red, CircleShape))
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null) {
    Column {
        Text(title, color = ink, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        if (subtitle != null) Text(subtitle, color = grey, fontSize = 12.sp)
    }
}

@Composable
private fun Home(
    state: ScreenState, padding: PaddingValues,
    enableRole: () -> Unit, refresh: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        AnimatedStatus(state, enableRole)
        Text("ÁTTEKINTÉS", color = grey, fontSize = 11.sp,
            letterSpacing = 1.3.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metric("LETILTVA", state.calls.size.toString(), Icons.Filled.Block, Modifier.weight(1f))
            Metric("MAGYAR HÍVÁS", "+36", Icons.Filled.Check, Modifier.weight(1f))
        }
        InfoTile(
            Icons.Filled.Security, "Aktív szabály",
            if (state.enabled) "Külföldi előhívók tiltása" else "Szűrés szünetel"
        )
        OutlinedButton(onClick = refresh, modifier = Modifier.fillMaxWidth()) {
            Text("Állapot frissítése")
        }
    }
}

@Composable
private fun AnimatedStatus(state: ScreenState, enableRole: () -> Unit) {
    val activeColor = when {
        state.active -> forest
        !state.roleHeld -> red
        else -> warning
    }
    val bg = when {
        state.active -> Color(0xFFE4FFF5)
        !state.roleHeld -> Color(0xFFFFF0F0)
        else -> Color(0xFFFFF6E5)
    }
    val tag = when {
        !state.roleAvailable -> "NEM TÁMOGATOTT"
        !state.roleHeld -> "ENGEDÉLY SZÜKSÉGES"
        !state.enabled -> "SZÜNETEL"
        else -> "VÉDELEM AKTÍV"
    }
    val title = when {
        !state.roleAvailable -> "Nem támogatott"
        !state.roleHeld -> "Kapcsold be!"
        !state.enabled -> "Védelem szünetel"
        else -> "Minden rendben."
    }
    val text = when {
        !state.roleHeld -> "Engedélyezd a hívásszűrést a telefonon."
        !state.enabled -> "A blokkolás kikapcsolva."
        else -> "A külföldi számokat szűrjük."
    }
    val pulse = rememberInfiniteTransition(label = "shieldLoop")
    val scale by pulse.animateFloat(
        initialValue = 0.96f, targetValue = 1.055f,
        animationSpec = infiniteRepeatable(tween(1900), RepeatMode.Reverse), label = "scale"
    )
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    AnimatedVisibility(
        visible = shown,
        enter = fadeIn(tween(450)) + slideInVertically(tween(450), initialOffsetY = { it / 4 })
    ) {
        Card(
            shape = RoundedCornerShape(29.dp),
            colors = CardDefaults.cardColors(containerColor = bg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(activeColor, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(tag, color = activeColor, fontSize = 11.sp,
                        letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Black,
                            color = ink, lineHeight = 30.sp)
                        Spacer(Modifier.height(9.dp))
                        Text(text, fontSize = 13.sp, color = grey, lineHeight = 18.sp)
                    }
                    Box(
                        Modifier.size(87.dp).scale(if (state.active) scale else 1f)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color.White, if (state.active) Color(0xFFA8F0D4) else Color(0xFFF8C8CC))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(painterResource(R.drawable.ic_shield), "HU védelem", Modifier.size(65.dp))
                    }
                }
                if (state.roleAvailable && !state.roleHeld) {
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = enableRole, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(13.dp)) {
                        Text("Hívásszűrés engedélyezése")
                    }
                }
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(18.dp)) {
            Icon(icon, null, tint = forest, modifier = Modifier.size(23.dp))
            Spacer(Modifier.height(14.dp))
            Text(value, color = ink, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp)
            Text(label, color = grey, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InfoTile(icon: ImageVector, title: String, subtitle: String, click: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth().then(if (click == null) Modifier else Modifier.clickable(onClick = click)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(43.dp).background(Color(0xFFE7FAF4), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = forest, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = grey, fontSize = 12.sp, lineHeight = 16.sp)
            }
            if (click != null) Icon(Icons.Filled.ChevronRight, null, tint = grey)
        }
    }
}

@Composable
private fun Filters(
    state: ScreenState, padding: PaddingValues,
    setEnabled: (Boolean) -> Unit, setContacts: (Boolean) -> Unit,
    openPrivateBlocking: () -> Unit
) {
    var showHelp by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 17.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        SectionTitle("Szűrés", "Döntsd el, kit engedünk át.")
        OptionSwitch(Icons.Filled.Block, "Külföldi számok", "A nem +36-os nemzetközi számok", state.enabled, setEnabled)
        OptionSwitch(Icons.Filled.Phone, "Mentett névjegyek", "Külföldi ismerősök átengedése", state.allowContacts, setContacts)
        Spacer(Modifier.height(5.dp))
        SectionTitle("Rejtett számok", "A Telefon alkalmazás beállítása")
        InfoTile(
            Icons.Filled.Lock,
            "Magán / ismeretlen hívók",
            "Rendszerszintű tiltás beállítása",
            click = { showHelp = true }
        )
        Text("A rejtett számokat az Android nem adja át ennek a szűrőnek.",
            color = grey, fontSize = 12.sp, lineHeight = 18.sp)
    }
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            icon = { Icon(Icons.Filled.Lock, null) },
            title = { Text("Rejtett számok") },
            text = {
                Text("A Telefon → Beállítások → Letiltott számok → Ismeretlen / Rejtett számok menüben kapcsolhatod be, ha a telefon támogatja. Ezt a HU Call Blocker nem állíthatja át helyetted.")
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false; openPrivateBlocking() }) {
                    Text("Telefon beállításai")
                }
            },
            dismissButton = { TextButton(onClick = { showHelp = false }) { Text("Mégse") } }
        )
    }
}

@Composable
private fun OptionSwitch(
    icon: ImageVector, title: String, subtitle: String,
    checked: Boolean, onChange: (Boolean) -> Unit
) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = forest, modifier = Modifier.size(23.dp))
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = grey, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Switch(checked = checked, onCheckedChange = onChange,
                colors = SwitchDefaults.colors(checkedTrackColor = forest))
        }
    }
}

@Composable
private fun LogPage(state: ScreenState, padding: PaddingValues, clear: () -> Unit, refresh: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 17.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                SectionTitle("Napló", state.calls.size.toString() + " blokkolt hívás")
            }
            if (state.calls.isNotEmpty()) {
                IconButton(onClick = { confirm = true }) {
                    Icon(Icons.Filled.Delete, "Napló törlése", tint = red)
                }
            }
        }
        Spacer(Modifier.height(15.dp))
        if (state.calls.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Security, null, tint = mint, modifier = Modifier.size(43.dp))
                    Spacer(Modifier.height(15.dp))
                    Text("Nincs blokkolt hívás", color = ink, fontWeight = FontWeight.Bold)
                    Text("A napló üres.", color = grey, fontSize = 12.sp)
                    Spacer(Modifier.height(15.dp))
                    OutlinedButton(onClick = refresh) { Text("Frissítés") }
                }
            }
        } else {
            val format = remember { SimpleDateFormat("yyyy.MM.dd.  HH:mm", Locale("hu", "HU")) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.calls) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(18.dp)) {
                        Row(Modifier.fillMaxWidth().padding(17.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Block, null, tint = red)
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.number, color = ink, fontWeight = FontWeight.Bold)
                                Text(format.format(Date(item.timestamp)), color = grey, fontSize = 12.sp)
                            }
                            Text("TILTVA", color = red, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Napló törlése?") },
            text = { Text("A HU Call Blocker saját naplója törlődik.") },
            confirmButton = {
                TextButton(onClick = { confirm = false; clear() }) { Text("Törlés") }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Mégse") } }
        )
    }
}

@Composable
private fun Options(
    state: ScreenState, padding: PaddingValues,
    enableRole: () -> Unit, requestNotifications: () -> Unit,
    openPrivateBlocking: () -> Unit
) {
    var showHelp by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 17.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        SectionTitle("Opciók", "HU Call Blocker 1.1.0")
        InfoTile(
            Icons.Filled.Security, "Hívásszűrési engedély",
            if (state.roleHeld) "Rendben" else "Engedély szükséges",
            if (!state.roleHeld && state.roleAvailable) enableRole else null
        )
        InfoTile(
            Icons.Filled.NotificationsActive, "Állapotsori HU pajzs",
            if (state.notifyAllowed) "Értesítés engedélyezve" else "Értesítés nincs engedélyezve",
            if (state.active && !state.notifyAllowed) requestNotifications else null
        )
        InfoTile(Icons.Filled.Lock, "Magánszámok tiltása", "Telefonrendszer beállítása",
            click = { showHelp = true })
        InfoTile(Icons.Filled.Check, "Adatvédelem", "Offline működés. Nincs fiók.")
    }
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Rejtett számok") },
            text = { Text("A Telefon → Beállítások → Letiltott számok menüben kapcsolható be, ha a készülék támogatja.") },
            confirmButton = {
                TextButton(onClick = { showHelp = false; openPrivateBlocking() }) { Text("Megnyitás") }
            },
            dismissButton = { TextButton(onClick = { showHelp = false }) { Text("Mégse") } }
        )
    }
}
