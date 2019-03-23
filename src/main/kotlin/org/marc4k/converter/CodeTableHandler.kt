package org.marc4k.converter

import org.marc4k.MarcException
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.util.*

class CodeTableHandler(private val codeTableHandlerCallback: CodeTableHandlerCallback) : DefaultHandler() {
    private var currentIsoCode: IsoCode = -1
    private var currentCodeData: CodeData = CodeData()

    private val currentCharacterSet = hashMapOf<Marc8Code, Char>()
    private val currentCombiningCodes = ArrayList<Marc8Code>()

    private val elementValueBuilder = StringBuilder()

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        elementValueBuilder.setLength(0)
        when (qName) {
            "characterSet" -> {
                currentCharacterSet.clear()
                currentCombiningCodes.clear()
                attributes?.getValue("ISOcode")?.let {
                    currentIsoCode = it.toInt(16)
                } ?: throw MarcException("CodeTable does not contain an ISOcode attribute")
            }
            "code" -> {
                currentCodeData = CodeData()
            }
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        when (qName) {
            "characterSet" -> {
                codeTableHandlerCallback.updateIsoCodeMaps(currentIsoCode, currentCharacterSet.toMap(), currentCombiningCodes.toList())
            }
            "code" -> {
                currentCharacterSet[currentCodeData.marc8Code] = currentCodeData.ucs
                if (currentCodeData.isCombining) {
                    currentCombiningCodes.add(currentCodeData.marc8Code)
                }
            }
            "marc", "ucs", "isCombining" -> {
                updateCurrentCode(qName)
            }
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        elementValueBuilder.append(ch, start, length)
    }

    private fun updateCurrentCode(elementName: String) {
        val elementValue = elementValueBuilder.toString()

        if (elementValue.isNotEmpty()) {
            if (elementName == "isCombining") {
                currentCodeData.isCombining = elementValue.equals("true", true)
            } else {
                val intValue = Integer.valueOf(elementValue, 16)
                when (elementName) {
                    "marc" -> currentCodeData.marc8Code = intValue
                    "ucs" -> currentCodeData.ucs = intValue.toChar()
                }
            }
        }
    }

    private class CodeData {
        var marc8Code: Marc8Code = 0x00
        var ucs: Char = '\u0000'
        var isCombining = false
    }
}

interface CodeTableHandlerCallback {
    fun updateIsoCodeMaps(isoCode: IsoCode, characterSet: Map<Marc8Code, Char>, combiningCodes: List<Marc8Code>)
}