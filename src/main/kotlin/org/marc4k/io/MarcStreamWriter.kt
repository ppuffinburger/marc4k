package org.marc4k.io

import org.marc4k.FIELD_TERMINATOR
import org.marc4k.LEADER_LENGTH
import org.marc4k.MarcException
import org.marc4k.RECORD_TERMINATOR
import org.marc4k.converter.CharacterConverter
import org.marc4k.converter.CharacterConverterResult
import org.marc4k.marc.CustomDecimalFormat
import org.marc4k.marc.MarcRecord
import org.marc4k.marc.Record
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset

class MarcStreamWriter(private val output: OutputStream, private var encoding: String = "ISO-8859-1", private val allowOversizeRecord: Boolean = false, private val converter: CharacterConverter? = null) : MarcWriter {
    private var currentEncoding: String
    private var hasOversizeLength = false
    private var hasOversizeOffset = false

    init {
        encoding = parseEncoding(encoding)
        currentEncoding = encoding
    }

    override fun write(record: Record) {
        val recordToWrite = if (record is MarcRecord) record else MarcRecord().apply { copyFrom(record) }

        setCurrentEncoding(recordToWrite)

        hasOversizeLength = false
        hasOversizeOffset = false

        var previousDataLength = 0

        try {
            ByteArrayOutputStream().use { directory ->
                ByteArrayOutputStream().use { data ->
                    recordToWrite.controlFields.forEach { field ->
                        data.write(getDataBytes(field.data))
                        data.write(FIELD_TERMINATOR)
                        directory.write(getDirectoryEntry(field.tag, data.size() - previousDataLength, previousDataLength))
                        previousDataLength = data.size()
                    }

                    recordToWrite.dataFields.forEach { field ->
                        data.write(field.indicator1.toInt())
                        data.write(field.indicator2.toInt())
                        data.write(getDataBytes(field.getData()))
                        data.write(FIELD_TERMINATOR)
                        directory.write(getDirectoryEntry(field.tag, data.size() - previousDataLength, previousDataLength))
                        previousDataLength = data.size()
                    }

                    directory.write(FIELD_TERMINATOR)

                    val baseAddressOfData = LEADER_LENGTH + directory.size()
                    recordToWrite.leader.baseAddressOfData = FIVE_DIGIT_DECIMAL_FORMAT.format(baseAddressOfData).toInt()

                    val recordLength = baseAddressOfData + data.size() + 1
                    recordToWrite.leader.recordLength = FIVE_DIGIT_DECIMAL_FORMAT.format(recordLength).toInt()

                    if (!allowOversizeRecord && (baseAddressOfData > 99999 || recordLength > 99999 || hasOversizeOffset)) {
                        throw MarcException("Record is too long to be a valid MARC binary record, it's length would be $recordLength which is more than 99999 bytes")
                    }

                    if (!allowOversizeRecord && hasOversizeLength) {
                        throw MarcException("Record has field that is too long to be a valid MARC binary record. The maximum length for a field counting all of the sub-fields is 9999 bytes.")
                    }

                    output.write(recordToWrite.leader.getData().toByteArray(Charsets.ISO_8859_1))
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

    private fun parseEncoding(encoding: String): String {
        return when (encoding.toUpperCase()) {
            "ISO-8859-1", "ISO8859_1", "ISO_8859_1" -> "ISO-8859-1"
            "UTF8", "UTF-8" -> "UTF8"
            else -> encoding
        }
    }

    // TODO : not sure I like this.  Does UNIMARC (or others) follow LoC's leader for position 9?   Maybe I should check if record
    //  is a Marc21Record first and then apply this.  If you're dealing with a straight MarcRecord, maybe it shouldn't have a character
    //  coding scheme either and make the developer handle what is need without the writer automatically doing it.  It also mean that
    //  if you don't pass in a converter and you have a ' ' in position 9 then it just encodes in ISO-8859-1 instead of converting to
    //  MARC8.  This just seems really Marc21 specific.
    //  Need more research.
    private fun setCurrentEncoding(record: MarcRecord) {
        converter?.let {
            record.leader.characterCodingScheme = if (it.outputsUnicode()) 'a' else ' '
        }

        currentEncoding = if (encoding == ENCODING_DEFINED_BY_LEADER) if (record.leader.characterCodingScheme == 'a') "UTF-8" else "ISO-8859-1" else encoding
    }

    private fun getDataBytes(data: String): ByteArray {
        return if (converter != null) {
            when(val converterResult = converter.convert(data)) {
                is CharacterConverterResult.Success -> converterResult.conversion.toByteArray(Charset.forName(currentEncoding))
                is CharacterConverterResult.WithErrors -> {
                    throw MarcException("Character conversion resulted in errors: ${converterResult.errors}")
                }
            }
        } else {
            data.toByteArray(Charset.forName(currentEncoding))
        }
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
        private const val ENCODING_DEFINED_BY_LEADER = "PER_RECORD"
    }
}