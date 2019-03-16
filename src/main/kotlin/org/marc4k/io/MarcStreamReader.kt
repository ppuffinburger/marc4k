package org.marc4k.io

import org.marc4k.MarcException
import org.marc4k.converter.Marc8ToUnicode
import org.marc4k.marc.*
import java.io.*
import java.nio.charset.Charset
import java.util.function.Consumer

class MarcStreamReader(input: InputStream, private var encoding: String = "ISO-8859-1") : MarcReader {
    private val input: DataInputStream = DataInputStream(if (input.markSupported()) input else BufferedInputStream(input))
    private val overrideEncoding: Boolean
    private lateinit var marc8ToUnicode: Marc8ToUnicode

    init {
        encoding = parseEncoding(encoding)
        overrideEncoding = encoding != "ISO-8859-1"
    }

    override fun hasNext(): Boolean {
        try {
            input.mark(10)
            if (input.read() == -1) {
                return false
            }
            input.reset()
        } catch (e: IOException) {
            throw MarcException(e.message, e)
        }

        return true
    }

    override fun next(): MarcRecord {
        try {
            input.mark(LEADER_RECORD_LENGTH_LENGTH)
            val recordLengthBytes = ByteArray(LEADER_RECORD_LENGTH_LENGTH)
            input.readFully(recordLengthBytes)
            input.reset()

            val recordBytes = ByteArray(parseRecordLength(recordLengthBytes))
            input.readFully(recordBytes)

            return parseRecord(recordBytes)
        } catch (e: EOFException) {
            throw MarcException("Premature end of file encountered", e)
        } catch (e: IOException) {
            throw MarcException("An error occurred reading input", e)
        }
    }

    override fun forEachRemaining(action: Consumer<in Record>) {
        while (hasNext()) action.accept(next())
    }

    override fun close() {
        input.close()
    }

    private fun parseEncoding(encoding: String): String {
        return when (encoding.toUpperCase()) {
            "MARC8", "MARC-8" -> "MARC8"
            "ISO-8859-1", "ISO8859_1", "ISO_8859_1" -> "ISO-8859-1"
            "UTF8", "UTF-8" -> "UTF8"
            else -> encoding
        }
    }

    private fun parseRecordLength(recordLengthBytes: ByteArray): Int {
        return try {
            recordLengthBytes.toString(Charsets.ISO_8859_1).toInt()
        } catch (e: Exception) {
            throw MarcException("Unable to parse record length", e)
        }
    }

    private fun parseNumber(bytes: ByteArray): Int {
        return try {
            bytes.toString(Charsets.ISO_8859_1).toInt()
        } catch (e: NumberFormatException) {
            throw MarcException("Unable to parse number from bytes", e)
        }
    }

    private fun parseRecord(recordBytes: ByteArray): MarcRecord {
        val record = MarcRecord().apply {
            leader.setData(recordBytes.copyOfRange(0, LEADER_LENGTH).toString(Charsets.ISO_8859_1))
        }

        // if MARC21 check position 09 for encoding and override
        when (record.leader.characterCodingScheme) {
            ' ' -> if (!overrideEncoding) encoding = "ISO-8859-1"
            'a' -> if (!overrideEncoding) encoding = "UTF8"
        }

        val directoryEntries = parseDirectory(recordBytes.copyOfRange(LEADER_LENGTH, record.leader.baseAddressOfData - 1))

        if (recordBytes[record.leader.baseAddressOfData - 1] != FIELD_TERMINATOR_BYTE) {
            throw MarcException("Expected field terminator at end of directory")
        }

        parseFields(recordBytes.copyOfRange(record.leader.baseAddressOfData, recordBytes.lastIndex), directoryEntries, record)

        if (recordBytes.last() != RECORD_TERMINATOR_BYTE) {
            throw MarcException("Expected record terminator at end of record")
        }

        return record
    }

    private fun parseDirectory(directoryBytes: ByteArray): ArrayList<Triple<String, Int, Int>> {
        if (directoryBytes.size % DIRECTORY_ENTRY_LENGTH != 0) {
            throw MarcException("Invalid directory")
        }

        val entries = ArrayList<Triple<String, Int, Int>>(directoryBytes.size / DIRECTORY_ENTRY_LENGTH)
        for (offset in 0 until directoryBytes.size step DIRECTORY_ENTRY_LENGTH) {
            entries.add(Triple(
                directoryBytes.copyOfRange(offset, offset + 3).toString(Charsets.ISO_8859_1),
                parseNumber(directoryBytes.copyOfRange(offset + 3, offset + 7)),
                parseNumber(directoryBytes.copyOfRange(offset + 7, offset + 12))
            ))
        }

        // Sort the entries in case they are out of order.   The reader will still correctly read the record, but the
        // fields will be out of order, otherwise.
        entries.sortBy { it.third }

        return entries
    }

