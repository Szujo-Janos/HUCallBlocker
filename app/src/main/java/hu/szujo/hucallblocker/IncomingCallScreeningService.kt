package hu.szujo.hucallblocker

import android.telecom.Call
import android.telecom.CallScreeningService

class IncomingCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) return
        val number = callDetails.handle?.schemeSpecificPart
        var decision = CallRules.Decision(false)
        var region: String? = null
        try {
            if (SettingsStore.enabled(this)) {
                val allowed = SettingsStore.allowedNumbers(this)
                val blocked = SettingsStore.blockedNumbers(this)
                region = CountryDialing.region(number)
                val normalized = NumberRules.normalize(number)
                val foreign = ForeignNumberClassifier.classify(number) ==
                    ForeignNumberClassifier.Classification.FOREIGN
                val checkContact = foreign && SettingsStore.allowContacts(this) &&
                    (normalized == null || (normalized !in blocked && normalized !in allowed)) &&
                    ContactChecker.hasPermission(this) && !number.isNullOrBlank()
                val saved = checkContact && ContactChecker.isSaved(this, number!!)
                decision = CallRules.decide(
                    number = number,
                    enabled = true,
                    isSavedContact = saved,
                    allowContacts = SettingsStore.allowContacts(this),
                    allowedCountries = SettingsStore.allowedCountries(this),
                    allowedNumbers = allowed,
                    blockedNumbers = blocked,
                    region = region
                )
            }
        } catch (_: Exception) {
            // Hiba esetén a korábbi viselkedésnek megfelelően átengedjük a hívást.
            decision = CallRules.Decision(false)
        }

        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(decision.block)
                .setRejectCall(decision.block)
                .setSkipCallLog(false)
                .setSkipNotification(decision.block)
                .build()
        )
        if (decision.block) {
            runCatching {
                BlockedCallStore.add(
                    this,
                    number ?: "ismeretlen",
                    region,
                    decision.reason?.label ?: "Külföldi szám"
                )
            }
        }
        runCatching { ProtectionNotification.sync(this) }
    }
}
