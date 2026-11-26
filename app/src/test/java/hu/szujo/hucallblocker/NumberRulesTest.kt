package hu.szujo.hucallblocker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NumberRulesTest {
    @Test fun normalizesInternationalAndHungarianNumbers() {
        assertEquals("+4915112345678", NumberRules.normalize("+49 151 12345678"))
        assertEquals("+4915112345678", NumberRules.normalize("0049 (151) 12345678"))
        assertEquals("+36301234567", NumberRules.normalize("06 30 1234567"))
        assertEquals("+36301234567", NumberRules.normalize("+36-30-1234567"))
    }

    @Test fun rejectsAmbiguousAndInvalidInputs() {
        for (n in listOf("", "301234567", "+", "++49123456", "+0", "+123x456", "+1234567890123456")) {
            assertNull(n, NumberRules.normalize(n))
        }
    }
}
