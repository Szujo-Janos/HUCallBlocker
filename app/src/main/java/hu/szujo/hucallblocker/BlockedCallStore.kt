package hu.szujo.hucallblocker

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Helyben tárolt napló, legfeljebb 100 bejegyzéssel. Régi rekordok is olvashatók. */
internal object BlockedCallStore {
    private const val FILE = "blocked_calls"
    private const val KEY = "entries"
    private const val MAX_ENTRIES = 100

    data class Entry(
        val number: String,
        val timestamp: Long,
        val countryIso: String? = null,
        val reason: String = "Külföldi szám"
    )

    @Synchronized
    fun add(context: Context, number: String, countryIso: String?, reason: String) {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val previous = runCatching { JSONArray(prefs.getString(KEY, "[]")) }
            .getOrDefault(JSONArray())
        val item = JSONObject()
            .put("number", number)
            .put("time", System.currentTimeMillis())
            .put("reason", reason)
        if (countryIso != null) item.put("country", countryIso)
        val next = JSONArray().put(item)
        for (i in 0 until minOf(previous.length(), MAX_ENTRIES - 1)) {
            previous.optJSONObject(i)?.let { next.put(it) }
        }
        prefs.edit().putString(KEY, next.toString()).apply()
    }

    fun read(context: Context): List<Entry> {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val records = runCatching { JSONArray(prefs.getString(KEY, "[]")) }
            .getOrDefault(JSONArray())
        return (0 until records.length()).mapNotNull { i ->
            val obj = records.optJSONObject(i) ?: return@mapNotNull null
            val number = obj.optString("number")
            val time = obj.optLong("time")
            if (number.isBlank() || time <= 0L) null else Entry(
                number = number,
                timestamp = time,
                countryIso = obj.optString("country").takeIf { it.isNotBlank() },
                reason = obj.optString("reason").takeIf { it.isNotBlank() } ?: "Külföldi szám"
            )
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
}
