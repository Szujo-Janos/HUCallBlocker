package hu.szujo.hucallblocker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract

internal object ContactChecker {
    fun hasPermission(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    /** Névjegyolvasási hiba esetén kivételt dob: a szűrő ilyenkor átengedi a hívást. */
    fun isSaved(context: Context, number: String): Boolean {
        if (!hasPermission(context)) return false
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)
        )
        return context.contentResolver.query(
            uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null
        )?.use { it.moveToFirst() } ?: false
    }
}
