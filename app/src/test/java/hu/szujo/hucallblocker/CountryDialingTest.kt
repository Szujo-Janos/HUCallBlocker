package hu.szujo.hucallblocker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryDialingTest {
    @Test fun resolvesCountriesFromPhoneMetadata() {
        assertEquals("GB", CountryDialing.region("+44 20 7946 0958"))
        assertEquals("DE", CountryDialing.region("0049 151 12345678"))
        assertEquals("AT", CountryDialing.region("+43 664 1234567"))
        assertEquals("US", CountryDialing.region("+1 202 555 0142"))
        assertEquals("CA", CountryDialing.region("+1 416 555 0123"))
    }

    @Test fun invalidNumbersHaveNoCountry() {
        assertEquals("HU", CountryDialing.region("06301234567"))
        assertNull(CountryDialing.region("rejtett"))
        assertTrue(CountryDialing.isSupported("HU"))
    }

    @Test fun legacyMissingOrInvalidCountryIsRecovered() {
        val expected = CountryDialing.name("DE")
        assertEquals(expected, CountryDialing.label("+4915112345678", null))
        assertEquals(expected, CountryDialing.label("+4915112345678", ""))
        assertEquals(expected, CountryDialing.label("+4915112345678", "ZZ"))
        assertEquals(expected, CountryDialing.label("+4915112345678", "null"))
        assertEquals(expected, CountryDialing.label("+4915112345678", "GB"))
        val region = CountryDialing.region("+447911123456")
        assertTrue(region == "GG" || region == "GB")
        assertEquals(CountryDialing.name(region), CountryDialing.label("+447911123456", null))
    }

    @Test fun sharedPrefixUsesSpecificRegionWhenIdentifiable() {
        assertEquals("GB", CountryDialing.region("+442079460958"))
        assertEquals("GG", CountryDialing.region("+441481256789"))
        assertEquals("JE", CountryDialing.region("+441534456789"))
        assertEquals("IM", CountryDialing.region("+441624756789"))
        assertNull(CountryDialing.region("+10000000000"))
        assertTrue(CountryDialing.label("+10000000000", null).startsWith("Közös előhívó (+1)"))
    }

    @Test fun nonGeographicAndUnknownAreDistinct() {
        assertNull(CountryDialing.region("+80012345678"))
        assertEquals("Nem földrajzi előhívó (+800)",
            CountryDialing.label("+80012345678", "GB", withFlag = true))
        assertEquals("Ismeretlen ország", CountryDialing.label("+99912345678", null))
        assertEquals("Ismeretlen", CountryDialing.label("rejtett", null))
    }

    @Test fun flagsMatchResolvedRegion() {
        assertEquals("🇩🇪", CountryDialing.flag("DE"))
        assertEquals("🇬🇬", CountryDialing.flag("GG"))
        assertNull(CountryDialing.flag("001"))
        assertNull(CountryDialing.flag("ZZ"))
        assertEquals("🇩🇪 " + CountryDialing.name("DE"),
            CountryDialing.label("+4915112345678", null, withFlag = true))
    }
}
