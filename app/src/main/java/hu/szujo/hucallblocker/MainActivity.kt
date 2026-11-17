package hu.szujo.hucallblocker

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * HU Call Blocker – áttekinthető natív Android Views kezelőfelület.
 * Nem kell Compose vagy külső UI-függőség.
 */
class MainActivity : Activity() {
    private val bg = Color.rgb(9, 19, 30)
    private val surface = Color.rgb(20, 36, 50)
    private val surfaceRaised = Color.rgb(25, 45, 58)
    private val teal = Color.rgb(35, 214, 179)
    private val red = Color.rgb(255, 137, 142)
    private val amber = Color.rgb(255, 193, 110)
    private val white = Color.rgb(244, 249, 250)
    private val muted = Color.rgb(154, 173, 186)
    private val stroke = Color.rgb(44, 68, 83)

    private lateinit var page: LinearLayout
    private lateinit var statusDot: View
    private lateinit var statusPill: TextView
    private lateinit var statusTitle: TextView
    private lateinit var statusDetail: TextView
    private lateinit var roleButton: Button
    private lateinit var enabledSwitch: Switch
    private lateinit var contactsSwitch: Switch
    private lateinit var contactHint: TextView
    private lateinit var iconStatus: TextView
    private lateinit var notificationButton: Button
    private lateinit var logCount: TextView
    private lateinit var history: LinearLayout
    private var refreshing = false

    companion object {
        private const val REQUEST_ROLE = 41
        private const val REQUEST_CONTACTS = 42
        private const val REQUEST_NOTIFICATIONS = 43
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = 0

        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
            fillViewportCompat()
            clipToPadding = false
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
            scroll.setPadding(dp(20), top + dp(20), dp(20), bottom + dp(24))
            insets
        }
        scroll.requestApplyInsets()