    private fun parseFields(recordBytes: ByteArray, directoryEntries: ArrayList<Triple<String, Int, Int>>, record: Record) {
        for (entry in directoryEntries) {
            if (entry.first.startsWith("00")) {
                if (recordBytes[entry.second + entry.third - 1] != FIELD_TERMINATOR_BYTE) {
                    throw MarcException("Expected field terminator at end of field")
                }
                record.controlFields.add(ControlField(entry.first, getDataAsString(recordBytes.copyOfRange(entry.third, entry.third + entry.second - 1))))
            } else {
                val indicator1 = recordBytes[entry.third].toChar()
                val indicator2 = recordBytes[entry.third + 1].toChar()
                try {
                    record.dataFields.add(DataField(entry.first, indicator1, indicator2).apply {
                        subfields.addAll(
                            parseSubfields(
                                recordBytes.copyOfRange(
                                    entry.third + 2,
                                    entry.third + entry.second
                                )
                            )
                        )
                    })
                } catch (e: Exception) {
                    if (e is IndexOutOfBoundsException) {
                        throw MarcException("Error parsing data field for tag ${entry.first} because of trying to read beyond end of record.")
                    } else {
                        throw MarcException("Error parsing data field for tag ${entry.first} with data: ${recordBytes.copyOfRange(entry.third + 2, entry.third + entry.second).toString(Charsets.ISO_8859_1)}")
                    }
                }
            }
        }
    }

    private fun parseSubfields(subfieldsBytes: ByteArray): ArrayList<Subfield> {
        val subfields = arrayListOf<Subfield>()

        subfieldsBytes.inputStream().apply {
            loop@ while (true) {
                when (read()) {
                    -1 -> break@loop
                    SUBFIELD_DELIMITER -> {
                        val name = read()
                        if (name == -1) {
                            throw MarcException("Unexpected end of data field")
                        }

                        if (name != FIELD_TERMINATOR) {
                            val data = ByteArray(getSubfieldLength(this))
                            read(data)
                            subfields.add(Subfield(name.toChar(), getDataAsString(data)))
                        }
                    }
                }
            }
        }

        return subfields
    }

    private fun getSubfieldLength(stream: ByteArrayInputStream): Int {
        stream.mark(9999)
        var bytesRead = 0
        while (true) {
            when (stream.read()) {
                SUBFIELD_DELIMITER, FIELD_TERMINATOR -> {
                    stream.reset()
                    return bytesRead
                }
                -1 -> {
                    stream.reset()
                    throw MarcException("Subfield not terminated")
                }
                else -> bytesRead++
            }
        }
    }

    private fun getDataAsString(bytes: ByteArray) = when (encoding) {
        "MARC8" -> {
            if (!::marc8ToUnicode.isInitialized) {
                marc8ToUnicode = Marc8ToUnicode()
            }
            marc8ToUnicode.convert(bytes)
        }
        "UTF8" -> {
            bytes.toString(Charsets.UTF_8)
        }
        "ISO-8859-1" -> {
            bytes.toString(Charsets.ISO_8859_1)
        }
        else -> {
            try {
                bytes.toString(Charset.forName(encoding))
            } catch (e: UnsupportedEncodingException) {
                throw MarcException("Unsupported encoding", e)
            }
        }
    }

    companion object {
        private const val LEADER_LENGTH = 24
        private const val LEADER_RECORD_LENGTH_LENGTH = 5
        private const val DIRECTORY_ENTRY_LENGTH = 12
        private const val RECORD_TERMINATOR = 0x1D
        private const val FIELD_TERMINATOR = 0x1E
        private const val SUBFIELD_DELIMITER = 0x1F
        private const val RECORD_TERMINATOR_BYTE = RECORD_TERMINATOR.toByte()
        private const val FIELD_TERMINATOR_BYTE = FIELD_TERMINATOR.toByte()

        /*
            Eight characters are specifically designated as control characters for MARC 21 use:

            escape character, 1B(hex) in MARC-8 and Unicode encoding
            subfield delimiter, 1F(hex) in MARC-8 and Unicode encoding
            field terminator, 1E(hex) in MARC-8 and Unicode encoding
            record terminator, 1D(hex) in MARC-8 and Unicode encoding
            non-sorting character(s) begin, 88(hex) in MARC-8 and 98(hex) in Unicode encoding
            non-sorting character(s) end, 89(hex) in MARC-8 and 9C(hex) in Unicode encoding
            joiner, 8D(hex) in MARC-8 and 200D (hex) in Unicode encoding
            nonjoiner, 8E(hex) in MARC-8 and 200C (hex) in Unicode encoding.
         */
    }
}