package org.marc4k.io

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.marc4k.MarcException
import org.marc4k.converter.marc8.Marc8ToUnicode
import org.marc4k.converter.marc8.UnicodeToMarc8
import org.marc4k.marc.ControlField
import org.marc4k.marc.DataField
import org.marc4k.marc.MarcRecord
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

internal class MarcStreamWriterTest {
    @Test
    fun `test with ISO-8859-5 encoding`() {
        val originalRecord = getRecordFromStreamWithEncoding(javaClass.getResourceAsStream("/records/ISO-8859-5_bib_record.mrc"), "ISO-8859-5")
        val writerArray = getByteArrayFromRecordWithEncoding(originalRecord, "ISO-8859-5")
        val newRecord = getRecordFromStreamWithEncoding(ByteArrayInputStream(writerArray), "ISO-8859-5")

        assertRecordDataIsTheSame(originalRecord, newRecord)
    }

    @Test
    fun `test with UTF8 encoding`() {
        val originalRecord = getRecordFromStreamWithEncoding(javaClass.getResourceAsStream("/records/UTF8_auth_record.mrc"), "UTF-8")
        val writerArray = getByteArrayFromRecordWithEncoding(originalRecord, "UTF-8")
        val newRecord = getRecordFromStreamWithEncoding(ByteArrayInputStream(writerArray), "UTF-8")

        assertRecordDataIsTheSame(originalRecord, newRecord)
    }

    @Test
    fun `test with MARC8 encoding`() {
        val originalRecord = getRecordFromMarc8Stream(javaClass.getResourceAsStream("/records/MARC8_auth_record.mrc"))
        val writerArray = getByteArrayFromRecordAsMarc8(originalRecord)
        val newRecord = getRecordFromMarc8Stream(ByteArrayInputStream(writerArray))

        assertRecordDataIsTheSame(originalRecord, newRecord)
    }

    @Test
    fun `test that exception is thrown when field is too large and oversize is not allowed`() {
        val record = MarcRecord().apply {
            val data = with(StringBuilder()) {
                val tenCharacters = "0123456789"
                for (index in 0..1000) {
                    append(tenCharacters)
                }
                toString()
            }
            controlFields.add(ControlField("001", data))
        }
        assertThatExceptionOfType(MarcException::class.java).isThrownBy { getByteArrayFromRecordWithEncoding(record, "UTF-8") }
    }

    @Test
    fun `test that record is written when field is too large and oversize is allowed`() {
        val record = MarcRecord().apply {
            val data = with(StringBuilder()) {
                val tenCharacters = "0123456789"
                for (index in 0..1000) {
                    append(tenCharacters)
                }
                toString()
            }
            controlFields.add(ControlField("001", data))
        }

        val byteArray =  ByteArrayOutputStream().use { outputStream ->
            MarcStreamWriter(outputStream, "UTF-8", true).use { writer ->
                writer.write(record)
            }

            outputStream.toByteArray()
        }

        assertThat(byteArray.copyOfRange(0, 36)).isEqualTo("10049     0000037       001999900000".toByteArray(Charsets.ISO_8859_1))
    }

    @Test
    fun `test that exception is thrown when record is too large and oversize is not allowed`() {
        val record = MarcRecord().apply {
            val data = with(StringBuilder()) {
                val tenCharacters = "0123456789"
                for (index in 0..100) {
                    append(tenCharacters)
                }
                toString()
            }
            for (index in 0..100) {
                controlFields.add(ControlField("001", data))
            }
        }
        assertThatExceptionOfType(MarcException::class.java).isThrownBy { getByteArrayFromRecordWithEncoding(record, "UTF-8") }
    }

    @Test
    fun `test that record is written when record is too large and oversize is allowed`() {
        val record = MarcRecord().apply {
            val data = with(StringBuilder()) {
                val tenCharacters = "0123456789"
                for (index in 0..100) {
                    append(tenCharacters)
                }
                toString()
            }
            for (index in 0..100) {
                controlFields.add(ControlField("001", data))
            }
        }

        val byteArray = ByteArrayOutputStream().use { outputStream ->
            MarcStreamWriter(outputStream, "UTF-8", true).use { writer ->
                writer.write(record)
            }

            outputStream.toByteArray()
        }

        assertThat(byteArray.copyOfRange(0, 36)).isEqualTo("99999     0001237       001101100000".toByteArray(Charsets.ISO_8859_1))
    }

    private fun getRecordFromStreamWithEncoding(inputStream: InputStream, encoding: String): MarcRecord {
        MarcStreamReader(inputStream, encoding).use { reader ->
            return reader.next()
        }
    }

    private fun getByteArrayFromRecordWithEncoding(record: MarcRecord, encoding: String): ByteArray {
        ByteArrayOutputStream().use { outputStream ->
            MarcStreamWriter(outputStream, encoding).use { writer ->
                writer.write(record)
            }

            return outputStream.toByteArray()
        }
    }

    private fun getRecordFromMarc8Stream(inputStream: InputStream): MarcRecord {
        MarcStreamReader(inputStream, converter = Marc8ToUnicode(true)).use { reader ->
            return reader.next()
        }
    }

    private fun getByteArrayFromRecordAsMarc8(record: MarcRecord): ByteArray {
        ByteArrayOutputStream().use { outputStream ->
            MarcStreamWriter(outputStream, converter = UnicodeToMarc8()).use { writer ->
                writer.write(record)
            }

            return outputStream.toByteArray()
        }
    }

    private fun assertRecordDataIsTheSame(originalRecord: MarcRecord, newRecord: MarcRecord) {
        assertAll(
            { assertThat(originalRecord.errors).isEmpty() },
            { assertThat(newRecord.errors).isEmpty() },
            { assertThat(newRecord.leader.getData().substring(5)).isEqualTo(originalRecord.leader.getData().substring(5)) },
            { assertControlFields(originalRecord.controlFields, newRecord.controlFields) },
            { assertDataFields(originalRecord.dataFields, newRecord.dataFields) }
        )
    }

    private fun assertControlFields(originalControlFields: MutableList<ControlField>, newControlFields: MutableList<ControlField>) {
        for ((index, controlField) in originalControlFields.withIndex()) {
            assertThat(newControlFields[index]).isEqualTo(controlField)
        }
    }

    private fun assertDataFields(originalDataFields: MutableList<DataField>, newDataFields: MutableList<DataField>) {
        for ((index, dataField) in originalDataFields.withIndex()) {
            assertThat(newDataFields[index]).isEqualTo(dataField)
        }
    }
}