package org.marc4k.io.marcxml

import org.marc4k.MarcException
import org.marc4k.io.MarcWriter
import org.marc4k.io.converter.CharacterConverter
import org.marc4k.io.converter.CharacterConverterResult
import org.marc4k.marc.MarcRecord
import org.marc4k.marc.Record
import org.marc4k.marc.marc21.authority.AuthorityRecord
import org.marc4k.marc.marc21.bibliographic.BibliographicRecord
import org.marc4k.marc.marc21.classification.ClassificationRecord
import org.marc4k.marc.marc21.community.CommunityRecord
import org.marc4k.marc.marc21.holdings.HoldingsRecord
import org.marc4k.unicodeToHex
import org.xml.sax.SAXException
import org.xml.sax.helpers.AttributesImpl
import java.io.*
import java.nio.charset.UnsupportedCharsetException
import java.text.Normalizer
import java.util.regex.Pattern
import javax.xml.transform.OutputKeys
import javax.xml.transform.Result
import javax.xml.transform.Source
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.sax.TransformerHandler
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * A [MarcWriter] for writing MARCXML that supports stylesheets for writing other XML based
 * formats such as MODS.
 */
class MarcXmlWriter : MarcWriter {
    private lateinit var handler: TransformerHandler
    private var writer: Writer? = null
    private var indent = false
    private var encoding = "UTF8"
    private var writeCollectionElement = true

    /**
     * true if output will compose Unicode with NFC.  Defaults to false.
     */
    var normalizeUnicode = false

    /**
     * true if replacing characters that are not valid in XML with <U+####> format.  Defaults to false.
     */
    var replaceNonXmlCharacters = false

    /**
     * Optional [CharacterConverter] that will be used to transform data when writing.  Defaults to null.
     */
    var converter: CharacterConverter? = null

    private constructor()

    /**
     * Instantiates class with given [outputStream] and starts writing the document using the given [encoding].
     *
     * @property[indent] true if indentation is wanted in the document.   Defaults to false.
     */
    constructor(outputStream: OutputStream, encoding: String = "UTF8", indent: Boolean = false) {
        this.encoding = encoding
        this.indent = indent

        try {
            writer = BufferedWriter(OutputStreamWriter(outputStream, encoding))
            handler = createHandler(StreamResult(writer), null)

            writeStartDocument()
        } catch (e: UnsupportedCharsetException) {
            throw MarcException(e.message, e)
        }
    }

    /**
     * Instantiates class with given [result] and starts writing the document.
     */
    constructor(result: Result) {
        handler = createHandler(result, null)
        writeStartDocument()
    }

    /**
     * Instantiates class with given [result] and [styleSheet] and starts writing the document.
     */
    constructor(result: Result, styleSheet: String) : this(result = result, styleSheet = StreamSource(styleSheet))

    /**
     * Instantiates class with given [result] and [styleSheet] and starts writing the document.
     */
    constructor(result: Result, styleSheet: Source) {
        handler = createHandler(result, styleSheet)
        writeStartDocument()
    }

    /**
     * Writes [record] to underlying [handler].
     */
    override fun write(record: Record) {
        try {
            createXml(record)
        } catch (e: SAXException) {
            throw MarcException("SAX error occurred while writing record", e)
        }
    }

    /**
     * Writes the end of document and closes the underlying [writer], if used.
     */
    override fun close() {
        writeEndDocument()

        try {
            writer?.close()
        } catch (e: IOException) {
            throw MarcException(e.message, e)
        }
    }

    private fun createHandler(result: Result, styleSheet: Source?): TransformerHandler {
        try {
            val factory = SAXTransformerFactory.newInstance() as SAXTransformerFactory

            val handler = if (styleSheet == null) {
                factory.newTransformerHandler()
            } else {
                factory.newTransformerHandler(styleSheet)
            }

            handler.transformer.setOutputProperty(OutputKeys.METHOD, "xml")

            if (indent) {
                handler.transformer.setOutputProperty(OutputKeys.INDENT, "yes")
                handler.transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

                // adding this because of https://bugs.openjdk.java.net/browse/JDK-7150637
                handler.transformer.setOutputProperty("http://www.oracle.com/xml/is-standalone", "yes")
            }

            handler.setResult(result)

            return handler
        } catch (e: Exception) {
            throw MarcException(e.message, e)
        }
    }

