package hu.szujo.hucallblocker

import org.junit.Assert.assertEquals
import org.junit.Test

class ForeignNumberClassifierTest {
    private val H = ForeignNumberClassifier.Classification.HUNGARIAN
    private val F = ForeignNumberClassifier.Classification.FOREIGN
    private val U = ForeignNumberClassifier.Classification.UNKNOWN

    @Test fun hungarian() {
        assertEquals(H, ForeignNumberClassifier.classify("+36 30 123 4567"))
        assertEquals(H, ForeignNumberClassifier.classify("0036-1-234-5678"))
    }

    @Test fun foreign() {
        assertEquals(F, ForeignNumberClassifier.classify("+44 7911 123456"))
        assertEquals(F, ForeignNumberClassifier.classify("0049 151 12345678"))
        assertEquals(F, ForeignNumberClassifier.classify("+1 (202) 555-0142"))
    }

    @Test fun unknownPassesThrough() {
        for (number in listOf(null, "", "rejtett", "06301234567", "301234567", "+0", "004")) {
            assertEquals(U, ForeignNumberClassifier.classify(number))
        }
    }
}
