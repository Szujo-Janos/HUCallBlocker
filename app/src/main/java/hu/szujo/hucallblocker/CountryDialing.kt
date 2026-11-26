package hu.szujo.hucallblocker

import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

/** Az országot az előhívó alapján becsüljük, nem a hívó tartózkodási helyéből. */
internal object CountryDialing {
    data class Country(val iso: String, val name: String, val callingCode: Int)
    private val util by lazy { PhoneNumberUtil.getInstance() }
    private val hu = Locale("hu", "HU")

    val countries: List<Country> by lazy {
        util.supportedRegions.map { iso ->
            Country(iso, Locale("", iso).getDisplayCountry(hu), util.getCountryCodeForRegion(iso))
        }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }

    fun isSupported(iso: String): Boolean = iso in util.supportedRegions

    fun name(iso: String?): String? {
        if (iso == null || !isSupported(iso)) return null
        return Locale("", iso).getDisplayCountry(hu)
    }

    fun region(raw: String?): String? {
        val normalized = NumberRules.normalize(raw) ?: return null
        return try {
            val parsed = util.parse(normalized, "ZZ")
            val primary = util.getRegionCodeForNumber(parsed)
            when {
                primary != null && primary != "001" && isSupported(primary) -> primary
                else -> util.getRegionCodesForCountryCode(parsed.countryCode)
                    .singleOrNull()?.takeIf(::isSupported)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun label(raw: String?, iso: String? = region(raw)): String =
        name(iso) ?: if (ForeignNumberClassifier.classify(raw) ==
            ForeignNumberClassifier.Classification.FOREIGN) "Ismeretlen ország" else "Ismeretlen"
}
