package hu.szujo.hucallblocker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallRulesTest {
    private fun decide(
        number: String = "+447911123456",
        enabled: Boolean = true,
        saved: Boolean = false,
        contacts: Boolean = false,
        countries: Set<String> = emptySet(),
        allow: Set<String> = emptySet(),
        block: Set<String> = emptySet(),
        region: String? = "GB"
    ) = CallRules.decide(number, enabled, saved, contacts, countries, allow, block, region)

    @Test fun foreignNumbersBlockedByDefaultAndHungarianAllowed() {
        assertEquals(CallRules.Reason.FOREIGN, decide().reason)
        assertTrue(decide().block)
        assertFalse(decide(number = "+36301234567", region = "HU").block)
        assertFalse(decide(number = "06301234567", region = null).block)
    }

    @Test fun countryExceptionWorks() {
        assertFalse(decide(countries = setOf("GB")).block)
        assertTrue(decide(countries = setOf("DE")).block)
    }

    @Test fun savedContactExceptionRequiresToggle() {
        assertFalse(decide(saved = true, contacts = true).block)
        assertTrue(decide(saved = true, contacts = false).block)
    }

    @Test fun ownBlockWinsOverEveryExceptionIncludingHungarian() {
        val number = "+447911123456"
        assertEquals(
            CallRules.Reason.OWN_BLOCK,
            decide(saved = true, contacts = true, countries = setOf("GB"),
                allow = setOf(number), block = setOf(number)).reason
        )
        assertTrue(decide(number = "+36301234567", region = "HU",
            block = setOf("+36301234567")).block)
    }

    @Test fun ownAllowOverridesCountryBlocking() {
        assertFalse(decide(allow = setOf("+447911123456")).block)
    }

    @Test fun pausedFilterPassesEverything() {
        assertFalse(decide(enabled = false, block = setOf("+447911123456")).block)
    }

    @Test fun nonInternationalNumbersAreOnlyBlockedWhenExplicit() {
        assertFalse(decide(number = "301234567", region = null).block)
        assertTrue(decide(number = "06301234567", region = null,
            block = setOf("+36301234567")).block)
    }
}
