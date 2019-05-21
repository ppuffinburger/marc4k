package org.marc4k.io

import org.marc4k.*
import org.marc4k.converter.CharacterConverter
import org.marc4k.converter.CharacterConverterResult
import org.marc4k.converter.marc8.UnicodeToMarc8
import org.marc4k.marc.CustomDecimalFormat
import org.marc4k.marc.MarcRecord
import org.marc4k.marc.Record
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class NewMarcStreamWriter(private val output: OutputStream, private val allowOversizeRecord: Boolean = false, private val encoder: MarcDataEncoder = DefaultMarcDataEncoder()) : MarcWriter {
    private var hasOversizeLength = false
    private var hasOversizeOffset = false

    override fun write(record: Record) {
        val recordToWrite = encoder.createIso2709Record(if (record is MarcRecord) record else MarcRecord().apply { copyFrom(record) })

        hasOversizeLength = false
        hasOversizeOffset = false

        var previousDataLength = 0

        try {
            ByteArrayOutputStream().use { directory ->
                ByteArrayOutputStream().use { data ->
                    recordToWrite.controlFields.forEach { field ->
                        data.write(field.data)
                        data.write(FIELD_TERMINATOR)
                        directory.write(getDirectoryEntry(field.tag, data.size() - previousDataLength, previousDataLength))
                        previousDataLength = data.size()
                    }

                    recordToWrite.dataFields.forEach { field ->
                        data.write(field.indicator1.toInt())
                        data.write(field.indicator2.toInt())
                        field.subfields.forEach { subfield ->
                            data.write(SUBFIELD_DELIMITER)
                            data.write(subfield.name.toInt())
                            data.write(subfield.data)
                        }
                        data.write(FIELD_TERMINATOR)
                        directory.write(getDirectoryEntry(field.tag, data.size() - previousDataLength, previousDataLength))
                        previousDataLength = data.size()
                    }

                    directory.write(FIELD_TERMINATOR)

                    val baseAddressOfData = LEADER_LENGTH + directory.size()
                    val recordLength = baseAddressOfData + data.size() + 1

                    if (!allowOversizeRecord && (baseAddressOfData > 99999 || recordLength > 99999 || hasOversizeOffset)) {
                        throw MarcException("Record is too long to be a valid MARC binary record, it's length would be $recordLength which is more than 99999 bytes")
                    }

                    if (!allowOversizeRecord && hasOversizeLength) {
                        throw MarcException("Record has field that is too long to be a valid MARC binary record. The maximum length for a field counting all of the sub-fields is 9999 bytes.")
                    }

                    val newLeaderBytes = with(StringBuilder()) {
                        append(FIVE_DIGIT_DECIMAL_FORMAT.format(recordLength))
                        append(recordToWrite.leader.substring(5..11))
                        append(FIVE_DIGIT_DECIMAL_FORMAT.format(baseAddressOfData))
                        append(recordToWrite.leader.substring(17))
                        toString().toByteArray(Charsets.ISO_8859_1)
                    }

                    output.write(newLeaderBytes)
                    output.write(directory.toByteArray())
                    output.write(data.toByteArray())
                    output.write(RECORD_TERMINATOR)
                }
            }
        } catch (ioe: IOException) {
            throw MarcException("IO Error occurred while writing record", ioe)
        } catch (me: MarcException) {
            throw me
        }
    }

    override fun close() {
        output.close()
    }

    private fun getDirectoryEntry(tag: String, length: Int, start: Int): ByteArray {
        if (length > 9999) {
            hasOversizeLength = true
        }

        if (start > 99999) {
            hasOversizeOffset = true
        }

        return "$tag${FOUR_DIGIT_DECIMAL_FORMAT.format(length)}${FIVE_DIGIT_DECIMAL_FORMAT.format(start)}".toByteArray(Charsets.ISO_8859_1)
    }

    companion object {
        private val FOUR_DIGIT_DECIMAL_FORMAT = CustomDecimalFormat(4)
        private val FIVE_DIGIT_DECIMAL_FORMAT = CustomDecimalFormat(5)
    }
}

abstract class MarcDataEncoder {
    protected var applyConverter = false

    protected abstract fun setApplyConverter(marcRecord: MarcRecord): Boolean
    protected abstract fun getDataAsBytes(data: String): ByteArray

    fun createIso2709Record(marcRecord: MarcRecord): Iso2709Record {
        applyConverter = setApplyConverter(marcRecord)

        val iso2709Record = Iso2709Record(marcRecord.leader.getData())

        iso2709Record.controlFields +=
            marcRecord.controlFields.map { field ->
                Iso2709ControlField(field.tag, getDataAsBytes(field.data))
            }

        iso2709Record.dataFields +=
            marcRecord.dataFields.map { field ->
                Iso2709DataField(field.tag, field.indicator1, field.indicator2).apply {
                    subfields += field.subfields.map { subfield ->
                        Iso2709Subfield(subfield.name, getDataAsBytes(subfield.data))
                    }
                }
            }

        return iso2709Record
    }
}

class DefaultMarcDataEncoder(private var encoding: String = ISO_8859_1, private val converter: CharacterConverter? = null) : MarcDataEncoder() {
    init {
        encoding = parseEncoding(encoding)
    }

    override fun setApplyConverter(marcRecord: MarcRecord): Boolean = true

    override fun getDataAsBytes(data: String): ByteArray {
        if (converter != null && applyConverter) {
            return when (val converterResult = converter.convert(data)) {
                is CharacterConverterResult.Success -> converterResult.conversion.toByteArray(Charset.forName(encoding))
                is CharacterConverterResult.WithErrors -> {
                    throw MarcException("Character conversion resulted in errors: ${converterResult.errors}")
                }
            }
        } else {
            return when (encoding) {
                UTF_8 -> {
                    data.toByteArray(Charsets.UTF_8)
                }
                ISO_8859_1 -> {
                    data.toByteArray(Charsets.ISO_8859_1)
                }
                else -> {
                    try {
                        data.toByteArray(Charset.forName(encoding))
                    } catch (e: UnsupportedEncodingException) {
                        throw MarcException("Unsupported encoding", e)
                    }
                }
            }
        }
    }

    private fun parseEncoding(encoding: String): String {
        return when (encoding.toUpperCase()) {
            "ISO-8859-1", "ISO8859_1", "ISO_8859_1" -> ISO_8859_1
            "UTF8", "UTF-8" -> UTF_8
            else -> encoding
        }
    }
}

class Marc21DataEncoder(private val converter: UnicodeToMarc8 = UnicodeToMarc8()) : MarcDataEncoder() {
    override fun setApplyConverter(marcRecord: MarcRecord): Boolean {
        return marcRecord.leader.implementationDefined1[2] == MARC8_SCHEME_CHARACTER
    }

    override fun getDataAsBytes(data: String): ByteArray {
        return if (applyConverter) {
            when (val converterResult = converter.convert(data)) {
                is CharacterConverterResult.Success -> converterResult.conversion.toByteArray(Charsets.ISO_8859_1)
                is CharacterConverterResult.WithErrors -> {
                    throw MarcException("Character conversion resulted in errors: ${converterResult.errors}")
                }
            }
        } else {
            data.toByteArray(Charsets.UTF_8)
        }
    }

    companion object {
        private const val MARC8_SCHEME_CHARACTER = ' '
    }
}