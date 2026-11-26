package hu.szujo.hucallblocker

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.unit.dp

@Composable
internal fun OptionsScreen(
    state: ScreenState,
    enableRole: () -> Unit,
    requestNotifications: () -> Unit,
    openPrivateBlocking: () -> Unit
) {
    var showHiddenHelp by remember { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 17.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.fillMaxWidth().widthIn(max = 760.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PageHeading("Opciók", "Engedélyek és alkalmazásinformációk")
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
            FeatureRow(
                Icons.Filled.Info, "Névjegy", "HU Call Blocker · ${BuildConfig.VERSION_NAME}",
                onClick = { showAbout = true }
            )
            Spacer(Modifier.height(10.dp))
        }
    }
    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
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

private const val PROJECT_URL = "https://github.com/Szujo-Janos/HUCallBlocker"

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    var showUrl by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_shield),
                    contentDescription = null,
                    modifier = Modifier.size(86.dp)
                )
                Text(
                    "HU Call Blocker",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        "v${BuildConfig.VERSION_NAME}",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Text(
                    "Magyar fejlesztésű hívásvédelem",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                Text(
                    "Fejlesztő",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Szujó János",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Lock, null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Offline · nincs fiók vagy reklám",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = {
                        try {
                            uriHandler.openUri(PROJECT_URL)
                        } catch (_: IllegalArgumentException) {
                            showUrl = true
                        } catch (_: android.content.ActivityNotFoundException) {
                            showUrl = true
                        } catch (_: SecurityException) {
                            showUrl = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Code, null, Modifier.size(19.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("GitHub projekt")
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Filled.OpenInNew, null, Modifier.size(16.dp))
                }
                if (showUrl) {
                    SelectionContainer {
                        Text(
                            PROJECT_URL,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Bezárás") }
        }
    )
}
