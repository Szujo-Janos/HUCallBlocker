package hu.szujo.hucallblocker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

/**
 * Egyszerű, alacsony zavarású állapotértesítés.
 *
 * Nem foreground service, nem szükséges folyamatosan futó app.
 * Az értesítés csak az utolsó rendszerellenőrzés eredményét mutatja:
 * a felhasználó/rendszer eltávolíthatja, és gyártói UI elrejtheti az ikont.
 */
internal object ProtectionNotification {
    private const val TAG = "ProtectionNotification"
    private const val CHANNEL_ID = "protection_status_v1"
    private const val NOTIFICATION_ID = 3601

    fun isProtectionEnabled(context: Context): Boolean {
        val role = context.getSystemService(RoleManager::class.java)
        return SettingsStore.enabled(context) &&
            role.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
            role.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        ) return false

        val manager = context.getSystemService(NotificationManager::class.java)
        if (!manager.areNotificationsEnabled()) return false
        val channel = manager.getNotificationChannel(CHANNEL_ID)
        return channel == null || channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun sync(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (!isProtectionEnabled(context)) {
            manager.cancel(NOTIFICATION_ID)
            return
        }
        createChannel(context)
        if (!canNotify(context)) {
            manager.cancel(NOTIFICATION_ID)
            return
        }

        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(
            context, 0, openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_status_shield)
            .setContentTitle("HU Call Blocker · Védelem aktív")
            .setContentText("A külföldi számok automatikus szűrése be van kapcsolva.")
            .setContentIntent(pi)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setColor(android.graphics.Color.rgb(35, 214, 179))
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build()
        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Az értesítés nem engedélyezett", e)
        } catch (e: RuntimeException) {
            Log.w(TAG, "Az értesítést a rendszer elutasította", e)
        }
    }

    private fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID, "Védelmi állapot", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Az aktív hívásszűrést jelző pajzsikon."
            setSound(null, null)
            enableVibration(false)
            enableLights(false)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }
}
