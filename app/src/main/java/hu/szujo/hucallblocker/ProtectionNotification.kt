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
import android.graphics.Color
import android.os.Build
import android.util.Log

internal object ProtectionNotification {
    private const val TAG = "ProtectionNotification"
    private const val CHANNEL_ID = "protection_status_v2"
    private const val NOTIFICATION_ID = 3601

    fun isProtectionEnabled(context: Context): Boolean {
        val role = context.getSystemService(RoleManager::class.java)
        return SettingsStore.enabled(context) &&
            role.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
            role.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
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

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_status_shield)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setColor(Color.parseColor("#0AAE8A"))
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build()

        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.w(TAG, "Notification update failed", e)
        }
    }

    private fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            setSound(null, null)
            enableLights(false)
            enableVibration(false)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }
}
