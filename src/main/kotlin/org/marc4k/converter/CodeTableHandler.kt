package org.marc4k.converter

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.util.*

class CodeTableHandler(private val codeTableHandlerCallback: CodeTableHandlerCallback) : DefaultHandler() {
    private var currentCodeData: CodeData = CodeData(-1)

    private val currentCharacterSet = hashMapOf<Marc8Code, Char>()
    private val currentCombiningCodes = ArrayList<Marc8Code>()

    private val elementValueBuilder = StringBuilder()

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        elementValueBuilder.setLength(0)
        when (qName) {
            "characterSet" -> {
                currentCharacterSet.clear()
                currentCombiningCodes.clear()
                currentCodeData = CodeData(attributes?.getValue("ISOcode")!!.toInt(16))
            }
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        when (qName) {
            "characterSet" -> {
                codeTableHandlerCallback.updateIsoCodeMaps(currentCodeData.isoCode, currentCharacterSet.toMap(), currentCombiningCodes.toList())
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

    private class CodeData(val isoCode: IsoCode) {
        var marc8Code: Marc8Code = 0x00
        var ucs: Char = '\u0000'
        var isCombining = false
    }
}

interface CodeTableHandlerCallback {
    fun updateIsoCodeMaps(isoCode: IsoCode, characterSet: Map<Marc8Code, Char>, combiningCodes: List<Marc8Code>)
}