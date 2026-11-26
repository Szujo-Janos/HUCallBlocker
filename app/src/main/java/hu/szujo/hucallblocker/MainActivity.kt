package hu.szujo.hucallblocker

import android.Manifest
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.content.res.Configuration
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

internal data class ScreenState(
    val roleAvailable: Boolean = true,
    val roleHeld: Boolean = false,
    val enabled: Boolean = true,
    val allowContacts: Boolean = true,
    val contactsPermission: Boolean = false,
    val allowedCountries: Set<String> = emptySet(),
    val allowedNumbers: Set<String> = emptySet(),
    val blockedNumbers: Set<String> = emptySet(),
    val notifyAllowed: Boolean = false,
    val calls: List<BlockedCallStore.Entry> = emptyList()
) {
    val active: Boolean get() = roleAvailable && roleHeld && enabled
}

class MainActivity : ComponentActivity() {
    private var state by mutableStateOf(ScreenState())
    private var requestedContactSetting: Boolean? = null

    private val roleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refresh()
        if (state.active && !state.notifyAllowed) askNotifications()
    }
    private val contactLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val desired = requestedContactSetting
        requestedContactSetting = null
        if (granted && desired != null) SettingsStore.setAllowContacts(this, desired)
        refresh()
    }
    private val notificationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // A rendszer natív splash-e azonos színű, mint a Compose intró.
        // Így nincs fehér villanás a két animáció között.
        splash.setOnExitAnimationListener { provider ->
            provider.view.animate()
                .alpha(0f)
                .setDuration(85L)
                .withEndAction { provider.remove() }
                .start()
        }
        setSplashBars()
        refresh()

        setContent {
            var showIntro by androidx.compose.runtime.saveable.rememberSaveable {
                mutableStateOf(true)
            }

            val dark = isSystemInDarkTheme()
            LaunchedEffect(showIntro, dark) {
                if (!showIntro) setMainBars()
            }

            if (showIntro) {
                SplashExperience(
                    onFinished = { showIntro = false }
                )
            } else {
                HuApp(
                    state = state,
                    enableRole = ::requestRole,
                    setEnabled = {
                        SettingsStore.setEnabled(this, it)
                        refresh()
                        if (state.active && !state.notifyAllowed) askNotifications()
                    },
                    setContacts = {
                        if (!ContactChecker.hasPermission(this)) {
                            requestedContactSetting = it
                            contactLauncher.launch(Manifest.permission.READ_CONTACTS)
                        } else {
                            SettingsStore.setAllowContacts(this, it)
                            refresh()
                        }
                    },
                    requestContactsPermission = {
                        requestedContactSetting = null
                        contactLauncher.launch(Manifest.permission.READ_CONTACTS)
                    },
                    setCountryAllowed = { iso, allowed ->
                        SettingsStore.setCountryAllowed(this, iso, allowed)
                        refresh()
                    },
                    addNumber = { number, allowed ->
                        val saved = SettingsStore.addNumber(this, number, allowed)
                        if (saved) refresh()
                        saved
                    },
                    removeNumber = { number, allowed ->
                        SettingsStore.removeNumber(this, number, allowed)
                        refresh()
                    },
                    requestNotifications = ::askNotifications,
                    openPrivateBlocking = ::openPrivateBlocking,
                    clearCalls = { BlockedCallStore.clear(this); refresh() },
                    refresh = ::refresh
                )
            }
        }
    }

    private fun setSplashBars() {
        window.statusBarColor = android.graphics.Color.rgb(5, 23, 33)
        window.navigationBarColor = android.graphics.Color.rgb(5, 23, 33)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = 0
    }

    private fun setMainBars() {
        val dark = resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        @Suppress("DEPRECATION")
        window.statusBarColor = if (dark) android.graphics.Color.rgb(11, 22, 28)
                                else android.graphics.Color.rgb(244, 248, 247)
        @Suppress("DEPRECATION")
        window.navigationBarColor = if (dark) android.graphics.Color.rgb(20, 35, 43)
                                    else android.graphics.Color.WHITE
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        ProtectionNotification.sync(this)
        val roles = getSystemService(RoleManager::class.java)
        val available = roles.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)
        state = ScreenState(
            roleAvailable = available,
            roleHeld = available && roles.isRoleHeld(RoleManager.ROLE_CALL_SCREENING),
            enabled = SettingsStore.enabled(this),
            allowContacts = SettingsStore.allowContacts(this),
            contactsPermission = ContactChecker.hasPermission(this),
            allowedCountries = SettingsStore.allowedCountries(this),
            allowedNumbers = SettingsStore.allowedNumbers(this),
            blockedNumbers = SettingsStore.blockedNumbers(this),
            notifyAllowed = ProtectionNotification.canNotify(this),
            calls = BlockedCallStore.read(this)
        )
    }

    private fun requestRole() {
        val roles = getSystemService(RoleManager::class.java)
        if (roles.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            roleLauncher.launch(roles.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
        }
    }

    private fun askNotifications() {
        if (!state.active) return
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (!ProtectionNotification.canNotify(this)) {
            startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            )
        } else refresh()
    }

    // A rejtett hívás blokkolása a telefon saját beállításának feladata.
    private fun openPrivateBlocking() {
        try {
            startActivity(Intent("android.telecom.action.MANAGE_BLOCKED_NUMBERS"))
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(Intent(Intent.ACTION_DIAL))
            } catch (_: ActivityNotFoundException) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        } catch (_: SecurityException) {
            startActivity(Intent(Intent.ACTION_DIAL))
        }
    }
}
