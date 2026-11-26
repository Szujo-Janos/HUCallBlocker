package hu.szujo.hucallblocker

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Tab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
private val tabs = listOf(
    Tab("Főoldal", Icons.Filled.Home),
    Tab("Szűrés", Icons.Filled.Tune),
    Tab("Napló", Icons.Filled.History),
    Tab("Opciók", Icons.Filled.Settings)
)

@Composable
internal fun HuApp(
    state: ScreenState,
    enableRole: () -> Unit,
    setEnabled: (Boolean) -> Unit,
    setContacts: (Boolean) -> Unit,
    requestContactsPermission: () -> Unit,
    setCountryAllowed: (String, Boolean) -> Unit,
    addNumber: (String, Boolean) -> Boolean,
    removeNumber: (String, Boolean) -> Unit,
    setTheme: (ThemeMode) -> Unit,
    requestNotifications: () -> Unit,
    openPrivateBlocking: () -> Unit,
    clearCalls: () -> Unit,
    refresh: () -> Unit
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    ShieldTheme(state.themeMode) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 600.dp
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = { TopBar(state.active, refresh) },
                bottomBar = {
                    if (!wide) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                NavigationBarItem(
                                    selected = page == index,
                                    onClick = { page = index },
                                    icon = { Icon(tab.icon, contentDescription = tab.title) },
                                    label = { Text(tab.title, fontSize = 11.sp) },
                                    alwaysShowLabel = true,
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            ) { padding ->
                Row(Modifier.fillMaxSize().padding(padding)) {
                    if (wide) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Spacer(Modifier.height(16.dp))
                            tabs.forEachIndexed { index, tab ->
                                NavigationRailItem(
                                    selected = page == index,
                                    onClick = { page = index },
                                    icon = { Icon(tab.icon, contentDescription = tab.title) },
                                    label = { Text(tab.title) },
                                    alwaysShowLabel = true,
                                    colors = NavigationRailItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                    Crossfade(
                        targetState = page,
                        animationSpec = tween(210),
                        label = "képernyőváltás",
                        modifier = Modifier.fillMaxSize().weight(1f)
                    ) { selected ->
                        when (selected) {
                            0 -> HomeScreen(state, enableRole)
                            1 -> FilterScreen(
                                state, setEnabled, setContacts, requestContactsPermission,
                                setCountryAllowed, addNumber, removeNumber, openPrivateBlocking
                            )
                            2 -> LogScreen(state, clearCalls, refresh, addNumber)
                            else -> OptionsScreen(
                                state, enableRole, requestNotifications,
                                openPrivateBlocking, setTheme
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(active: Boolean, refresh: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(
                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
            )).padding(horizontal = 19.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(47.dp).clip(RoundedCornerShape(15.dp))
                .background(ShieldColors.navy),
            contentAlignment = Alignment.Center
        ) {
            Image(painterResource(R.drawable.ic_shield), "HU pajzs", Modifier.size(41.dp))
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text("HU CALL BLOCKER", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold)
            Text("HÍVÁSVÉDELEM", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.1.sp)
        }
        Box(Modifier.size(9.dp).background(
            if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            CircleShape
        ))
        Spacer(Modifier.width(5.dp))
        IconButton(onClick = refresh) {
            Icon(Icons.Filled.Refresh, "Állapot frissítése",
                tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}
