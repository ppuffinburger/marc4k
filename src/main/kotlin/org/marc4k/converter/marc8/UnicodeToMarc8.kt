package org.marc4k.converter.marc8

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

    constructor(filename: String, defaultCharacterSetsOnly: Boolean = false, useCompatibilityDecomposition: Boolean = false) : this(FileInputStream(filename), defaultCharacterSetsOnly, useCompatibilityDecomposition)

    constructor(inputStream: InputStream, defaultCharacterSetsOnly: Boolean = false, useCompatibilityDecomposition: Boolean = false) {
        codeTable = ReverseCodeTable(inputStream)
        this.defaultCharacterSetsOnly = defaultCharacterSetsOnly
        this.useCompatibilityDecomposition = useCompatibilityDecomposition
    }

    override fun convert(data: CharArray): CharacterConverterResult {
        val convertedString = with(StringBuilder()) {
            codeTable.init()

            convertPortion(data, this)

            if (codeTable.getPreviousG0() != 0x42) {
                append("\u001B\u0028\u0042")
            }

            toString()
        }

        return CharacterConverterResult.Success(convertedString)
    }

    private fun convertPortion(data: CharArray, conversion: StringBuilder) {
        var previousLength = 1

        for (index in 0..data.lastIndex) {
            val character = data[index]
            val marc8 = with(StringBuilder()) {
                if (character == '\u0020' && codeTable.getPreviousG0() != 0x31) {
                    append(" ")
                } else if (!codeTable.characterHasMatch(character)) {
                    val preNormalized = character.toString()
                    val decomposed = Normalizer.normalize(preNormalized, if (useCompatibilityDecomposition) Normalizer.Form.NFKD else Normalizer.Form.NFD)

                    // TODO : this is ugly because of the with()
                    val converted = if (preNormalized != decomposed) {
                        if (allCharactersHaveMatch(decomposed)) {
                            convertPortion(decomposed.toCharArray(), this)
                            true
                        } else if (decomposed.length > 2) {
                            val firstTwo = decomposed.substring(0, 2)
                            val composed = Normalizer.normalize(firstTwo, Normalizer.Form.NFC)

                            if (composed != firstTwo && allCharactersHaveMatch(composed) && allCharactersHaveMatch(decomposed.substring(2))) {
                                convertPortion("$composed${decomposed.substring(2)}".toCharArray(), this)
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
                        if (codeTable.getPreviousG0() != 0x42) {
                            append("\u001B\u0028\u0042")
                            codeTable.setPreviousG0(0x42)
                        }

                        append(ncrGenerator.generate(character))
                    }
                } else if (codeTable.inPreviousG0CharEntry(character)) {
                    append(codeTable.getCurrentG0CharEntry(character))
                } else if (codeTable.inPreviousG1CharEntry(character)) {
                    append(codeTable.getCurrentG1CharEntry(character))
                } else if (defaultCharacterSetsOnly) {
                    append(ncrGenerator.generate(character))
                } else {
                    codeTable.getBestCharacterSet(character)?.let { characterSet ->
                        val marc8Array = codeTable.getMarc8Array(character, characterSet)

                        when {
                            marc8Array.size == 3 -> {
                                append("\u001B\u0024")
                                codeTable.setPreviousG0(characterSet)
                            }
                            marc8Array[0] < '\u0080' -> {
                                append("\u001B")

                                if (characterSet == 0x62 || characterSet == 0x70) {
                                    // technique 1
                                } else {
                                    append("\u0028")
                                }

                                codeTable.setPreviousG0(characterSet)
                            }
                            else -> {
                                append("\u001B\u0029")
                                codeTable.setPreviousG1(characterSet)
                            }
                        }

                        append("${characterSet.toChar()}${marc8Array.joinToString("")}")
                    }
                }

                toString()
            }

            if (codeTable.isCombining(character) && conversion.isNotEmpty()) {
                conversion.insert(conversion.length - previousLength, marc8)

                if (character == '\u0360') {
                    conversion.append('\u00FB')
                }

                if (character == '\u0361') {
                    conversion.append('\u00EC')
                }
            } else {
                conversion.append(marc8)
            }

            previousLength = marc8.length
        }
    }

    private fun allCharactersHaveMatch(data: String): Boolean {
        return data.all { codeTable.characterHasMatch(it) }
    }
}