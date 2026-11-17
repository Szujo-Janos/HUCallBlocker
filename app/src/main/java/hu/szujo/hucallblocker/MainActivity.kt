package hu.szujo.hucallblocker

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.AlertDialog
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var heroCard: MaterialCardView
    private lateinit var heroIcon: ImageView
    private lateinit var statusChip: TextView
    private lateinit var statusTitle: TextView
    private lateinit var statusSubtitle: TextView
    private lateinit var roleButton: MaterialButton

    private lateinit var enabledSwitch: MaterialSwitch
    private lateinit var contactsSwitch: MaterialSwitch
    private lateinit var contactsHint: TextView

    private lateinit var iconStatus: TextView
    private lateinit var notificationButton: MaterialButton

    private lateinit var historyCount: TextView
    private lateinit var historyContainer: LinearLayout
    private lateinit var emptyLogs: TextView

    private var refreshing = false
    private var pulseAnimator: ObjectAnimator? = null

    companion object {
        private const val REQUEST_ROLE = 41
        private const val REQUEST_CONTACTS = 42
        private const val REQUEST_NOTIFICATIONS = 43
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        heroCard = findViewById(R.id.heroCard)
        heroIcon = findViewById(R.id.heroIcon)
        statusChip = findViewById(R.id.statusChip)
        statusTitle = findViewById(R.id.statusTitle)
        statusSubtitle = findViewById(R.id.statusSubtitle)
        roleButton = findViewById(R.id.roleButton)

        enabledSwitch = findViewById(R.id.enabledSwitch)
        contactsSwitch = findViewById(R.id.contactsSwitch)
        contactsHint = findViewById(R.id.contactsHint)

        iconStatus = findViewById(R.id.iconStatus)
        notificationButton = findViewById(R.id.notificationButton)

        historyCount = findViewById(R.id.historyCount)
        historyContainer = findViewById(R.id.historyContainer)
        emptyLogs = findViewById(R.id.emptyLogs)

        roleButton.setOnClickListener { requestScreeningRole() }
        notificationButton.setOnClickListener { askNotificationPermissionIfNeeded(true) }
        findViewById<MaterialButton>(R.id.clearLogsButton).setOnClickListener { confirmClearLogs() }

        enabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (refreshing) return@setOnCheckedChangeListener
            SettingsStore.setEnabled(this, isChecked)
            refreshStatus()
            if (isChecked && ProtectionNotification.isProtectionEnabled(this)) {
                askNotificationPermissionIfNeeded()
            }
        }

        contactsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (refreshing) return@setOnCheckedChangeListener
            if (!isChecked && !ContactChecker.hasPermission(this)) {
                refreshing = true
                contactsSwitch.isChecked = true
                refreshing = false
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CONTACTS)
            } else {
                SettingsStore.setAllowContacts(this, isChecked)
                refreshContactsHint()
            }
        }

        startPulse(heroIcon)
        refreshSwitches()
        refreshStatus()
        refreshHistory()
    }

    override fun onResume() {
        super.onResume()
        pulseAnimator?.resume()
        refreshSwitches()
        refreshStatus()
        refreshHistory()
    }

    override fun onPause() {
        pulseAnimator?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        pulseAnimator?.cancel()
        super.onDestroy()
    }

    private fun refreshSwitches() {
        refreshing = true
        enabledSwitch.isChecked = SettingsStore.enabled(this)
        contactsSwitch.isChecked = SettingsStore.allowContacts(this)
        refreshing = false
        refreshContactsHint()
    }

    private fun refreshContactsHint() {
        contactsHint.text = when {
            SettingsStore.allowContacts(this) && ContactChecker.hasPermission(this) ->
                getString(R.string.contacts_hint_allow_saved)
            SettingsStore.allowContacts(this) ->
                getString(R.string.contacts_hint_system_skip)
            ContactChecker.hasPermission(this) ->
                getString(R.string.contacts_hint_block_saved)
            else ->
                getString(R.string.contacts_hint_permission_needed)
        }
    }

    private fun roleManager(): RoleManager = getSystemService(RoleManager::class.java)

    private fun refreshStatus() {
        val roleManager = roleManager()
        val roleAvailable = roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)
        val roleHeld = roleAvailable && roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        val active = roleHeld && SettingsStore.enabled(this)

        ProtectionNotification.sync(this)

        val chipText: String
        val chipBg: Int
        val titleText: String
        val subtitleText: String
        val heroBg: Int

        when {
            !roleAvailable -> {
                chipText = getString(R.string.status_unsupported)
                chipBg = R.color.status_red
                titleText = getString(R.string.hero_title_unsupported)
                subtitleText = getString(R.string.hero_subtitle_unsupported)
                heroBg = R.color.hero_red_soft
            }
            !roleHeld -> {
                chipText = getString(R.string.status_permission)
                chipBg = R.color.status_red
                titleText = getString(R.string.hero_title_permission)
                subtitleText = getString(R.string.hero_subtitle_permission)
                heroBg = R.color.hero_red_soft
            }
            !active -> {
                chipText = getString(R.string.status_paused)
                chipBg = R.color.status_orange
                titleText = getString(R.string.hero_title_paused)
                subtitleText = getString(R.string.hero_subtitle_paused)
                heroBg = R.color.hero_orange_soft
            }
            else -> {
                chipText = getString(R.string.status_active)
                chipBg = R.color.status_green
                titleText = getString(R.string.hero_title_active)
                subtitleText = getString(R.string.hero_subtitle_active)
                heroBg = R.color.hero_green_soft
            }
        }

        statusChip.text = chipText
        statusChip.backgroundTintList = ContextCompat.getColorStateList(this, chipBg)
        statusTitle.text = titleText
        statusSubtitle.text = subtitleText
        heroCard.setCardBackgroundColor(ContextCompat.getColor(this, heroBg))
        roleButton.visibility = if (roleAvailable && !roleHeld) View.VISIBLE else View.GONE

        val canNotify = ProtectionNotification.canNotify(this)
        iconStatus.text = when {
            !active -> getString(R.string.icon_status_inactive)
            canNotify -> getString(R.string.icon_status_enabled)
            else -> getString(R.string.icon_status_permission_needed)
        }
        notificationButton.visibility = if (active && !canNotify) View.VISIBLE else View.GONE
    }

    @Suppress("DEPRECATION")
    private fun requestScreeningRole() {
        val manager = roleManager()
        if (manager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            startActivityForResult(
                manager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING),
                REQUEST_ROLE
            )
        } else {
            AlertDialog.Builder(this)
                .setMessage(R.string.role_not_supported)
                .setPositiveButton(R.string.ok, null)
                .show()
        }
    }

    private fun askNotificationPermissionIfNeeded(openSettingsIfDenied: Boolean = false) {
        if (!ProtectionNotification.isProtectionEnabled(this)) return

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS)
            return
        }

        if (!ProtectionNotification.canNotify(this) && openSettingsIfDenied) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            startActivity(intent)
            return
        }

        ProtectionNotification.sync(this)
        refreshStatus()
    }

    @Deprecated("Android role request")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ROLE) {
            refreshStatus()
            if (ProtectionNotification.isProtectionEnabled(this)) {
                askNotificationPermissionIfNeeded()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CONTACTS -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                    SettingsStore.setAllowContacts(this, false)
                }
                refreshSwitches()
            }
            REQUEST_NOTIFICATIONS -> {
                ProtectionNotification.sync(this)
                refreshStatus()
            }
        }
    }

    private fun refreshHistory() {
        historyContainer.removeAllViews()
        val entries = BlockedCallStore.read(this)
        historyCount.text = entries.size.toString()
        emptyLogs.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        if (entries.isEmpty()) return

        val format = SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale("hu", "HU"))
        entries.forEachIndexed { index, entry ->
            val row = layoutInflater.inflate(R.layout.item_log_entry, historyContainer, false)
            row.findViewById<TextView>(R.id.logNumber).text = entry.number
            row.findViewById<TextView>(R.id.logDate).text = format.format(Date(entry.timestamp))
            if (index == entries.lastIndex) {
                row.findViewById<View>(R.id.logDivider).visibility = View.GONE
            }
            historyContainer.addView(row)
        }
    }

    private fun confirmClearLogs() {
        AlertDialog.Builder(this)
            .setTitle(R.string.clear_logs_title)
            .setMessage(R.string.clear_logs_message)
            .setPositiveButton(R.string.clear) { _, _ ->
                BlockedCallStore.clear(this)
                refreshHistory()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun startPulse(view: View) {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.06f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.06f, 1f)
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.9f, 1f, 0.9f)
        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, alpha).apply {
            duration = 2200
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
}
