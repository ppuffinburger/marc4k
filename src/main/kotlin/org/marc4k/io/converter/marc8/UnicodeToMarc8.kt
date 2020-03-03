package org.marc4k.io.converter.marc8

import org.marc4k.COMBINING_DOUBLE_INVERTED_BREVE_CHARACTER
import org.marc4k.COMBINING_DOUBLE_TILDE_CHARACTER
import org.marc4k.IsoCode
import org.marc4k.SPACE_CHARACTER
import org.marc4k.io.converter.CharacterConverter
import org.marc4k.io.converter.CharacterConverterResult
import org.marc4k.io.converter.NcrGenerator
import org.marc4k.io.converter.ReverseCodeTable
import java.io.FileInputStream
import java.io.InputStream
import java.text.Normalizer

/**
 * A [CharacterConverter] that converts Unicode to MARC8.
 */
class UnicodeToMarc8 : CharacterConverter {
    private val codeTable: ReverseCodeTable
    private val defaultCharacterSetsOnly: Boolean
    private val useCompatibilityDecomposition: Boolean
    private val ncrGenerator: NcrGenerator by lazy { NcrGenerator() }
    private val escapeSequenceGenerator = Marc8EscapeSequenceGenerator()

    /**
     * Instantiates class using the internal MARC8 code tables.
     *
     * @param[defaultCharacterSetsOnly] true if only using the default G0 and G1 character sets.  Defaults to false.
     * @param[useCompatibilityDecomposition] true if using NFKD normalization instead of NFD.  Defaults to false.
     */
    constructor(defaultCharacterSetsOnly: Boolean = false, useCompatibilityDecomposition: Boolean = false) : this(UnicodeToMarc8::class.java.getResourceAsStream("/codetables.xml"), defaultCharacterSetsOnly, useCompatibilityDecomposition)

    /**
     * Instantiates class using the given code table at [filename].
     *
     * @param[defaultCharacterSetsOnly] true if only using the default G0 and G1 character sets.  Defaults to false.
     * @param[useCompatibilityDecomposition] true if using NFKD normalization instead of NFD.  Defaults to false.
     */
    constructor(filename: String, defaultCharacterSetsOnly: Boolean = false, useCompatibilityDecomposition: Boolean = false) : this(FileInputStream(filename), defaultCharacterSetsOnly, useCompatibilityDecomposition)

    /**
     * Instantiates class using the given code table in [inputStream].
     *
     * @param[defaultCharacterSetsOnly] true if only using the default G0 and G1 character sets.  Defaults to false.
     * @param[useCompatibilityDecomposition] true if using NFKD normalization instead of NFD.  Defaults to false.
     */
    constructor(inputStream: InputStream, defaultCharacterSetsOnly: Boolean = false, useCompatibilityDecomposition: Boolean = false) {
        codeTable = ReverseCodeTable(inputStream)
        this.defaultCharacterSetsOnly = defaultCharacterSetsOnly
        this.useCompatibilityDecomposition = useCompatibilityDecomposition
    }

    /**
     * Returns a [CharacterConverterResult] for the given [data].
     */
    override fun convert(data: CharArray): CharacterConverterResult {
        val convertedString = with(StringBuilder()) {
            val tracker = CodeTableTracker()

            convertPortion(data, tracker, this)

            append(escapeSequenceGenerator.generate(BASIC_LATIN_GRAPHIC_ISO_CODE, tracker))

            toString()
        }

        return CharacterConverterResult.Success(convertedString)
    }

    private fun convertPortion(data: CharArray, tracker: CodeTableTracker, conversion: StringBuilder) {
        var previousLength = 1
        var diacriticCount = 0
        var escapeSequence: String? = null

        for (index in 0..data.lastIndex) {
            val character = data[index]
            val marc8 = with(StringBuilder()) {
                if (character == SPACE_CHARACTER && tracker.g0 != CJK_GRAPHIC_ISO_CODE) {
                    append(" ")
                } else if (!codeTable.isCharacterInCodeTable(character)) {
                    attemptToDecomposeAndConvert(character, tracker, this)
                } else if (codeTable.isCharacterInCurrentCharacterSet(character, tracker.g0)) {
                    append(codeTable.getMarc8Array(character, tracker.g0))
                } else if (codeTable.isCharacterInCurrentCharacterSet(character, tracker.g1)) {
                    append(codeTable.getMarc8Array(character, tracker.g1))
                } else if (defaultCharacterSetsOnly) {
                    append(ncrGenerator.generate(character))
                } else {
                    codeTable.getBestCharacterSet(character, tracker.characterSetsUsed)?.let { characterSet ->
                        escapeSequence = escapeSequenceGenerator.generate(characterSet, tracker)
                        append(codeTable.getMarc8Array(character, characterSet))
                    }
                }

                toString()
            }

            if (codeTable.isCombining(character) && conversion.isNotEmpty()) {
                conversion.insert(conversion.length - previousLength - diacriticCount, marc8)

                if (character == COMBINING_DOUBLE_TILDE_CHARACTER) {
                    conversion.append(COMBINING_DOUBLE_TILDE_SECOND_HALF)
                }

                if (character == COMBINING_DOUBLE_INVERTED_BREVE_CHARACTER) {
                    conversion.append(COMBINING_DOUBLE_INVERTED_BREVE_SECOND_HALF)
                }

                diacriticCount++
            } else {
                escapeSequence?.let {
                    conversion.append(it)
                    escapeSequence = null
                }
                conversion.append(marc8)
                diacriticCount = 0
            }

            previousLength = marc8.length
        }
    }

    private fun attemptToDecomposeAndConvert(character: Char, tracker: CodeTableTracker, conversion: StringBuilder) {
        val preNormalized = character.toString()
        val decomposed = Normalizer.normalize(preNormalized, if (useCompatibilityDecomposition) Normalizer.Form.NFKD else Normalizer.Form.NFD)

        val converted = if (preNormalized != decomposed) {
            if (allCharactersHaveMatch(decomposed)) {
                convertPortion(decomposed.toCharArray(), tracker, conversion)
                true
            } else if (decomposed.length > 2) {
                val firstTwo = decomposed.substring(0, 2)
                val composed = Normalizer.normalize(firstTwo, Normalizer.Form.NFC)

                if (composed != firstTwo && allCharactersHaveMatch(composed) && allCharactersHaveMatch(decomposed.substring(2))) {
                    convertPortion("$composed${decomposed.substring(2)}".toCharArray(), tracker, conversion)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } else {
            false
        }

        if (!converted) {
            conversion.append(escapeSequenceGenerator.generate(BASIC_LATIN_GRAPHIC_ISO_CODE, tracker))
            conversion.append(ncrGenerator.generate(character))
        }
    }

    private fun allCharactersHaveMatch(data: String): Boolean {
        return data.all { codeTable.isCharacterInCodeTable(it) }
    }
}

data class CodeTableTracker(var g0: IsoCode = BASIC_LATIN_GRAPHIC_ISO_CODE, var g1: IsoCode = EXTENDED_LATIN_GRAPHIC_ISO_CODE, val characterSetsUsed: MutableSet<IsoCode> = mutableSetOf(g0, g1))