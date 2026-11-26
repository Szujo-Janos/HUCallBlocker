package hu.szujo.hucallblocker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LogScreen(
    state: ScreenState,
    clear: () -> Unit,
    refresh: () -> Unit,
    addNumber: (String, Boolean) -> Boolean
) {
    var selected by remember { mutableStateOf<BlockedCallStore.Entry?>(null) }
    var clearDialog by remember { mutableStateOf(false) }
    val messages = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clock = remember { SimpleDateFormat("HH:mm", Locale.forLanguageTag("hu-HU")) }
    val dayFormat = remember { SimpleDateFormat("yyyy. MM. dd.", Locale.forLanguageTag("hu-HU")) }
    val groups = remember(state.calls) { state.calls.groupBy { dayFormat.format(Date(it.timestamp)) } }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    PageHeading("Napló", "Az utolsó 100 blokkolt hívás")
                }
                if (state.calls.isNotEmpty()) {
                    IconButton(onClick = { clearDialog = true }) {
                        Icon(Icons.Filled.DeleteOutline, "Napló törlése",
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            if (state.calls.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(Modifier.padding(25.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.Security, null, Modifier.size(51.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Text("Nincs blokkolt hívás", style = MaterialTheme.typography.titleLarge)
                        Text("Az események itt jelennek meg.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedButton(onClick = refresh) { Text("Frissítés") }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    groups.forEach { (day, calls) ->
                        item(key = "day:$day") {
                            Text(day, style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 10.dp, bottom = 5.dp))
                        }
                        items(calls, key = { "${it.timestamp}:${it.number}" }) { entry ->
                            val countryLabel = remember(entry.number, entry.countryIso) {
                                CountryDialing.label(entry.number, entry.countryIso, withFlag = true)
                            }
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth().clickable { selected = entry }
                            ) {
                                Row(Modifier.padding(horizontal = 15.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(43.dp).background(
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.11f),
                                            CircleShape
                                        ), contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.CallEnd, null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(entry.number, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "$countryLabel · ${entry.reason}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }
                                    Spacer(Modifier.width(6.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(clock.format(Date(entry.timestamp)),
                                            style = MaterialTheme.typography.labelMedium)
                                        Icon(Icons.Filled.ChevronRight, null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        SnackbarHost(messages, modifier = Modifier.align(Alignment.BottomCenter))
    }
    if (clearDialog) {
        AlertDialog(
            onDismissRequest = { clearDialog = false },
            title = { Text("Napló törlése?") },
            text = { Text("Csak az alkalmazás saját naplója törlődik.") },
            confirmButton = {
                TextButton(onClick = { clear(); clearDialog = false }) { Text("Törlés") }
            },
            dismissButton = {
                TextButton(onClick = { clearDialog = false }) { Text("Mégse") }
            }
        )
    }
    selected?.let { entry ->
        val countryLabel = remember(entry.number, entry.countryIso) {
            CountryDialing.label(entry.number, entry.countryIso, withFlag = true)
        }
        val normalized = NumberRules.normalize(entry.number)
        val inAllow = normalized != null && normalized in state.allowedNumbers
        val inBlock = normalized != null && normalized in state.blockedNumbers
        ModalBottomSheet(onDismissRequest = { selected = null }) {
            Column(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Hívás részletei", style = MaterialTheme.typography.headlineMedium)
                Text(entry.number, style = MaterialTheme.typography.titleLarge)
                SmallTag(countryLabel, highlighted = true)
                Text("Tiltás oka: ${entry.reason}", style = MaterialTheme.typography.bodyMedium)
                Text(dayFormat.format(Date(entry.timestamp)) + " " + clock.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider()
                Button(
                    onClick = {
                        val success = addNumber(entry.number, true)
                        if (success) {
                            selected = null
                            scope.launch { messages.showSnackbar("Szám engedélyezve") }
                        }
                    },
                    enabled = normalized != null && !inAllow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CheckCircle, null, Modifier.size(19.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (inAllow) "Már engedélyezett" else "Szám engedélyezése")
                }
                OutlinedButton(
                    onClick = {
                        val success = addNumber(entry.number, false)
                        if (success) {
                            selected = null
                            scope.launch { messages.showSnackbar("Szám tiltólistára helyezve") }
                        }
                    },
                    enabled = normalized != null && !inBlock,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Block, null, Modifier.size(19.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (inBlock) "Már a tiltólistán" else "Saját tiltólistára")
                }
            }
        }
    }
}
