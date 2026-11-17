package hu.szujo.hucallblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/** Újraindítás után, az Android BOOT_COMPLETED jelzésére visszaállítja az állapotikont. */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        try {
            ProtectionNotification.sync(context)
        } catch (e: RuntimeException) {
            Log.w("HUCallBlocker", "Induláskori értesítés sikertelen", e)
        }
    }
}
