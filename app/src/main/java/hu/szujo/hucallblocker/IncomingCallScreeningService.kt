package hu.szujo.hucallblocker

import android.telecom.Call
import android.telecom.CallScreeningService

/**
 * A rendszer a ROLE_CALL_SCREENING szerepkör jóváhagyása után hívja meg.
 * 5 másodpercen belül válaszol; bizonytalanság esetén nem tilt.
 */
class IncomingCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) return

        val number = callDetails.handle?.schemeSpecificPart
        var shouldBlock = false
        try {
            if (SettingsStore.enabled(this) &&
                ForeignNumberClassifier.classify(number) ==
                    ForeignNumberClassifier.Classification.FOREIGN
            ) {
                val isException = SettingsStore.allowContacts(this) &&
                    !number.isNullOrBlank() && ContactChecker.isSaved(this, number)
                shouldBlock = !isException
            }
        } catch (_: Exception) {
            // A jogosultsági / telefonkönyvi hibák ne szakítsanak meg hívást.
            shouldBlock = false
        }

        respondToCall(callDetails, CallResponse.Builder()
            .setDisallowCall(shouldBlock)
            .setRejectCall(shouldBlock)
            .setSkipCallLog(false)
            .setSkipNotification(shouldBlock)
            .build())

        if (shouldBlock) {
            runCatching { BlockedCallStore.add(this, number ?: "ismeretlen") }
        }
        // A hívás eldöntése után szinkronizáljuk az állapotjelzést.
        runCatching { ProtectionNotification.sync(this) }
    }
}
