package hu.szujo.hucallblocker

import android.content.Context

internal object SettingsStore {
    private const val FILE = "hucallblocker_preferences"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_ALLOW_CONTACTS = "allow_contacts"

    fun enabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ENABLED, value).apply()
    }

    fun allowContacts(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_ALLOW_CONTACTS, true)

    fun setAllowContacts(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ALLOW_CONTACTS, value).apply()
    }
}
