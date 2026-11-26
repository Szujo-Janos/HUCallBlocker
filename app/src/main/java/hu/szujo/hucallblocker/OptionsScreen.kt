package hu.szujo.hucallblocker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun OptionsScreen(
    state: ScreenState,
    enableRole: () -> Unit,
    requestNotifications: () -> Unit,
    openPrivateBlocking: () -> Unit,
    setTheme: (ThemeMode) -> Unit
) {
    var showHiddenHelp by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 17.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.fillMaxWidth().widthIn(max = 760.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PageHeading("Opciók", "Személyre szabás és engedélyek")
            GroupHeading("Megjelenés")
            Surface(
                shape = tileShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(17.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Színtéma", style = MaterialTheme.typography.titleMedium)
                    Text("Válassz világos, sötét vagy rendszer szerinti nézetet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = state.themeMode == mode,
                                onClick = { setTheme(mode) },
                                label = { Text(mode.title) },
                                leadingIcon = if (state.themeMode == mode) {
                                    { Icon(Icons.Filled.Check, null, Modifier.size(15.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }
            GroupHeading("Jogosultságok")
            FeatureRow(
                Icons.Filled.Security,
                "Hívásszűrés",
                if (state.roleHeld) "Engedélyezve" else "Engedély szükséges",
                onClick = if (!state.roleHeld && state.roleAvailable) enableRole else null,
                trailing = if (state.roleHeld) { { SmallTag("Rendben", true) } } else null
            )
            FeatureRow(
                Icons.Filled.NotificationsActive,
                "Állapotsori védelem",
                if (state.notifyAllowed) "Az értesítések engedélyezve vannak"
                else "Az értesítések nincsenek engedélyezve",
                onClick = if (state.active && !state.notifyAllowed) requestNotifications else null
            )
            FeatureRow(
                Icons.Filled.SettingsPhone,
                "Rejtett számok beállítása",
                "A telefon hívásbeállításainál érhető el",
                onClick = { showHiddenHelp = true }
            )
            GroupHeading("Az alkalmazásról")
            FeatureRow(Icons.Filled.Lock, "Adatvédelem",
                "Offline működés · nincs fiók vagy reklám")
            FeatureRow(Icons.Filled.Info, "HU Call Blocker",
                "Verzió: ${BuildConfig.VERSION_NAME}")
            Spacer(Modifier.height(10.dp))
        }
    }
    if (showHiddenHelp) {
        AlertDialog(
            onDismissRequest = { showHiddenHelp = false },
            title = { Text("Rejtett hívószámok") },
            text = {
                Text("A telefon saját hívásbeállításainál tudod engedélyezni " +
                    "a rejtett számok tiltását, ha a készülék támogatja.")
            },
            confirmButton = {
                TextButton(onClick = { showHiddenHelp = false; openPrivateBlocking() }) {
                    Text("Megnyitás")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHiddenHelp = false }) { Text("Mégse") }
            }
        )
    }
}
