package hu.szujo.hucallblocker

/** Tiszta döntési függvény: a tesztek nem igényelnek Android-futókörnyezetet. */
internal object CallRules {
    enum class Reason(val label: String) {
        OWN_BLOCK("Saját tiltólista"),
        FOREIGN("Külföldi szám")
    }

    data class Decision(val block: Boolean, val reason: Reason? = null)

    fun decide(
        number: String?,
        enabled: Boolean,
        isSavedContact: Boolean,
        allowContacts: Boolean,
        allowedCountries: Set<String>,
        allowedNumbers: Set<String>,
        blockedNumbers: Set<String>,
        region: String?
    ): Decision {
        if (!enabled) return Decision(false)
        val normalized = NumberRules.normalize(number)
        if (normalized != null && normalized in blockedNumbers) return Decision(true, Reason.OWN_BLOCK)
        if (normalized != null && normalized in allowedNumbers) return Decision(false)
        if (ForeignNumberClassifier.classify(number) != ForeignNumberClassifier.Classification.FOREIGN) {
            return Decision(false)
        }
        if (allowContacts && isSavedContact) return Decision(false)
        if (region != null && region in allowedCountries) return Decision(false)
        return Decision(true, Reason.FOREIGN)
    }
}
