package org.marc4k.converter

import org.marc4k.IsoCode
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.XMLReaderFactory
import java.io.InputStream

class ReverseCodeTableXmlParser : ReverseCodeTableHandlerCallback {
    private val characterSets = mutableMapOf<Char, MutableMap<IsoCode, CharArray>>()
    private val combiningCharacters = mutableSetOf<Char>()

    fun parse(inputStream: InputStream): ReverseCodeTableParseResult {
        val reader = try {
            XMLReaderFactory.createXMLReader()
        } catch (e: SAXException) {
            return ReverseCodeTableParseResult.Failure(e)
        }

        reader.contentHandler = ReverseCodeTableHandler(this)

        try {
            reader.parse(InputSource(inputStream))
        } catch (e: Exception) {
            return ReverseCodeTableParseResult.Failure(e)
        }

        return ReverseCodeTableParseResult.Success(characterSets.toMap(), combiningCharacters.toHashSet())
    }

    override fun updateIsoCodeMaps(isoCode: IsoCode, characterSets: List<Pair<Char, CharArray>>, combiningCharacters: Set<Char>) {
        characterSets.forEach { pair ->
//            this.characterSets[pair.first]?.put(isoCode, pair.second)
//                ?: this.characterSets.computeIfAbsent(pair.first) { mutableMapOf(isoCode to pair.second) }
            val characterMap = this.characterSets[pair.first]
            if (characterMap == null) {
                this.characterSets[pair.first] = mutableMapOf(isoCode to pair.second)
            } else {
                characterMap.putIfAbsent(isoCode, pair.second)
            }
        }
        this.combiningCharacters.addAll(combiningCharacters)
    }
}


sealed class ReverseCodeTableParseResult {
    data class Success(val characterSets: Map<Char, Map<IsoCode, CharArray>>, val combiningCharacters: Set<Char>) : ReverseCodeTableParseResult()
    data class Failure(val exception: Exception) : ReverseCodeTableParseResult()
}