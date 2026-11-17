package hu.szujo.hucallblocker

import android.Manifest
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

internal data class ScreenState(
    val roleAvailable: Boolean = true,
    val roleHeld: Boolean = false,
    val enabled: Boolean = true,
    val allowContacts: Boolean = true,
    val contactsPermission: Boolean = false,
    val notifyAllowed: Boolean = false,
    val calls: List<BlockedCallStore.Entry> = emptyList()
) {
    val active: Boolean get() = roleAvailable && roleHeld && enabled
}

class MainActivity : ComponentActivity() {
    private var state by mutableStateOf(ScreenState())

    private val roleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refresh()
        if (state.active && !state.notifyAllowed) askNotifications()
    }
    private val contactLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) SettingsStore.setAllowContacts(this, false)
        refresh()
    }
    private val notificationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setOnExitAnimationListener { provider ->
            provider.view.animate().alpha(0f).scaleX(1.04f).scaleY(1.04f)
                .setDuration(250).withEndAction { provider.remove() }.start()
        }
        window.statusBarColor = android.graphics.Color.rgb(245, 248, 252)
        window.navigationBarColor = android.graphics.Color.WHITE
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        refresh()
        setContent {
            HuApp(
                state = state,
                enableRole = ::requestRole,
                setEnabled = {
                    SettingsStore.setEnabled(this, it)
                    refresh()
                    if (state.active && !state.notifyAllowed) askNotifications()
                },
                setContacts = {
                    if (!it && !ContactChecker.hasPermission(this)) {
                        contactLauncher.launch(Manifest.permission.READ_CONTACTS)
                    } else {
                        SettingsStore.setAllowContacts(this, it)
                        refresh()
                    }
                },
                requestNotifications = ::askNotifications,
                openPrivateBlocking = ::openPrivateBlocking,
                clearCalls = { BlockedCallStore.clear(this); refresh() },
                refresh = ::refresh
            )
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