    private fun writeStartDocument() {
        try {
            with(handler) {
                startDocument()
                startPrefixMapping(MARCXML_NS_PREFIX, MARCXML_NS_URI)

                if (writeCollectionElement) {
                    startElement(MARCXML_NS_URI, COLLECTION, "$MARCXML_NS_PREFIX:$COLLECTION", AttributesImpl())
                }
            }
        } catch (e: SAXException) {
            throw MarcException("SAX error occurred while writing start document", e)
        }
    }

    private fun writeEndDocument() {
        try {
            with(handler) {
                if (writeCollectionElement) {
                    endElement(MARCXML_NS_URI, COLLECTION, "$MARCXML_NS_PREFIX:$COLLECTION")
                }

                endPrefixMapping(MARCXML_NS_PREFIX)
                endDocument()
            }
        } catch (e: SAXException) {
            throw MarcException("SAX error occurred while writing end document", e)
        }
    }

    private fun createXml(record: Record) {
        with(handler) {
            val recordAttributes = getRecordType(record)?.let { recordType ->
                AttributesImpl().apply {
                    addAttribute("", RECORD_TYPE, RECORD_TYPE, "", recordType)
                }
            } ?: EMPTY_ATTRIBUTES

            startElement(MARCXML_NS_URI, RECORD, "$MARCXML_NS_PREFIX:$RECORD", recordAttributes)

            startElement(MARCXML_NS_URI, LEADER, "$MARCXML_NS_PREFIX:$LEADER", EMPTY_ATTRIBUTES)

            val leader = getDataElementArray(record.leader.getData())
            characters(leader, 0, leader.size)

            endElement(MARCXML_NS_URI, LEADER, "$MARCXML_NS_PREFIX:$LEADER")

            for (controlField in record.controlFields) {
                val controlFieldAttributes = AttributesImpl().apply {
                    addAttribute("", TAG, TAG, "CDATA", getDataElementString(controlField.tag))
                }

                startElement(MARCXML_NS_URI, CONTROL_FIELD, "$MARCXML_NS_PREFIX:$CONTROL_FIELD", controlFieldAttributes)

                val fieldData = getDataElementArray(controlField.data)
                characters(fieldData, 0, fieldData.size)

                endElement(MARCXML_NS_URI, CONTROL_FIELD, "$MARCXML_NS_PREFIX:$CONTROL_FIELD")
            }

            for (dataField in record.dataFields) {
                val dataFieldAttributes = AttributesImpl().apply {
                    addAttribute("", TAG, TAG, "CDATA", getDataElementString(dataField.tag))
                    addAttribute("", INDICATOR_1, INDICATOR_1, "CDATA", getDataElementString(dataField.indicator1.toString()))
                    addAttribute("", INDICATOR_2, INDICATOR_2, "CDATA", getDataElementString(dataField.indicator2.toString()))
                }

                startElement(MARCXML_NS_URI, DATA_FIELD, "$MARCXML_NS_PREFIX:$DATA_FIELD", dataFieldAttributes)

                for (subfield in dataField.subfields) {
                    val subfieldAttributes = AttributesImpl().apply {
                        addAttribute("", SUBFIELD_CODE, SUBFIELD_CODE, "CDATA", getDataElementString(subfield.name.toString()))
                    }

                    startElement(MARCXML_NS_URI, SUBFIELD, "$MARCXML_NS_PREFIX:$SUBFIELD", subfieldAttributes)

                    val fieldData = getDataElementArray(subfield.data)
                    characters(fieldData, 0, fieldData.size)

                    endElement(MARCXML_NS_URI, SUBFIELD, "$MARCXML_NS_PREFIX:$SUBFIELD")
                }

                endElement(MARCXML_NS_URI, DATA_FIELD, "$MARCXML_NS_PREFIX:$DATA_FIELD")
            }

            endElement(MARCXML_NS_URI, RECORD, "$MARCXML_NS_PREFIX:$RECORD")
        }
    }

