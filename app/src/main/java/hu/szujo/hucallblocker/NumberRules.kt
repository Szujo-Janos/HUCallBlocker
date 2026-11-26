package hu.szujo.hucallblocker

/** A saját listákban csak egyértelmű, nemzetközi alakú számokat tárolunk. */
internal object NumberRules {
    fun normalize(raw: String?): String? {
        val input = raw?.trim() ?: return null
        if (input.any { !it.isDigit() && it !in "+ -()." }) return null
        val compact = input.filter { it.isDigit() || it == '+' }
        if (compact.count { it == '+' } > 1 || (compact.contains('+') && !compact.startsWith("+"))) return null
        val digits = when {
            compact.startsWith("+") -> compact.drop(1)
            compact.startsWith("00") -> compact.drop(2)
            compact.startsWith("06") && compact.length in 10..13 -> "36" + compact.drop(2)
            else -> return null
        }
        if (digits.length !in 5..15 || digits.startsWith('0')) return null
        return "+$digits"
    }
}
