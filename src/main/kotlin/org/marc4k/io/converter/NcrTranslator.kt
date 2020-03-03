package org.marc4k.io.converter

import java.util.regex.Pattern

/**
 * Class to translate Numeric Character References.
 */
class NcrTranslator {
    /**
     * Returns a string of the given [input] while replacing embedded NCRs with their actual Unicode characters.
     */
    fun parse(input: String): String {
        with(StringBuilder()) {
            var last = 0
            val matcher = pattern.matcher(input)

            while (matcher.find()) {
                append(input.substring(last, matcher.start()))
                append(matcher.group().substring(3, matcher.group().length - 1).toInt(16).toChar())
                last = matcher.end()
            }

            append(input.substring(last))
            return toString()
        }
    }

    companion object {
        private val pattern = Pattern.compile("&#[xX][0123456789abcdefABCDEF]{2,6};")
    }
}