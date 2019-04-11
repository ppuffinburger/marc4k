package org.marc4k.converter

import org.marc4k.IsoCode
import org.marc4k.MarcException
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI

class ReverseCodeTable {
    private val characterSets: Map<Char, Map<IsoCode, CharArray>>
    private val combiningCharacters: Set<Char>

    private val characterSetsUsed = mutableSetOf(BASIC_LATIN_GRAPHIC_ISO_CODE, EXTENDED_LATIN_GRAPHIC_ISO_CODE)

    constructor(inputStream: InputStream) {
        when (val result = ReverseCodeTableXmlParser().parse(inputStream)) {
            is ReverseCodeTableParseResult.Failure -> throw MarcException("Unable to process the Code Table", result.exception)
            is ReverseCodeTableParseResult.Success -> {
                characterSets = result.characterSets
                combiningCharacters = result.combiningCharacters
            }
        }
    }

    constructor(filename: String) : this(FileInputStream(filename))

    constructor(uri: URI) : this(uri.toURL().openStream())

    fun isCombining(character: Char) = combiningCharacters.contains(character)

    fun getMarc8Array(character: Char, characterSet: IsoCode) = characterSets[character]?.get(characterSet) ?: CharArray(0)

    fun characterHasMatch(character: Char): Boolean = characterSets[character] != null

    fun isCharacterInCurrentCharacterSet(character: Char, characterSet: IsoCode): Boolean {
        return characterSets[character]?.get(characterSet) != null
    }

    fun getBestCharacterSet(character: Char): IsoCode? {
        val characterSet = characterSets[character]?.let { map ->
            when {
                map.keys.count() == 1 -> map.keys.first()
                characterSetsUsed.any { map.keys.contains(it) } -> characterSetsUsed.first { map.keys.contains(it) }
                map.containsKey(BASIC_GREEK_GRAPHIC_ISO_CODE) -> BASIC_GREEK_GRAPHIC_ISO_CODE
                else -> map.keys.first()
            }
        }

        characterSet?.let { characterSetsUsed.add(it) }

        return characterSet
    }

    companion object {
        private const val BASIC_LATIN_GRAPHIC_ISO_CODE = 0x42
        private const val EXTENDED_LATIN_GRAPHIC_ISO_CODE = 0x45
        private const val BASIC_GREEK_GRAPHIC_ISO_CODE = 0x53
    }
}