package org.marc4k.io.converter

import org.marc4k.IsoCode
import org.marc4k.MarcException
import org.marc4k.io.converter.marc8.BASIC_GREEK_GRAPHIC_ISO_CODE
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI

/**
 * Class used to handle Unicode to MARC character lookups.
 */
class ReverseCodeTable {
    private val characterSets: Map<Char, Map<IsoCode, CharArray>>
    private val combiningCharacters: Set<Char>

    /**
     * Instantiates class using given code table in [inputStream].
     */
    constructor(inputStream: InputStream) {
        when (val result = ReverseCodeTableXmlParser().parse(inputStream)) {
            is ReverseCodeTableParseResult.Failure -> throw MarcException("Unable to process the Code Table", result.exception)
            is ReverseCodeTableParseResult.Success -> {
                characterSets = result.characterSets
                combiningCharacters = result.combiningCharacters
            }
        }
    }

    /**
     * Instantiates class using given code table at [filename].
     */
    constructor(filename: String) : this(FileInputStream(filename))

    /**
     * Instantiates class using given code table at [uri].
     */
    constructor(uri: URI) : this(uri.toURL().openStream())

    /**
     * Returns true if given [character] is a combining character.
     */
    fun isCombining(character: Char) = combiningCharacters.contains(character)

    /**
     * Returns a [CharArray] for the given [character] and [characterSet].  Will return an empty [CharArray] if look up fails.
     */
    fun getMarc8Array(character: Char, characterSet: IsoCode) = characterSets[character]?.get(characterSet) ?: CharArray(0)

    /**
     * Returns true if given [character] is in code table.
     */
    fun isCharacterInCodeTable(character: Char): Boolean = characterSets[character] != null

    /**
     * Returns true if given [character] is exists in [characterSet]
     */
    fun isCharacterInCurrentCharacterSet(character: Char, characterSet: IsoCode): Boolean {
        return characterSets[character]?.get(characterSet) != null
    }

    /**
     * Use only for MARC8!   Do not use for Unimarc!
     *
     * Lookups up the MARC8 translation of a given Unicode [character] and determines which of the MARC8
     * character sets that have a translation for that Unicode character is the best one to use.  If one
     * one charset has a translation, that one will be returned.  If more than one charset has a translation
     * then return the first one listed.
     */
    fun getBestCharacterSet(character: Char, characterSetsUsed: MutableSet<IsoCode>): IsoCode? {
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
}