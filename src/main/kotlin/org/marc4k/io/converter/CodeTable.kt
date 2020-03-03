package org.marc4k.io.converter

import org.marc4k.IsoCode
import org.marc4k.MarcCode
import org.marc4k.MarcException
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI

/**
 * Class used to handle MARC character to Unicode lookups.
 */
class CodeTable {
    private val characterSets: Map<IsoCode, Map<MarcCode, Char>>
    private val combiningCodes: Map<IsoCode, List<MarcCode>>

    /**
     * Instantiates class using given code table in [inputStream].
     */
    constructor(inputStream: InputStream) {
        when (val result = CodeTableXmlParser().parse(inputStream)) {
            is CodeTableParseResult.Failure -> throw MarcException("Unable to process the Code Table", result.exception)
            is CodeTableParseResult.Success -> {
                characterSets = result.characterSets
                combiningCodes = result.combiningCodes
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
     * Returns true if given [marcCode] in a given [g0] or [g1] is a combining character.
     */
    fun isCombining(marcCode: MarcCode, g0: IsoCode, g1: IsoCode): Boolean {
        return when (marcCode) {
            in 0x20..0x7E -> combiningCodes[g0]?.contains(marcCode) ?: combiningCodes[g0]?.contains(marcCode + 0x80) ?: false
            in 0xA0..0xFE -> combiningCodes[g1]?.contains(marcCode) ?: combiningCodes[g1]?.contains(marcCode - 0x80) ?: false
            else -> false
        }
    }

    /**
     * Returns character for given [marcCode] in [characterSet].  Will return null if no character is found.
     */
    fun getChar(marcCode: MarcCode, characterSet: IsoCode): Char? {
        if (marcCode == 0x20) {
            return marcCode.toChar()
        } else {
            characterSets[characterSet]?.let {
                return it[marcCode] ?: it[if (marcCode < 0x80) marcCode + 0x80 else marcCode - 0x80] } ?: return marcCode.toChar()
        }
    }
}