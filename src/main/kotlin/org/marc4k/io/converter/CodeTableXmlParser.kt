package org.marc4k.io.converter

import org.marc4k.IsoCode
import org.marc4k.MarcCode
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory

class CodeTableXmlParser : CodeTableHandlerCallback {
    private val characterSets = mutableMapOf<IsoCode, Map<MarcCode, Char>>()
    private val combiningCodes = mutableMapOf<IsoCode, List<MarcCode>>()

    fun parse(inputStream: InputStream): CodeTableParseResult {
        val reader = try {
            SAXParserFactory.newInstance().newSAXParser().xmlReader
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

    override fun updateIsoCodeMaps(isoCode: IsoCode, characterSet: Map<MarcCode, Char>, combiningCodes: List<MarcCode>) {
        characterSets[isoCode] = characterSet
        this.combiningCodes[isoCode] = combiningCodes
    }
}

sealed class CodeTableParseResult {
    data class Success(val characterSets: Map<IsoCode, Map<MarcCode, Char>>, val combiningCodes: Map<IsoCode, List<MarcCode>>) : CodeTableParseResult()
    data class Failure(val exception: Exception) : CodeTableParseResult()
}