    // TODO : do I want to do the MarcRecord part?  I really don't know if it would be correct outside of MARC21
    private fun getRecordType(record: Record): String? {
        return when (record) {
            is BibliographicRecord -> "Bibliographic"
            is HoldingsRecord -> "Holdings"
            is AuthorityRecord -> "Authority"
            is ClassificationRecord -> "Classification"
            is CommunityRecord -> "Community"
            is MarcRecord -> {
                return when (record.leader.typeOfRecord) {
                    'a', 'c', 'd', 'e', 'f', 'g', 'i', 'j', 'k', 'm', 'n', 'o', 'p', 'r', 't' -> "Bibliographic"
                    'u', 'v', 'x', 'y' -> "Holdings"
                    'z' -> "Authority"
                    'w' -> "Classification"
                    'q' -> "Community"
                    else -> null
                }
            }
            else -> null
        }
    }

    private fun getDataElementArray(data: String): CharArray {
        return getDataElementString(data).toCharArray()
    }

    private fun getDataElementString(data: String): String {
        var dataElement = converter?.let {
            when(val converterResult = it.convert(data)) {
                is CharacterConverterResult.Success -> converterResult.conversion
                is CharacterConverterResult.WithErrors -> {
                    throw MarcException("Character conversion resulted in errors: ${converterResult.errors}")
                }
            }
        } ?: data

        if (normalizeUnicode) {
            dataElement = Normalizer.normalize(dataElement, Normalizer.Form.NFC)
        }

        if (replaceNonXmlCharacters) {
            dataElement = with(StringBuilder(dataElement.length)) {
                for (character in dataElement) {
                    if (INVALID_XML_CHARACTER_PATTERN.matcher(character.toString()).matches()) {
                        append(unicodeToHex(character))
                    } else {
                        append(character)
                    }
                }

                toString()
            }
        }

        return dataElement
    }

    companion object {
        private const val MARCXML_NS_PREFIX = "marc"
        private const val MARCXML_NS_URI = "http://www.loc.gov/MARC21/slim"
        private const val COLLECTION = "collection"
        private const val RECORD = "record"
        private const val RECORD_TYPE = "type"
        private const val LEADER = "leader"
        private const val TAG = "tag"
        private const val CONTROL_FIELD = "controlfield"
        private const val DATA_FIELD = "datafield"
        private const val INDICATOR_1 = "ind1"
        private const val INDICATOR_2 = "ind2"
        private const val SUBFIELD = "subfield"
        private const val SUBFIELD_CODE = "code"
        private val EMPTY_ATTRIBUTES = AttributesImpl()
        private val INVALID_XML_CHARACTER_PATTERN = Pattern.compile("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\\x{10000}-\\x{10FFFF}]")

        /**
         * Convenience method to write a single [record] to an [outputStream].
         *
         * @property[indent] true if indentation is wanted in the document.   Defaults to false.
         * @property[converter] that will be used to transform data when writing.  Defaults to null.
         */
        fun writeSingleRecord(record: Record, outputStream: OutputStream, indent: Boolean = false, converter: CharacterConverter? = null) {
            try {
                BufferedWriter(OutputStreamWriter(outputStream)).use { bufferedWriter ->
                    MarcXmlWriter().apply {
                        this.indent = indent
                        this.converter = converter
                        normalizeUnicode = true
                        writeCollectionElement = false
                    }.use { writer ->
                        writer.handler = writer.createHandler(StreamResult(bufferedWriter), null)

                        writer.writeStartDocument()
                        writer.write(record)
                        writer.writeEndDocument()
                    }
                }
            } catch (se: SAXException) {
                throw MarcException("SAX error occurred while writing record", se)
            }
        }
    }
}