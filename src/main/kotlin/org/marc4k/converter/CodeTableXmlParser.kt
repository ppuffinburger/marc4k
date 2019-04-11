package org.marc4k.converter

import org.marc4k.IsoCode
import org.marc4k.Marc8Code
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.XMLReaderFactory
import java.io.InputStream

class CodeTableXmlParser : CodeTableHandlerCallback {
    private val characterSets = mutableMapOf<IsoCode, Map<Marc8Code, Char>>()
    private val combiningCodes = mutableMapOf<IsoCode, List<Marc8Code>>()

    fun parse(inputStream: InputStream): CodeTableParseResult {
        val reader = try {
            XMLReaderFactory.createXMLReader()
        } catch (e: SAXException) {
            return CodeTableParseResult.Failure(e)
        }

        reader.contentHandler = CodeTableHandler(this)

        try {
            reader.parse(InputSource(inputStream))
        } catch (e: Exception) {
            return CodeTableParseResult.Failure(e)
        }

        return CodeTableParseResult.Success(characterSets.toMap(), combiningCodes.toMap())
    }

    override fun updateIsoCodeMaps(isoCode: IsoCode, characterSet: Map<Marc8Code, Char>, combiningCodes: List<Marc8Code>) {
        characterSets[isoCode] = characterSet
        this.combiningCodes[isoCode] = combiningCodes
    }
}

sealed class CodeTableParseResult {
    data class Success(val characterSets: Map<IsoCode, Map<Marc8Code, Char>>, val combiningCodes: Map<IsoCode, List<Marc8Code>>) : CodeTableParseResult()
    data class Failure(val exception: Exception) : CodeTableParseResult()
}