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
import androidx.compose.ui.platform.LocalUriHandler
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
    var showGitHubLink by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val githubUrl = "https://github.com/Szujo-Janos/HUCallBlocker"
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
            GroupHeading("Az alkalmazásról")
            FeatureRow(Icons.Filled.Lock, "Adatvédelem",
                "Offline működés · nincs fiók vagy reklám")
            FeatureRow(Icons.Filled.Info, "HU Call Blocker",
                "Verzió: ${BuildConfig.VERSION_NAME}")
            FeatureRow(Icons.Filled.Person, "Fejlesztő", "Szujó János")
            FeatureRow(
                Icons.Filled.Code, "GitHub", "Szujo-Janos/HUCallBlocker",
                onClick = {
                    try {
                        uriHandler.openUri(githubUrl)
                    } catch (_: IllegalArgumentException) {
                        showGitHubLink = true
                    } catch (_: android.content.ActivityNotFoundException) {
                        showGitHubLink = true
                    } catch (_: SecurityException) {
                        showGitHubLink = true
                    }
                }
            )
            Spacer(Modifier.height(10.dp))
        }
    }
    if (showGitHubLink) {
        AlertDialog(
            onDismissRequest = { showGitHubLink = false },
            title = { Text("GitHub") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("A hivatkozás nem nyitható meg automatikusan. A cím másolható:")
                    SelectionContainer { Text(githubUrl) }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGitHubLink = false }) { Text("Bezárás") }
            }
        )
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
