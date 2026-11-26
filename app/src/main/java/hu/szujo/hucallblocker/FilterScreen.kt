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
internal fun FilterScreen(
    state: ScreenState,
    setEnabled: (Boolean) -> Unit,
    setContacts: (Boolean) -> Unit,
    requestContactsPermission: () -> Unit,
    setCountryAllowed: (String, Boolean) -> Unit,
    addNumber: (String, Boolean) -> Boolean,
    removeNumber: (String, Boolean) -> Unit,
    openPrivateBlocking: () -> Unit
) {
    var showCountries by remember { mutableStateOf(false) }
    var showNumbers by remember { mutableStateOf(false) }
    var showHiddenHelp by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 17.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.fillMaxWidth().widthIn(max = 760.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PageHeading("Szűrés", "Minden szabály egy helyen")
            FeatureRow(
                Icons.Filled.Shield,
                "Külföldi számok tiltása",
                if (state.enabled) "Bekapcsolva · a kivételek érvényesek"
                else "Kikapcsolva · minden hívás átjöhet",
                trailing = {
                    Switch(state.enabled, setEnabled, modifier = Modifier.padding(start = 4.dp))
                }
            )
            GroupHeading("Kivételek", "Ezek a beállítások csak aktív szűrésnél számítanak")
            FeatureRow(
                Icons.Filled.Contacts,
                "Mentett névjegyek",
                when {
                    !state.contactsPermission -> "Névjegyengedély szükséges"
                    state.allowContacts -> "A mentett külföldi ismerősöket átengedjük"
                    else -> "A névjegyek nem kapnak kivételt"
                },
                trailing = { Switch(state.allowContacts && state.contactsPermission, setContacts) }
            )
            if (!state.contactsPermission) {
                OutlinedButton(onClick = requestContactsPermission,
                    modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.ContactPage, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Névjegyengedély megadása")
                }
            }
            FeatureRow(
                Icons.Filled.Public,
                "Engedélyezett országok",
                if (state.allowedCountries.isEmpty()) "Nincs országkivétel"
                else "${state.allowedCountries.size} ország engedélyezve",
                onClick = { showCountries = true }
            )
            if (state.allowedCountries.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.allowedCountries.sorted().take(3).forEach { iso ->
                        SmallTag(CountryDialing.name(iso) ?: iso, highlighted = true)
                    }
                    if (state.allowedCountries.size > 3)
                        SmallTag("+${state.allowedCountries.size - 3}")
                }
            }
            FeatureRow(
                Icons.Filled.PhoneAndroid,
                "Saját számlisták",
                "${state.allowedNumbers.size} engedélyezett · ${state.blockedNumbers.size} tiltott",
                onClick = { showNumbers = true }
            )
            HighlightMessage(
                "Sorrend: saját tiltólista → saját engedélyezések → névjegyek → országkivételek. " +
                    "A magyar számokat csak akkor tiltjuk, ha külön a tiltólistára kerültek."
            )
            Spacer(Modifier.height(4.dp))
            GroupHeading("Rendszerbeállítások")
            FeatureRow(
                Icons.Filled.Lock,
                "Rejtett telefonszámok",
                "A telefon saját tiltási beállításaiban kezelhetők",
                onClick = { showHiddenHelp = true }
            )
            Spacer(Modifier.height(10.dp))
        }
    }
    if (showCountries) {
        CountrySelector(state.allowedCountries, setCountryAllowed) { showCountries = false }
    }
    if (showNumbers) {
        NumberRulesEditor(state.allowedNumbers, state.blockedNumbers, addNumber, removeNumber) {
            showNumbers = false
        }
    }
    if (showHiddenHelp) {
        AlertDialog(
            onDismissRequest = { showHiddenHelp = false },
            title = { Text("Rejtett számok") },
            text = {
                Text("Az Android ezeket a hívásokat nem adja át az alkalmazás szűrőjének. " +
                    "A telefon saját hívásbeállításainál tudod tiltani őket, ha támogatott.")
            },
            confirmButton = {
                TextButton(onClick = { showHiddenHelp = false; openPrivateBlocking() }) {
                    Text("Telefon beállításai")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHiddenHelp = false }) { Text("Mégse") }
            }
        )
    }
}
