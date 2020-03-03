package org.marc4k.io

import org.marc4k.*
import org.marc4k.io.codec.DefaultMarcDataEncoder
import org.marc4k.io.codec.MarcDataEncoder
import org.marc4k.marc.CustomDecimalFormat
import org.marc4k.marc.MarcRecord
import org.marc4k.marc.Record
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * A [MarcWriter] that writes a [Record] to an [OutputStream] using a [MarcDataEncoder] to handle character conversions.
 *
 * @property[output] the [OutputStream] to write the MARC data to.
 * @property[allowOversizeRecord] true if allowing oversize records to be written.  Defaults to false.
 * @property[encoder] the [MarcDataEncoder] used to transform the character data.  Defaults is a [DefaultMarcDataEncoder].
 */
class NewMarcStreamWriter(private val output: OutputStream, private val allowOversizeRecord: Boolean = false, private val encoder: MarcDataEncoder = DefaultMarcDataEncoder()) : MarcWriter {
    private var hasOversizeLength = false
    private var hasOversizeOffset = false

    /**
     * Writes a [Record] to the underlying [OutputStream].
     *
     * @throws[MarcException] if an [IOException] occurs or an oversize record occurs and is not allowed.
     */
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

    /**
     * Closes the underlying [OutputStream].
     */
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