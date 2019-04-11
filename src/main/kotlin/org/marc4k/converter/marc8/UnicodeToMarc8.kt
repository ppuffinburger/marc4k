package org.marc4k.converter.marc8

import org.marc4k.COMBINING_DOUBLE_INVERTED_BREVE_CHARACTER
import org.marc4k.COMBINING_DOUBLE_TILDE_CHARACTER
import org.marc4k.IsoCode
import org.marc4k.SPACE_CHARACTER
import org.marc4k.converter.CharacterConverter
import org.marc4k.converter.CharacterConverterResult
import org.marc4k.converter.NcrGenerator
import org.marc4k.converter.ReverseCodeTable
import java.io.FileInputStream
import java.io.InputStream
import java.text.Normalizer

class UnicodeToMarc8 : CharacterConverter {
    private val codeTable: ReverseCodeTable
    private val defaultCharacterSetsOnly: Boolean
    private val useCompatibilityDecomposition: Boolean
    private val ncrGenerator = NcrGenerator()
    private val escapeSequenceGenerator = Marc8EscapeSequenceGenerator()

    constructor(filename: String, defaultCharacterSetsOnly: Boolean = false, useCompatibilityDecomposition: Boolean = false) : this(FileInputStream(filename), defaultCharacterSetsOnly, useCompatibilityDecomposition)

    constructor(inputStream: InputStream, defaultCharacterSetsOnly: Boolean = false, useCompatibilityDecomposition: Boolean = false) {
        codeTable = ReverseCodeTable(inputStream)
        this.defaultCharacterSetsOnly = defaultCharacterSetsOnly
        this.useCompatibilityDecomposition = useCompatibilityDecomposition
    }

    override fun convert(data: CharArray): CharacterConverterResult {
        val convertedString = with(StringBuilder()) {
            val codeTableTracker = CodeTableTracker()

            convertPortion(data, codeTableTracker, this)

            append(escapeSequenceGenerator.generate(BASIC_LATIN_GRAPHIC_ISO_CODE, codeTableTracker))

            toString()
        }

        return CharacterConverterResult.Success(convertedString)
    }

    private fun convertPortion(data: CharArray, codeTableTracker: CodeTableTracker, conversion: StringBuilder) {
        var previousLength = 1
        var diacriticCount = 0

        for (index in 0..data.lastIndex) {
            val character = data[index]
            val marc8 = with(StringBuilder()) {
                if (character == SPACE_CHARACTER && codeTableTracker.g0 != CJK_GRAPHIC_ISO_CODE) {
                    append(" ")
                } else if (!codeTable.characterHasMatch(character)) {
                    attemptToDecomposeAndConvert(character, codeTableTracker, this)
                } else if (codeTable.isCharacterInCurrentCharacterSet(character, codeTableTracker.g0)) {
                    append(codeTable.getMarc8Array(character, codeTableTracker.g0))
                } else if (codeTable.isCharacterInCurrentCharacterSet(character, codeTableTracker.g1)) {
                    append(codeTable.getMarc8Array(character, codeTableTracker.g1))
                } else if (defaultCharacterSetsOnly) {
                    append(ncrGenerator.generate(character))
                } else {
                    codeTable.getBestCharacterSet(character)?.let { characterSet ->
                        append(escapeSequenceGenerator.generate(characterSet, codeTableTracker))
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
                conversion.append(marc8)
                diacriticCount = 0
            }

            previousLength = marc8.length
        }
    }

    private fun attemptToDecomposeAndConvert(character: Char, codeTableTracker: CodeTableTracker, conversion: StringBuilder) {
        val preNormalized = character.toString()
        val decomposed = Normalizer.normalize(preNormalized, if (useCompatibilityDecomposition) Normalizer.Form.NFKD else Normalizer.Form.NFD)

        val converted = if (preNormalized != decomposed) {
            if (allCharactersHaveMatch(decomposed)) {
                convertPortion(decomposed.toCharArray(), codeTableTracker, conversion)
                true
            } else if (decomposed.length > 2) {
                val firstTwo = decomposed.substring(0, 2)
                val composed = Normalizer.normalize(firstTwo, Normalizer.Form.NFC)

                if (composed != firstTwo && allCharactersHaveMatch(composed) && allCharactersHaveMatch(decomposed.substring(2))) {
                    convertPortion("$composed${decomposed.substring(2)}".toCharArray(), codeTableTracker, conversion)
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
            conversion.append(escapeSequenceGenerator.generate(BASIC_LATIN_GRAPHIC_ISO_CODE, codeTableTracker))
            conversion.append(ncrGenerator.generate(character))
        }
    }

    private fun allCharactersHaveMatch(data: String): Boolean {
        return data.all { codeTable.characterHasMatch(it) }
    }
}

data class CodeTableTracker(var g0: IsoCode = BASIC_LATIN_GRAPHIC_ISO_CODE, var g1: IsoCode = EXTENDED_LATIN_GRAPHIC_ISO_CODE)