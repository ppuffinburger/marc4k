package org.marc4k.converter

import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.XMLReaderFactory
import java.io.InputStream

class CodeTableXmlParser : CodeTableHandlerCallback {
    private val characterSets = mutableMapOf<IsoCode, Map<Marc8Code, Char>>()
    private val combiningCodes = mutableMapOf<IsoCode, List<Marc8Code>>()

    fun parse(inputStream: InputStream): ParseResult {
        val reader = try {
            XMLReaderFactory.createXMLReader()
        } catch (e: SAXException) {
            return ParseResult.Failure(e)
        }

        reader.contentHandler = CodeTableHandler(this)

        try {
            reader.parse(InputSource(inputStream))
        } catch (e: Exception) {
            return ParseResult.Failure(e)
        }

        return ParseResult.Success(characterSets.toMap(), combiningCodes.toMap())
    }

    override fun updateIsoCodeMaps(
        isoCode: IsoCode,
        characterSet: Map<Marc8Code, Char>,
        combiningCodes: List<Marc8Code>
    ) {
        characterSets[isoCode] = characterSet
        this.combiningCodes[isoCode] = combiningCodes
    }
}

sealed class ParseResult {
    data class Success(
        val characterSets: Map<IsoCode, Map<Marc8Code, Char>>,
        val combiningCodes: Map<IsoCode, List<Marc8Code>>
    ) : ParseResult()
    data class Failure(val error: Exception) : ParseResult()
}