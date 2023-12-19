package org.marc4k.io.marcxml

import org.marc4k.MarcError
import org.marc4k.MarcException
import org.marc4k.marc.*
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class MarcXmlHandler(private val marcXmlHandlerCallback: MarcXmlHandlerCallback) : DefaultHandler() {
    private var currentRecord: Record? = null
    private var currentTag: String? = null
    private var currentDataField: DataField? = null
    private var currentSubfieldName: Char? = null
    private var previousTag = "N/A"
    private val elementValueBuilder = StringBuilder()

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        elementValueBuilder.setLength(0)

        when (val realName = getRealName(localName, qName)) {
            "collection" -> {}
            "record" -> {
                currentRecord = MarcRecord()
                previousTag = "N/A"
            }
            "leader" -> {}
            "controlfield" -> {
                currentTag = attributes?.getValue("tag")

                if (currentTag == null) {
                    addOrThrowError(ErrorType.CONTROL_FIELD, previousTag)
                }
            }
            "datafield" -> {
                val tagAttribute = attributes?.getValue("tag")
                val indicator1Attribute = attributes?.getValue("ind1")
                val indicator2Attribute = attributes?.getValue("ind2")

                tagAttribute?.let { tag ->
                    currentTag = tag

                    indicator1Attribute?.let { indicator1 ->
                        indicator2Attribute?.let { indicator2 ->
                            currentDataField = DataField(tag, getCharacterData(indicator1), getCharacterData(indicator2))
                        } ?: addOrThrowError(ErrorType.INDICATOR_2, currentTag)
                    } ?: addOrThrowError(ErrorType.INDICATOR_1, currentTag)
                } ?: addOrThrowError(ErrorType.DATA_FIELD, previousTag)
            }
            "subfield" -> {
                val nameAttribute = attributes?.getValue("code")

                nameAttribute?.let {
                    currentSubfieldName = getCharacterData(it)
                } ?: addOrThrowError(ErrorType.SUBFIELD, currentTag)
            }
            else -> { addOrThrowError(ErrorType.UNKNOWN, realName) }
        }

        previousTag = currentTag ?: "N/A"
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        when (getRealName(localName, qName)) {
            "collection" -> {}
            "record" -> {
                currentRecord?.let {
                    marcXmlHandlerCallback.recordRead(it)
                }
            }
            "leader" -> {
                currentRecord?.leader?.setData(elementValueBuilder.toString())
            }
            "controlfield" -> {
                currentTag?.let { tag ->
                    currentRecord?.controlFields?.add(ControlField(tag, elementValueBuilder.toString()))
                }
            }
            "datafield" -> {
                currentDataField?.let {
                    currentRecord?.dataFields?.add(it)
                }
                currentDataField = null
            }
            "subfield" -> {
                currentSubfieldName?.let {
                    currentDataField?.subfields?.add(Subfield(it, elementValueBuilder.toString()))
                }
            }
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        elementValueBuilder.appendRange(ch!!, start, start + length)
    }

    override fun endDocument() {
        marcXmlHandlerCallback.parseComplete()
    }

    private fun getRealName(localName: String?, qName: String?): String {
        val realName = if (localName?.isNotEmpty() == true) localName else if (qName?.isNotEmpty() == true) qName else ""
        return removeNameSpacePrefix(realName)
    }

    private fun removeNameSpacePrefix(elementName: String): String {
        return elementName.substringAfter(':')
    }

    private fun getCharacterData(data: String): Char {
        return if (data.isEmpty()) ' ' else data[0]
    }

    private fun addOrThrowError(errorType: ErrorType, replacement: String?) {
        PARSING_ERROR_MAP[errorType]?.let { (exceptionError, recordError) ->
            currentRecord?.errors?.add(MarcError.StructuralError(handleErrorReplacement(recordError, replacement)))
                ?: throw MarcException(handleErrorReplacement(exceptionError, replacement))
        } ?: throw MarcException("Unknown Parsing Error: $errorType")
    }

    private fun handleErrorReplacement(error: String, replacement: String?): String {
        return replacement?.let {
            error.replace(REPLACE_TEXT, it)
        } ?: error
    }

    companion object {
        private const val REPLACE_TEXT = "%REPLACE%"
        private val PARSING_ERROR_MAP = mapOf(
            ErrorType.CONTROL_FIELD to Pair("ControlField missing tag value, found outside a record element", "Missing tag element in ControlField after tag: $REPLACE_TEXT"),
            ErrorType.DATA_FIELD to Pair("DataField missing tag value, found outside a record element", "Missing tag element in DataField after tag:$REPLACE_TEXT"),
            ErrorType.INDICATOR_1 to Pair("DataField ($REPLACE_TEXT) missing first indicator, found outside a record element", "DataField ($REPLACE_TEXT) missing first indicator"),
            ErrorType.INDICATOR_2 to Pair("DataField ($REPLACE_TEXT) missing second indicator, found outside a record element", "DataField ($REPLACE_TEXT) missing second indicator"),
            ErrorType.SUBFIELD to Pair("Subfield in DataField ($REPLACE_TEXT) missing code attribute", "Subfield in DataField ($REPLACE_TEXT) missing code attribute"),
            ErrorType.UNKNOWN to Pair("Unexpected XML element: $REPLACE_TEXT", "Unexpected XML element: $REPLACE_TEXT")
        )

        private enum class ErrorType {
            CONTROL_FIELD,
            DATA_FIELD,
            INDICATOR_1,
            INDICATOR_2,
            SUBFIELD,
            UNKNOWN
        }
    }
}

interface MarcXmlHandlerCallback {
    fun recordRead(record: Record)
    fun parseComplete()
}