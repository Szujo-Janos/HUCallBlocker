package hu.szujo.hucallblocker

import android.content.Context

internal object SettingsStore {
    private const val FILE = "hucallblocker_preferences"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_ALLOW_CONTACTS = "allow_contacts"
    private const val KEY_COUNTRIES = "allowed_countries"
    private const val KEY_ALLOW_NUMBERS = "allowed_numbers"
    private const val KEY_BLOCK_NUMBERS = "blocked_numbers"
    private const val MAX_RULES = 250
    private const val KEY_THEME_MODE = "theme_mode"

    fun themeMode(context: Context): ThemeMode =
        runCatching { ThemeMode.valueOf(prefs(context).getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM") }
            .getOrDefault(ThemeMode.SYSTEM)

    fun setThemeMode(context: Context, mode: ThemeMode) {
        prefs(context).edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun enabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, true)
    fun setEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    /** A korábban mentett beállítások továbbra is érvényesek. */
    fun allowContacts(context: Context): Boolean = prefs(context).getBoolean(KEY_ALLOW_CONTACTS, true)
    fun setAllowContacts(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_ALLOW_CONTACTS, value).apply()
    }

    fun allowedCountries(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_COUNTRIES, emptySet())?.toSet() ?: emptySet()

    fun allowedNumbers(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_ALLOW_NUMBERS, emptySet())?.toSet() ?: emptySet()

    fun blockedNumbers(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_BLOCK_NUMBERS, emptySet())?.toSet() ?: emptySet()

    @Synchronized
    fun setCountryAllowed(context: Context, iso: String, allowed: Boolean) {
        if (!CountryDialing.isSupported(iso)) return
        val countries = allowedCountries(context).toMutableSet()
        if (allowed) countries.add(iso) else countries.remove(iso)
        prefs(context).edit().putStringSet(KEY_COUNTRIES, countries).apply()
    }

    /** Az új szabály felülírja ugyanannak a számnak az ellenkező listáját. */
    @Synchronized
    fun addNumber(context: Context, raw: String, allowed: Boolean): Boolean {
        val number = NumberRules.normalize(raw) ?: return false
        val allow = allowedNumbers(context).toMutableSet()
        val block = blockedNumbers(context).toMutableSet()
        val target = if (allowed) allow else block
        if (number !in target && target.size >= MAX_RULES) return false
        target.add(number)
        if (allowed) block.remove(number) else allow.remove(number)
        prefs(context).edit()
            .putStringSet(KEY_ALLOW_NUMBERS, allow)
            .putStringSet(KEY_BLOCK_NUMBERS, block)
            .apply()
        return true
    }

    @Synchronized
    fun removeNumber(context: Context, raw: String, allowed: Boolean) {
        val number = NumberRules.normalize(raw) ?: return
        val key = if (allowed) KEY_ALLOW_NUMBERS else KEY_BLOCK_NUMBERS
        val numbers = (if (allowed) allowedNumbers(context) else blockedNumbers(context)).toMutableSet()
        if (numbers.remove(number)) prefs(context).edit().putStringSet(key, numbers).apply()
    }
}
