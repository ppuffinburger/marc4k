package org.marc4k.io

import org.marc4k.*
import org.marc4k.io.codec.*
import org.marc4k.marc.MarcRecord
import org.marc4k.marc.Record
import java.io.*
import java.util.function.Consumer

class NewMarcStreamReader(input: InputStream, private val decoder: MarcDataDecoder = DefaultMarcDataDecoder()) : MarcReader {
    private val input: DataInputStream = DataInputStream(if (input.markSupported()) input else BufferedInputStream(input))

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
        val leaderBytes = recordBytes.copyOfRange(0, LEADER_LENGTH)
        val baseAddressOfData = leaderBytes.copyOfRange(12, 17).toString(Charsets.ISO_8859_1).toIntOrNull() ?: 0
        val directoryEntries = parseDirectory(recordBytes.copyOfRange(LEADER_LENGTH, baseAddressOfData - 1))

        if (recordBytes[baseAddressOfData - 1] != FIELD_TERMINATOR_BYTE) {
            throw MarcException("Expected field terminator at end of directory")
        }

        val iso2709Record = Iso2709Record(leaderBytes.toString(Charsets.ISO_8859_1)).apply {
            parseFields(recordBytes.copyOfRange(baseAddressOfData, recordBytes.lastIndex), directoryEntries, this)
        }

        if (recordBytes.last() != RECORD_TERMINATOR_BYTE) {
            throw MarcException("Expected record terminator at end of record")
        }

        return decoder.createMarcRecord(iso2709Record)
    }

    private fun parseDirectory(directoryBytes: ByteArray): ArrayList<Triple<String, Int, Int>> {
        if (directoryBytes.size % DIRECTORY_ENTRY_LENGTH != 0) {
            throw MarcException("Invalid directory")
        }

        val entries = ArrayList<Triple<String, Int, Int>>(directoryBytes.size / DIRECTORY_ENTRY_LENGTH)
        for (offset in directoryBytes.indices step DIRECTORY_ENTRY_LENGTH) {
            entries.add(
                Triple(
                    directoryBytes.copyOfRange(offset, offset + 3).toString(Charsets.ISO_8859_1),
                    parseNumber(directoryBytes.copyOfRange(offset + 3, offset + 7)),
                    parseNumber(directoryBytes.copyOfRange(offset + 7, offset + 12))
                )
            )
        }

        // Sort the entries in case they are out of order.   The reader will still correctly read the record, but the
        // fields will be out of order, otherwise.
        entries.sortBy { it.third }

        return entries
    }

    private fun parseFields(recordBytes: ByteArray, directoryEntries: ArrayList<Triple<String, Int, Int>>, iso2709Record: Iso2709Record) {
        for (entry in directoryEntries) {
            if (recordBytes[entry.second + entry.third - 1] != FIELD_TERMINATOR_BYTE) {
                throw MarcException("Expected field terminator at end of field")
            }

            try {
                if (entry.first.startsWith("00")) {
                    iso2709Record.controlFields.add(Iso2709ControlField(entry.first, recordBytes.copyOfRange(entry.third, entry.third + entry.second - 1)))
                } else {
                    val indicator1 = recordBytes[entry.third].toChar()
                    val indicator2 = recordBytes[entry.third + 1].toChar()
                    iso2709Record.dataFields.add(Iso2709DataField(entry.first, indicator1, indicator2).apply {
                        subfields.addAll(
                            parseSubfields(recordBytes.copyOfRange(entry.third + 2, entry.third + entry.second))
                        )
                    })
                }
            } catch (e: Exception) {
                if (e is IndexOutOfBoundsException) {
                    throw MarcException("Error parsing data field for tag ${entry.first} because of trying to read beyond end of record.")
                } else {
                    throw MarcException(
                        "Error parsing data field for tag ${entry.first} with data: ${recordBytes.copyOfRange(
                            entry.third + 2,
                            entry.third + entry.second
                        ).toString(Charsets.ISO_8859_1)}"
                    )
                }
            }
        }
    }

    private fun parseSubfields(subfieldsBytes: ByteArray): ArrayList<Iso2709Subfield> {
        val subfields = arrayListOf<Iso2709Subfield>()

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
                            subfields.add(Iso2709Subfield(name.toChar(), data))
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

    companion object {
        private const val LEADER_RECORD_LENGTH_LENGTH = 5
        private const val DIRECTORY_ENTRY_LENGTH = 12
        private const val RECORD_TERMINATOR_BYTE = RECORD_TERMINATOR.toByte()
        private const val FIELD_TERMINATOR_BYTE = FIELD_TERMINATOR.toByte()
    }
}