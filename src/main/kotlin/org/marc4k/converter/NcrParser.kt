package org.marc4k.converter

import java.util.regex.Pattern

class NcrParser {
    fun parse(input: String): String {
        var last = 0

        val matcher = pattern.matcher(input)

        with(StringBuilder()) {
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