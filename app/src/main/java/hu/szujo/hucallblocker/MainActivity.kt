package hu.szujo.hucallblocker

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Nincs Compose, AndroidX vagy külső UI-függőség. */
class MainActivity : Activity() {
    private val bg = 0xFF091622.toInt()
    private val cardColor = 0xFF142635.toInt()
    private val teal = 0xFF2DD4BF.toInt()
    private val red = 0xFFFF858C.toInt()
    private val white = 0xFFF3F7FA.toInt()
    private val muted = 0xFF9FAFBD.toInt()

    private lateinit var page: LinearLayout
    private lateinit var statusTitle: TextView
    private lateinit var statusDetail: TextView
    private lateinit var roleButton: Button
    private lateinit var enabledSwitch: Switch
    private lateinit var contactsSwitch: Switch
    private lateinit var contactHint: TextView
    private lateinit var history: LinearLayout
    private var refreshing = false

    companion object {
        private const val ROLE_REQUEST = 41
        private const val CONTACTS_REQUEST = 42
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg

        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
            clipToPadding = false
            isFillViewport = true
        }
        page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(page)
        setContentView(scroll)
        scroll.setOnApplyWindowInsetsListener { _, insets ->
            val top: Int
            val bottom: Int
            if (Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars())
                top = bars.top
                bottom = bars.bottom
            } else {
                @Suppress("DEPRECATION")
                top = insets.systemWindowInsetTop
                @Suppress("DEPRECATION")
                bottom = insets.systemWindowInsetBottom
            }
            scroll.setPadding(dp(18), dp(18) + top, dp(18), dp(24) + bottom)
            insets
        }
        scroll.requestApplyInsets()

        add(page, label("HU CALL BLOCKER", 12f, teal, true), 0)
        add(page, label("Külföldi hívások kiszűrése", 23f, white, true), 8)
        add(page, label("Offline védelem  •  Android 10+", 13f, muted), 5)

        section("ÁLLAPOT")
        val statusCard = card()
        statusTitle = label("", 19f, white, true)
        add(statusCard, statusTitle)
        statusDetail = label("", 13f, muted)
        add(statusCard, statusDetail, 7)
        roleButton = button("Hívásszűrés engedélyezése")
        add(statusCard, roleButton, 16, dp(48))
        roleButton.setOnClickListener { requestScreeningRole() }

        section("SZŰRÉSI SZABÁLYOK")
        val filtersCard = card()
        enabledSwitch = switch("Külföldi számok automatikus tiltása")
        add(filtersCard, enabledSwitch)
        add(filtersCard, label("A +36 és 0036 kezdetű számok átjöhetnek. A más ország-előhívóval érkezők elutasításra kerülnek.", 13f, muted), 7)
        add(filtersCard, divider(), 16, dp(1))
        contactsSwitch = switch("Mentett névjegyek átengedése")
        add(filtersCard, contactsSwitch, 17)
        contactHint = label("", 13f, muted)
        add(filtersCard, contactHint, 7)

        enabledSwitch.setOnCheckedChangeListener { _, enabled ->
            if (refreshing) return@setOnCheckedChangeListener
            SettingsStore.setEnabled(this, enabled)
            refreshStatus()
        }
        contactsSwitch.setOnCheckedChangeListener { _, allowed ->
            if (refreshing) return@setOnCheckedChangeListener
            if (!allowed && !ContactChecker.hasPermission(this)) {
                refreshing = true
                contactsSwitch.isChecked = true
                refreshing = false
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), CONTACTS_REQUEST)
            } else {
                SettingsStore.setAllowContacts(this, allowed)
                refreshContactHint()
            }
        }

        section("BLOKKOLÁSI NAPLÓ")
        val logCard = card()
        history = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        add(logCard, history)
        add(logCard, button("Napló törlése").apply {
            setTextColor(white)
            background = rounded(0xFF29404E.toInt(), 12)
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Napló törlése")
                    .setMessage("Az alkalmazás saját naplóját töröljük. A telefon rendszerhíváslistája nem változik.")
                    .setPositiveButton("Törlés") { _, _ ->
                        BlockedCallStore.clear(this@MainActivity)
                        refreshHistory()
                    }
                    .setNegativeButton("Mégse", null)
                    .show()
            }
        }, 16, dp(44))

        add(page, label("A szűrés az előhívószám alapján működik, nem a hívó valódi tartózkodási helye alapján. A rejtett számok és az internetes hívások nem szűrhetők ezzel a módszerrel.", 12f, muted), 20)
        refreshSwitches()
        refreshStatus()
        refreshHistory()
    }

    override fun onResume() {
        super.onResume()
        if (::statusTitle.isInitialized) {
            refreshSwitches()
            refreshStatus()
            refreshHistory()
        }
    }

    private fun refreshSwitches() {
        refreshing = true
        enabledSwitch.isChecked = SettingsStore.enabled(this)
        contactsSwitch.isChecked = SettingsStore.allowContacts(this)
        refreshing = false
        refreshContactHint()
    }

    private fun refreshContactHint() {
        contactHint.text = when {
            SettingsStore.allowContacts(this) && ContactChecker.hasPermission(this) ->
                "A mentett külföldi névjegyeket az alkalmazás átengedi."
            SettingsStore.allowContacts(this) ->
                "Névjegyengedély nélkül az Android eleve kihagyja a mentett számokat a szűrésből."
            ContactChecker.hasPermission(this) ->
                "A mentett külföldi számokra is érvényes a blokkolás."
            else ->
                "A mentett számok tiltásához névjegyengedély szükséges."
        }
    }

    private fun roleManager(): RoleManager = getSystemService(RoleManager::class.java)

    private fun refreshStatus() {
        val role = roleManager()
        val available = role.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)
        val held = available && role.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        val enabled = SettingsStore.enabled(this)
        statusTitle.text = when {
            !available -> "A hívásszűrés nem támogatott"
            !held -> "Engedélyezés szükséges"
            !enabled -> "Szűrés szüneteltetve"
            else -> "Védelem aktív"
        }
        statusTitle.setTextColor(if (held && enabled) teal else if (!available || !held) red else white)
        statusDetail.text = when {
            !available -> "A telefon nem kínálja a hivatalos Android hívásszűrő szerepkört."
            !held -> "Engedélyezd a HU Call Blockert a telefon hívásszűrőjeként."
            !enabled -> "A jogosultság megvan, de a blokkolást kikapcsoltad."
            else -> "A külföldi előhívószámú hívások blokkolása bekapcsolva."
        }
        roleButton.visibility = if (!available || held) View.GONE else View.VISIBLE
    }

    @Suppress("DEPRECATION")
    private fun requestScreeningRole() {
        val manager = roleManager()
        if (manager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            startActivityForResult(
                manager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING),
                ROLE_REQUEST
            )
        } else {
            AlertDialog.Builder(this)
                .setMessage("A készülék nem támogatja ezt a hívásszűrő szerepkört.")
                .setPositiveButton("Rendben", null)
                .show()
        }
    }

    @Deprecated("API 29+ szerepkör-kérelem")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ROLE_REQUEST) refreshStatus()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_REQUEST) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                SettingsStore.setAllowContacts(this, false)
            } else {
                AlertDialog.Builder(this)
                    .setMessage("Névjegyengedély nélkül a mentett számokat az Android nem adja át a hívásszűrőnek.")
                    .setPositiveButton("Rendben", null).show()
            }
            refreshSwitches()
        }
    }

    private fun refreshHistory() {
        history.removeAllViews()
        val records = BlockedCallStore.read(this)
        if (records.isEmpty()) {
            add(history, label("Még nincs elutasított hívás.", 14f, muted))
            return
        }
        val format = SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale("hu", "HU"))
        for ((index, entry) in records.withIndex()) {
            if (index > 0) add(history, divider(), 10, dp(1))
            add(history, label(entry.number, 15f, white, true), if (index == 0) 0 else 10)
            add(history, label(format.format(Date(entry.timestamp)), 12f, muted), 3)
        }
    }

    private fun card(): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(17), dp(17), dp(17), dp(17))
            background = rounded(cardColor, 17, 0xFF244053.toInt())
        }
        add(page, container)
        return container
    }

    private fun section(title: String) {
        add(page, label(title, 12f, muted, true), 25)
        add(page, View(this), 6, dp(1))
    }

    private fun label(value: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    private fun switch(value: String): Switch = Switch(this).apply {
        text = value
        textSize = 15f
        setTextColor(white)
        showText = false
    }

    private fun button(title: String): Button = Button(this).apply {
        text = title
        isAllCaps = false
        textSize = 14f
        setTextColor(bg)
        setTypeface(typeface, Typeface.BOLD)
        background = rounded(teal, 12)
    }

    private fun divider(): View = View(this).apply {
        setBackgroundColor(0xFF304758.toInt())
    }

    private fun rounded(color: Int, radius: Int, border: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (border != null) setStroke(dp(1), border)
        }

    private fun add(parent: LinearLayout, view: View, top: Int = 0, height: Int = LinearLayout.LayoutParams.WRAP_CONTENT) {
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
        params.topMargin = dp(top)
        parent.addView(view, params)
    }

    private fun dp(value: Int): Int = (resources.displayMetrics.density * value + 0.5f).toInt()
}
