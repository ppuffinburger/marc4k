package org.marc4k.io.converter

import org.marc4k.IsoCode
import org.marc4k.MarcException
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class ReverseCodeTableHandler(private val reverseCodeTableHandlerCallback: ReverseCodeTableHandlerCallback) : DefaultHandler() {
    private var currentIsoCode: IsoCode = -1
    private var currentCodeData: CodeData =
        CodeData()

    private val currentCharacterSet = mutableListOf<Pair<Char, CharArray>>()
    private val currentCombiningCharacters = mutableSetOf<Char>()

    private val elementValueBuilder = StringBuilder()

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        elementValueBuilder.setLength(0)
        when (qName) {
            "characterSet" -> {
                currentCharacterSet.clear()
                currentCombiningCharacters.clear()
                attributes?.getValue("ISOcode")?.let { currentIsoCode = it.toInt(16) }
                    ?: throw MarcException("CodeTable does not contain an ISOcode attribute")
            }
            "code" -> {
                currentCodeData = CodeData()
            }
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        when (qName) {
            "characterSet" -> {
                reverseCodeTableHandlerCallback.updateIsoCodeMaps(currentIsoCode, currentCharacterSet.toList(), currentCombiningCharacters.toSet())
            }
            "code" -> {
                if (currentCodeData.isCombining) {
                    currentCodeData.ucs?.let { currentCombiningCharacters.add(it) }
                    currentCodeData.altUcs?.let { currentCombiningCharacters.add(it) }

                }

                currentCodeData.ucs?.let {
                    currentCharacterSet.add(it to currentCodeData.marc8Characters.toCharArray())
                }
                currentCodeData.altUcs?.let {
                    currentCharacterSet.add(it to currentCodeData.marc8Characters.toCharArray())
                }
            }
            "marc", "ucs", "alt", "isCombining" -> {
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
            when (elementName) {
                "isCombining" -> currentCodeData.isCombining = elementValue.equals("true", true)
                "marc" -> {
                    if (elementValue.length == 6) {
                        currentCodeData.marc8Characters.add(elementValue.substring(0, 2).toInt(16).toChar())
                        currentCodeData.marc8Characters.add(elementValue.substring(2, 4).toInt(16).toChar())
                        currentCodeData.marc8Characters.add(elementValue.substring(4, 6).toInt(16).toChar())
                    } else {
                        currentCodeData.marc8Characters.add(elementValue.toInt(16).toChar())
                    }
                }
                "ucs" -> currentCodeData.ucs = elementValue.toInt(16).toChar()
                "alt" -> currentCodeData.altUcs = elementValue.toInt(16).toChar()
            }
        }
    }

    companion object {
        private class CodeData {
            var marc8Characters = mutableListOf<Char>()
            var ucs: Char? = null
            var altUcs: Char? = null
            var isCombining = false
        }
    }
}

interface ReverseCodeTableHandlerCallback {
    fun updateIsoCodeMaps(characterSet: IsoCode, characterSets: List<Pair<Char, CharArray>>, combiningCharacters: Set<Char>)
}