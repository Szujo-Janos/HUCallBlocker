package hu.szujo.hucallblocker

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Csak helyi napló, legfeljebb 100 eseménnyel. */
internal object BlockedCallStore {
    private const val FILE = "blocked_calls"
    private const val KEY = "entries"
    private const val MAX_ENTRIES = 100
    data class Entry(val number: String, val timestamp: Long)

    @Synchronized
    fun add(context: Context, number: String) {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val previous = runCatching { JSONArray(prefs.getString(KEY, "[]")) }
            .getOrDefault(JSONArray())
        val next = JSONArray()
        next.put(JSONObject().put("number", number).put("time", System.currentTimeMillis()))
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
            if (number.isBlank() || time <= 0L) null else Entry(number, time)
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
}
