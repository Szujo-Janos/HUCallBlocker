package hu.szujo.hucallblocker

/**
 * Nem valódi tartózkodási helyet vizsgál: kizárólag a kapott hívószám
 * nemzetközi előhívóját. A bizonytalan formátumokat átengedi.
 */
object ForeignNumberClassifier {
    enum class Classification { HUNGARIAN, FOREIGN, UNKNOWN }

    fun classify(raw: String?): Classification {
        if (raw.isNullOrBlank()) return Classification.UNKNOWN
        val input = raw.trim()
        val digits = when {
            input.startsWith("+") -> input.drop(1).filter(Char::isDigit)
            input.startsWith("00") -> input.drop(2).filter(Char::isDigit)
            else -> return Classification.UNKNOWN
        }
        if (digits.length < 5 || digits.startsWith('0')) return Classification.UNKNOWN
        return if (digits.startsWith("36")) Classification.HUNGARIAN
        else Classification.FOREIGN
    }
}
