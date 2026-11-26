package hu.szujo.hucallblocker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
private fun FullPageDialog(title: String, close: () -> Unit, body: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = close, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = close) { Icon(Icons.Filled.ArrowBack, "Vissza") }
                    Spacer(Modifier.width(6.dp))
                    Text(title, style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f))
                    TextButton(onClick = close) { Text("Kész") }
                }
                Spacer(Modifier.height(10.dp))
                body()
            }
        }
    }
}

@Composable
internal fun CountrySelector(
    allowed: Set<String>, setAllowed: (String, Boolean) -> Unit, close: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val countries = remember(query) {
        val text = query.trim()
        CountryDialing.countries.filter {
            text.isEmpty() || it.name.contains(text, ignoreCase = true) ||
                it.iso.contains(text, ignoreCase = true) ||
                ("+" + it.callingCode).contains(text)
        }
    }
    FullPageDialog("Országkivételek", close) {
        Text("A kijelölt országokból fogadhatsz hívásokat.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            query, { query = it },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            label = { Text("Ország vagy előhívó keresése") },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            trailingIcon = if (query.isNotBlank()) {
                { IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, "Törlés") } }
            } else null
        )
        Spacer(Modifier.height(9.dp))
        SmallTag("${allowed.size} engedélyezett ország", highlighted = true)
        Spacer(Modifier.height(9.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(countries, key = { it.iso }) { country ->
                val checked = country.iso in allowed
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (checked) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                        .clickable { setAllowed(country.iso, !checked) }
                ) {
                    Row(Modifier.padding(start = 15.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(country.name, style = MaterialTheme.typography.titleMedium)
                            Text("+${country.callingCode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Checkbox(checked, onCheckedChange = { setAllowed(country.iso, it) })
                    }
                }
            }
        }
    }
}

@Composable
internal fun NumberRulesEditor(
    allowed: Set<String>, blocked: Set<String>,
    addNumber: (String, Boolean) -> Boolean,
    removeNumber: (String, Boolean) -> Unit,
    close: () -> Unit
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var input by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val isAllow = selected == 0
    val numbers = if (isAllow) allowed else blocked
    FullPageDialog("Saját számlisták", close) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = isAllow, onClick = { selected = 0; error = false },
                label = { Text("Engedélyezett (${allowed.size})") },
                leadingIcon = if (isAllow) { { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) } } else null
            )
            FilterChip(
                selected = !isAllow, onClick = { selected = 1; error = false },
                label = { Text("Tiltott (${blocked.size})") },
                leadingIcon = if (!isAllow) { { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) } } else null
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it; error = false },
            label = { Text("Telefonszám") },
            placeholder = { Text("+49 151 12345678") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            isError = error
        )
        Spacer(Modifier.height(5.dp))
        Text("Nemzetközi formátum (+ vagy 00), magyar számnál 06 is lehet.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        Button(
            enabled = NumberRules.normalize(input) != null,
            onClick = {
                if (addNumber(input, isAllow)) input = "" else error = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(if (isAllow) Icons.Filled.PersonAdd else Icons.Filled.Block,
                null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (isAllow) "Szám engedélyezése" else "Szám tiltása")
        }
        if (error) {
            Text("Nem menthető. Egy listában legfeljebb 250 szám lehet.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(14.dp))
        GroupHeading("Mentett számok")
        Spacer(Modifier.height(6.dp))
        if (numbers.isEmpty()) {
            HighlightMessage("Még nincs szám ebben a listában.")
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                items(numbers.sorted(), key = { it }) { number ->
                    Surface(color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.fillMaxWidth().padding(start = 15.dp, end = 5.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(number, style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f))
                            IconButton(onClick = { removeNumber(number, isAllow) }) {
                                Icon(Icons.Filled.DeleteOutline, "Szám eltávolítása",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
