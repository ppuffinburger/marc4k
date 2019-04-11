package org.marc4k.converter

import org.marc4k.IsoCode
import org.marc4k.MarcException
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI

class ReverseCodeTable {
    private val characterSets: Map<Char, Map<IsoCode, CharArray>>
    private val combiningCharacters: Set<Char>

    private var g0 = 0x42
    private var g1 = 0x45
    private val characterSetsUsed = mutableSetOf(0x42, 0x45)

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

    fun init() {
        g0 = 0x42
        g1 = 0x45
        characterSetsUsed.retainAll { it == 0x42 || it == 0x45 }
    }

    fun getPreviousG0() = g0

    fun setPreviousG0(isoCode: IsoCode) {
        g0 = isoCode
    }

    fun getPreviousG1() = g1

    fun setPreviousG1(isoCode: IsoCode) {
        g1 = isoCode
    }

    fun isCombining(character: Char) = combiningCharacters.contains(character)

    fun getMarc8Array(character: Char, isoCode: IsoCode) = characterSets[character]?.get(isoCode) ?: CharArray(0)

    fun characterHasMatch(character: Char): Boolean = characterSets[character] != null

    fun inPreviousG0CharEntry(character: Char): Boolean {
        return characterSets[character]?.get(g0)?.let { true } ?: false
    }

    fun inPreviousG1CharEntry(character: Char): Boolean {
        return characterSets[character]?.get(g1)?.let { true } ?: false
    }

    fun getCurrentG0CharEntry(character: Char): CharArray? {
        return characterSets[character]?.get(g0)
    }

    fun getCurrentG1CharEntry(character: Char): CharArray? {
        return characterSets[character]?.get(g1)
    }

    fun getBestCharacterSet(character: Char): IsoCode? {
        val returnIsoCode = characterSets[character]?.let { map ->
            when {
                map.keys.count() == 1 -> map.keys.first()
                characterSetsUsed.any { map.keys.contains(it) } -> characterSetsUsed.first { map.keys.contains(it) }
                map.containsKey(0x53) -> 0x53
                else -> map.keys.first()
            }
        }

        returnIsoCode?.let { characterSetsUsed.add(it) }

        return returnIsoCode
    }
}