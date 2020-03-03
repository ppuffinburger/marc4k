package org.marc4k.io.converter

/**
 * Class used to generate Numeric Character Reference markup.
 */
class NcrGenerator {
    /**
     * Returns the markup of a given [character].
     */
    fun generate(character: Char) = generate(character.toInt())

    /**
     * Returns the markup of a given [codePoint].
     */
    fun generate(codePoint: Int): String {
        return if (codePoint <= 0xFFFF) {
            "&#x${String.format("%04x", codePoint).toUpperCase()};"
        } else {
            "&#x${String.format("%06x", codePoint).toUpperCase()};"
        }
    }
}