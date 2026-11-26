package hu.szujo.hucallblocker

import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

/** Az ország a számozási tervből becsülhető, nem a hívó tartózkodási helyéből. */
internal object CountryDialing {
    data class Country(val iso: String, val name: String, val callingCode: Int)
    private data class Resolution(
        val region: String?,
        val callingCode: Int,
        val shared: Boolean,
        val nonGeographic: Boolean
    )

    private val util by lazy { PhoneNumberUtil.getInstance() }
    private val hu = Locale("hu", "HU")

    val countries: List<Country> by lazy {
        util.supportedRegions.map { iso ->
            Country(iso, Locale("", iso).getDisplayCountry(hu), util.getCountryCodeForRegion(iso))
        }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }

    fun isSupported(iso: String): Boolean = iso in util.supportedRegions

    private fun validIso(iso: String?): String? =
        iso?.trim()?.uppercase(Locale.ROOT)?.takeIf(::isSupported)

    fun name(iso: String?): String? =
        validIso(iso)?.let { Locale("", it).getDisplayCountry(hu) }

    private fun resolve(raw: String?): Resolution? {
        val normalized = NumberRules.normalize(raw) ?: return null
        return try {
            val parsed = util.parse(normalized, "ZZ")
            val regions = util.getRegionCodesForCountryCode(parsed.countryCode)
            val geographical = regions.filter(::isSupported)
            val nonGeographic = "001" in regions
            // For shared numbering plans, use the metadata's number-specific region,
            // never the first/main country solely because the prefix is shared.
            val identified = if (nonGeographic) null else {
                util.getRegionCodeForNumber(parsed)?.takeIf { it in geographical }
                    ?: geographical.singleOrNull()
            }
            Resolution(identified, parsed.countryCode, geographical.size > 1, nonGeographic)
        } catch (_: Exception) {
            null
        }
    }

    /** Country exceptions continue to use the number's metadata, not a display label. */
    fun region(raw: String?): String? = resolve(raw)?.region

    fun flag(iso: String?): String? {
        val code = validIso(iso) ?: return null
        if (code.length != 2 || code.any { it !in 'A'..'Z' }) return null
        return String(Character.toChars(0x1F1E6 + (code[0] - 'A'))) +
            String(Character.toChars(0x1F1E6 + (code[1] - 'A')))
    }

    /**
     * Legacy log entries may have no ISO code. Re-resolve the number on display.
     * A previously stored ISO is only a fallback when the number cannot be parsed.
     * Never attach a national flag to a shared unresolved or non-geographic code.
     */
    fun label(raw: String?, iso: String? = null, withFlag: Boolean = false): String {
        val resolution = resolve(raw)
        val identified = if (resolution == null) validIso(iso) else resolution.region
        val countryName = name(identified)
        if (countryName != null) {
            val symbol = if (withFlag) flag(identified) else null
            return if (symbol == null) countryName else "$symbol $countryName"
        }
        return when {
            resolution?.nonGeographic == true ->
                "Nem földrajzi előhívó (+${resolution.callingCode})"
            resolution?.shared == true ->
                "Közös előhívó (+${resolution.callingCode}) · ország nem azonosítható"
            ForeignNumberClassifier.classify(raw) == ForeignNumberClassifier.Classification.FOREIGN ->
                "Ismeretlen ország"
            else -> "Ismeretlen"
        }
    }
}
