package org.marc4k.converter

import org.marc4k.MarcException
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI

class CodeTable {
    private val characterSets: Map<IsoCode, Map<Marc8Code, Char>>
    private val combiningCodes: Map<IsoCode, List<Marc8Code>>

    constructor(inputStream: InputStream) {
        when (val result = CodeTableXmlParser().parse(inputStream)) {
            is ParseResult.Failure -> throw MarcException("Unable to process the Code Table", result.error)
            is ParseResult.Success -> {
                characterSets = result.characterSets
                combiningCodes = result.combiningCodes
            }
        }
    }

    constructor(filename: String) : this(FileInputStream(filename))

    constructor(uri: URI) : this(uri.toURL().openStream())

    fun isCombining(marc8Code: Marc8Code, g0: GraphicSet, g1: GraphicSet): Boolean {
        return if (marc8Code <= 0x7E) {
            combiningCodes[g0]?.contains(marc8Code) ?: combiningCodes[g0]?.contains(marc8Code + 0x80) ?: false
        } else {
            combiningCodes[g1]?.contains(marc8Code) ?: combiningCodes[g1]?.contains(marc8Code - 0x80) ?: false
        }
    }

    fun getChar(marc8Code: Marc8Code, isoCode: IsoCode): Char {
        if (marc8Code == 0x20) {
            return marc8Code.toChar()
        } else {
            characterSets[isoCode]?.let {
                return it[marc8Code]
                    ?: it[if (marc8Code < 0x80) marc8Code + 0x80 else marc8Code - 0x80]
                    ?: CODE_TABLE_CHARACTER_NOT_FOUND
            } ?: return marc8Code.toChar()
        }
    }
}

internal const val CODE_TABLE_CHARACTER_NOT_FOUND = 0.toChar()

typealias IsoCode = Int
typealias Marc8Code = Int
typealias GraphicSet = Int