        // Márkanév + ikon
        val brand = horizontal(Gravity.CENTER_VERTICAL)
        val logoBackground = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            background = round(surfaceRaised, 16, stroke)
        }
        logoBackground.addView(ImageView(this).apply {
            setImageResource(R.drawable.ic_shield)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }, LinearLayout.LayoutParams(dp(38), dp(38)))
        brand.addView(logoBackground, LinearLayout.LayoutParams(dp(60), dp(60)))
        val titles = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), 0, 0, 0)
        }
        add(titles, label("HU CALL BLOCKER", 21f, white, true))
        add(titles, label("Intelligens hívásvédelem", 13f, muted), 4)
        brand.addView(titles, LinearLayout.LayoutParams(0, -2, 1f))
        add(page, brand)

        add(page, label("A nyugalom a vonal másik végén kezdődik.", 13f, muted), 15)

        // Állapot: nagy, jól látható információ és egyetlen fő művelet.
        val hero = card(true)
        val statusRow = horizontal(Gravity.CENTER_VERTICAL)
        statusDot = View(this).apply { background = circle(teal) }
        statusRow.addView(statusDot, LinearLayout.LayoutParams(dp(9), dp(9)))
        statusPill = label("", 12f, teal, true).apply { setPadding(dp(9), 0, 0, 0) }
        statusRow.addView(statusPill)
        add(hero, statusRow)

        statusTitle = label("", 26f, white, true)
        add(hero, statusTitle, 18)
        statusDetail = label("", 14f, muted)
        add(hero, statusDetail, 8)

        roleButton = button("Hívásszűrés engedélyezése", true)
        add(hero, roleButton, 20, dp(50))
        roleButton.setOnClickListener { requestScreeningRole() }

        val protectionTags = horizontal(Gravity.CENTER_VERTICAL)
        addTag(protectionTags, "+36 engedélyezve")
        addTag(protectionTags, "Offline működés")
        add(hero, protectionTags, 19)

        section("VÉDELMI BEÁLLÍTÁSOK")
        val filters = card()
        enabledSwitch = setting(
            filters,
            "Külföldi számok tiltása",
            "A nem magyar nemzetközi számokat automatikusan elutasítja."
        )
        add(filters, divider(), 17, dp(1))
        contactsSwitch = setting(
            filters,
            "Mentett névjegyek átengedése",
            "Az ismerős külföldi számok kivételt kapnak."
        )
        contactHint = label("", 12f, muted)
        add(filters, contactHint, 10)

        enabledSwitch.setOnCheckedChangeListener { _, enabled ->
            if (refreshing) return@setOnCheckedChangeListener
            SettingsStore.setEnabled(this, enabled)
            refreshStatus()
            if (enabled && ProtectionNotification.isProtectionEnabled(this)) {
                askNotificationPermissionIfNeeded()
            }
        }
        contactsSwitch.setOnCheckedChangeListener { _, allow ->
            if (refreshing) return@setOnCheckedChangeListener
            if (!allow && !ContactChecker.hasPermission(this)) {
                refreshing = true
                contactsSwitch.isChecked = true
                refreshing = false
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CONTACTS)
            } else {
                SettingsStore.setAllowContacts(this, allow)
                refreshContactHint()
            }
        }

        section("ÁLLAPOTSOR JELZÉS")
        val notificationCard = card()
        val iconRow = horizontal(Gravity.CENTER_VERTICAL)
        val iconTile = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            background = round(Color.rgb(27, 67, 66), 12)
        }
        iconTile.addView(ImageView(this).apply {
            setImageResource(R.drawable.ic_status_shield)
            setColorFilter(teal)
        }, LinearLayout.LayoutParams(dp(25), dp(25)))
        iconRow.addView(iconTile, LinearLayout.LayoutParams(dp(47), dp(47)))

        val iconTextCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), 0, 0, 0)
        }
        add(iconTextCol, label("Védelmi pajzsikon", 15f, white, true))
        iconStatus = label("", 12f, muted)
        add(iconTextCol, iconStatus, 5)
        iconRow.addView(iconTextCol, LinearLayout.LayoutParams(0, -2, 1f))
        add(notificationCard, iconRow)

        notificationButton = button("Értesítések engedélyezése")
        add(notificationCard, notificationButton, 16, dp(44))
        notificationButton.setOnClickListener { askNotificationPermissionIfNeeded(true) }
        add(notificationCard, label(
            "Az ikon csak rendszerengedéllyel jelenhet meg. A blokkolás az ikon nélkül is működik.",
            12f, muted
        ), 13)

        section("BLOKKOLÁSI NAPLÓ")
        val logs = card()
        val logsTop = horizontal(Gravity.CENTER_VERTICAL)
        val logsText = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        add(logsText, label("Elutasított hívások", 16f, white, true))
        add(logsText, label("A legutóbbi 100 esemény", 12f, muted), 5)
        logsTop.addView(logsText, LinearLayout.LayoutParams(0, -2, 1f))
        logCount = label("0", 29f, teal, true)
        logsTop.addView(logCount)
        add(logs, logsTop)
        add(logs, divider(), 17, dp(1))
        history = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        add(logs, history, 15)

        val clear = button("Napló ürítése")
        clear.background = round(Color.rgb(37, 61, 75), 11)
        clear.setTextColor(white)
        add(logs, clear, 17, dp(43))
        clear.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Blokkolási napló ürítése")
                .setMessage("Az alkalmazás naplóját töröljük, a telefon híváslistáját nem.")
                .setPositiveButton("Törlés") { _, _ ->
                    BlockedCallStore.clear(this)
                    refreshHistory()
                }
                .setNegativeButton("Mégse", null)
                .show()
        }

        val footer = label(
            "A szűrés az országhívószámot ellenőrzi, nem a hívó tartózkodási helyét. " +
                "A rejtett, illetve nemzetközi előhívó nélküli számokat biztonságból átengedi.",
            12f, muted
        )
        add(page, footer, 22)
        add(page, label("HU CALL BLOCKER  •  v1.0.3", 11f, stroke, true), 18)

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
                "A külföldi névjegyek átengedése engedélyezve."
            SettingsStore.allowContacts(this) ->
                "Az Android a mentett számokat engedély nélkül is kihagyja a szűrésből."
            ContactChecker.hasPermission(this) ->
                "A mentett külföldi számok is blokkolhatók."
            else ->
                "A mentett számok tiltásához névjegyhozzáférés szükséges."
        }
    }

    private fun roleManager(): RoleManager = getSystemService(RoleManager::class.java)

    private fun refreshStatus() {
        val role = roleManager()
        val available = role.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)
        val held = available && role.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        val active = held && SettingsStore.enabled(this)
        ProtectionNotification.sync(this)

        val caption = when {
            !available -> "NEM TÁMOGATOTT"
            !held -> "BEÁLLÍTÁS SZÜKSÉGES"
            !active -> "SZÜNETELTETVE"
            else -> "VÉDELEM AKTÍV"
        }
        val accent = when {
            active -> teal
            !held || !available -> red
            else -> amber
        }
        statusPill.text = caption
        statusPill.setTextColor(accent)
        statusDot.background = circle(accent)

        statusTitle.text = when {
            !available -> "Nem elérhető"
            !held -> "Kapcsold be a védelmet"
            !active -> "Védelem szünetel"
            else -> "Biztonságban vagy"
        }
        statusDetail.text = when {
            !available -> "Ez a telefon nem támogatja az Android hívásszűrő szerepkörét."
            !held -> "Engedélyezd az alkalmazást a rendszerben a hívások szűréséhez."
            !active -> "A hívásszűrés jogosultsága megvan, de a tiltás ki van kapcsolva."
            else -> "A külföldi előhívószámú hívások automatikus szűrése bekapcsolva."
        }
        roleButton.visibility = if (available && !held) View.VISIBLE else View.GONE

        val canNotify = ProtectionNotification.canNotify(this)
        iconStatus.text = when {
            !active -> "A védelem bekapcsolásakor jelenik meg."
            canNotify -> "Az állapotjelzés engedélyezve."
            else -> "Az értesítési engedély hiányzik."
        }
        iconStatus.setTextColor(if (active && canNotify) teal else muted)
        notificationButton.visibility = if (active && !canNotify) View.VISIBLE else View.GONE
    }

    @Suppress("DEPRECATION")
    private fun requestScreeningRole() {
        val role = roleManager()
        if (role.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            startActivityForResult(
                role.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING),
                REQUEST_ROLE
            )
        } else {
            AlertDialog.Builder(this)
                .setMessage("Ezen a telefonon nem elérhető az Android hívásszűrő szerepköre.")
                .setPositiveButton("Rendben", null)
                .show()
        }
    }

    private fun askNotificationPermissionIfNeeded(openSettingsIfDenied: Boolean = false) {
        if (!ProtectionNotification.isProtectionEnabled(this)) return
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            if (openSettingsIfDenied &&
                !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
            ) {
                // Új felhasználónál is megjelenítjük a rendszer saját engedélykérését.
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS
                )
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS
                )
            }
        } else if (!ProtectionNotification.canNotify(this)) {
            // Rendszerszinten tiltott értesítések / csatorna.
            val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
            }
            startActivity(intent)
        } else {
            ProtectionNotification.sync(this)
            refreshStatus()
        }
    }

    @Deprecated("Android 10+ rendszer-szerepkör kérés")
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
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CONTACTS) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                SettingsStore.setAllowContacts(this, false)
            }
            refreshSwitches()
        } else if (requestCode == REQUEST_NOTIFICATIONS) {
            ProtectionNotification.sync(this)
            refreshStatus()
        }
    }

    private fun refreshHistory() {
        history.removeAllViews()
        val entries = BlockedCallStore.read(this)
        logCount.text = entries.size.toString()
        if (entries.isEmpty()) {
            add(history, label("Még nincs blokkolt hívás.", 14f, muted))
            return
        }
        val dateFormat = SimpleDateFormat("yyyy.MM.dd.  HH:mm", Locale("hu", "HU"))
        entries.forEachIndexed { index, event ->
            if (index > 0) add(history, divider(), 12, dp(1))
            val record = horizontal(Gravity.CENTER_VERTICAL)
            val numberCol = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            add(numberCol, label(event.number, 15f, white, true))
            add(numberCol, label(dateFormat.format(Date(event.timestamp)), 12f, muted), 4)
            record.addView(numberCol, LinearLayout.LayoutParams(0, -2, 1f))
            record.addView(label("TILTVA", 10f, red, true))
            add(history, record, if (index == 0) 0 else 12)
        }
    }

    private fun setting(parent: LinearLayout, title: String, description: String): Switch {
        val row = horizontal(Gravity.CENTER_VERTICAL)
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        add(texts, label(title, 15f, white, true))
        add(texts, label(description, 12f, muted), 7)
        row.addView(texts, LinearLayout.LayoutParams(0, -2, 1f))
        val control = Switch(this).apply {
            showText = false
            thumbTintList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(teal, muted)
            )
            trackTintList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(Color.rgb(23, 110, 98), Color.rgb(72, 87, 98))
            )
            setPadding(dp(9), 0, 0, 0)
            contentDescription = title
        }
        row.addView(control, LinearLayout.LayoutParams(-2, dp(52)))
        add(parent, row)
        return control
    }

    private fun section(title: String) {
        add(page, label(title, 11f, muted, true), 27)
        add(page, View(this), 8, dp(1))
    }

    private fun card(hero: Boolean = false): LinearLayout {
        val v = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(if (hero) 23 else 18), dp(18), dp(18))
            background = if (hero) {
                GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(Color.rgb(23, 62, 66), Color.rgb(22, 40, 57))
                ).apply {
                    cornerRadius = dp(21).toFloat()
                    setStroke(dp(1), Color.rgb(48, 105, 106))
                }
            } else round(surface, 19, stroke)
        }
        add(page, v)
        return v
    }

    private fun addTag(parent: LinearLayout, text: String) {
        val tag = label(text, 11f, teal, true).apply {
            setPadding(dp(10), dp(7), dp(10), dp(7))
            background = round(Color.rgb(18, 58, 59), 9)
        }
        val lp = LinearLayout.LayoutParams(-2, -2)
        lp.marginEnd = dp(7)
        parent.addView(tag, lp)
    }

    private fun button(value: String, primary: Boolean = false): Button = Button(this).apply {
        text = value
        isAllCaps = false
        textSize = 14f
        setTextColor(if (primary) bg else white)
        setTypeface(typeface, Typeface.BOLD)
        background = round(if (primary) teal else surfaceRaised, 12)
        minHeight = 0
        minimumHeight = 0
        elevation = 0f
    }

    private fun label(textValue: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = textValue
            textSize = size
            setTextColor(color)
            includeFontPadding = true
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    private fun horizontal(gravityValue: Int): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = gravityValue
        }

    private fun divider(): View = View(this).apply { setBackgroundColor(stroke) }

    private fun circle(color: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }

    private fun round(color: Int, radius: Int, border: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (border != null) setStroke(dp(1), border)
        }

    private fun ScrollView.fillViewportCompat() {
        isFillViewport = true
    }

    private fun add(parent: LinearLayout, view: View, top: Int = 0, height: Int = -2) {
        val lp = LinearLayout.LayoutParams(-1, height)
        lp.topMargin = dp(top)
        parent.addView(view, lp)
    }

    private fun dp(n: Int): Int = (resources.displayMetrics.density * n + 0.5f).toInt()
}
