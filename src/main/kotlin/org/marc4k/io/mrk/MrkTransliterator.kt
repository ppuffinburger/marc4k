package org.marc4k.io.mrk

import java.util.regex.Pattern

object MrkTransliterator {
    private val mnemonicPattern = Pattern.compile("""\{([A-Za-z0-9]{2,8}?)}""")
    private val textToMarc: Map<String, String> by lazy { buildTextToMarcMap() }
    private val marcToText: Map<Char, String> by lazy { buildMarcToTextMap() }

    /**
     * Converts the given MARC text [input] with the mnemonics replaced with actual values and returns.
     *
     * If a mnemonic cannot be found it will be converted to an NCR.
     */
    fun fromMrk(input: String): String {
        with(StringBuilder()) {
            var last = 0
            val matcher = mnemonicPattern.matcher(input)

            while (matcher.find()) {
                append(input.substring(last, matcher.start()))
                append(textToMarc[matcher.group(0)] ?: "&${matcher.group(1)};")
                last = matcher.end()
            }

            append(input.substring(last))
            return toString()
        }
    }

    /**
     * Converts the given [input] using MARC text mnemonics and returns.
     *
     * @property[toUtf8] true if converting to UTF8 which will only translated $, {, and } to mnemonics.  Defaults to true.
     */
    fun toMrk(input: String, toUtf8: Boolean = true): String {
        if (toUtf8) {
            return with(StringBuilder()) {
                for (character in input) {
                    when (character) {
                        '$' -> append("{dollar}")
                        '{' -> append("{lcub}")
                        '}' -> append("{rcub}")
                        else -> append(character)
                    }
                }
                toString()
            }
        } else {
            return with(StringBuilder()) {
                for (character in input) {
                    append(marcToText[character] ?: character)
                }
                toString()
            }
        }
    }

    private fun buildTextToMarcMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        javaClass.getResourceAsStream("/text_to_marc.csv").bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if (!line.startsWith("*")) {
                    val (mnemonic, hex) = line.split(",")
                    map[mnemonic] = translateHexSequence(hex)
                }
            }
        }
        return map
    }

    private fun buildMarcToTextMap(): Map<Char, String> {
        val map = mutableMapOf<Char, String>()
        javaClass.getResourceAsStream("/marc_to_text.csv").bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if (!line.startsWith("*")) {
                    val (hex, _, mnemonic) = line.split(",")
                    map[translateHexChar(hex)] = mnemonic
                }
            }
        }
        return map
    }

    private fun translateHexSequence(hexSequence: String): String {
        return with(StringBuilder()) {
            for (index in 0..hexSequence.lastIndex step 4) {
                append("${hexSequence[0]}${hexSequence[1]}".toInt(16).toChar())
            }
            toString()
        }
    }

    private fun translateHexChar(hexChar: String) = "${hexChar[0]}${hexChar[1]}".toInt(16).toChar()
}