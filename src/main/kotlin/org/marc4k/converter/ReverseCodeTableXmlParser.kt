package org.marc4k.converter

import org.marc4k.IsoCode
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory

class ReverseCodeTableXmlParser : ReverseCodeTableHandlerCallback {
    private val characterSets = mutableMapOf<Char, MutableMap<IsoCode, CharArray>>()
    private val combiningCharacters = mutableSetOf<Char>()

    fun parse(inputStream: InputStream): ReverseCodeTableParseResult {
        val reader = try {
            SAXParserFactory.newInstance().newSAXParser().xmlReader
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

    override fun updateIsoCodeMaps(characterSet: IsoCode, characterSets: List<Pair<Char, CharArray>>, combiningCharacters: Set<Char>) {
        characterSets.forEach { pair ->
            this.characterSets[pair.first]?.put(characterSet, pair.second)
                ?: this.characterSets.computeIfAbsent(pair.first) { mutableMapOf(characterSet to pair.second) }
        }
        this.combiningCharacters.addAll(combiningCharacters)
    }
}


sealed class ReverseCodeTableParseResult {
    data class Success(val characterSets: Map<Char, Map<IsoCode, CharArray>>, val combiningCharacters: Set<Char>) : ReverseCodeTableParseResult()
    data class Failure(val exception: Exception) : ReverseCodeTableParseResult()
}