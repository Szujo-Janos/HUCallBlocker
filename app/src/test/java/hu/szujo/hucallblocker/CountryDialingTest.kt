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
}
