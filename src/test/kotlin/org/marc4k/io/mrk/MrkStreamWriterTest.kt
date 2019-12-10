package org.marc4k.io.mrk

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.marc4k.io.NewMarcStreamReader
import org.marc4k.io.codec.Marc21DataDecoder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.text.Collator
import kotlin.test.assertTrue

internal class MrkStreamWriterTest {
    private val collator = Collator.getInstance()

    @Test
    fun `test write MRK record`() {
        val marcRecord = NewMarcStreamReader(javaClass.getResourceAsStream("/records/MARC8_auth_record.mrc"), Marc21DataDecoder()).use {
            it.next()
        }

        val outputStream = ByteArrayOutputStream()
        MrkStreamWriter(outputStream, false).use {
            it.write(marcRecord)
        }

        val mrkRecord = MrkStreamReader(ByteArrayInputStream(outputStream.toByteArray())).use {
            it.next()
        }

        assertThat(mrkRecord.controlFields).isNotEmpty
        assertThat(mrkRecord.dataFields).isNotEmpty

        for ((index, mrkControlField) in mrkRecord.controlFields.withIndex()) {
            assertTrue { collator.equals(mrkControlField.toString(), marcRecord.controlFields[index].toString()) }
        }

        for ((index, mrkDataField) in mrkRecord.dataFields.withIndex()) {
            assertTrue { collator.equals(mrkDataField.toString(), marcRecord.dataFields[index].toString()) }
        }
    }


    @Test
    fun `test write MRK8 record`() {
        val marcRecord = NewMarcStreamReader(javaClass.getResourceAsStream("/records/UTF8_auth_record.mrc"), Marc21DataDecoder()).use {
            it.next()
        }

        val outputStream = ByteArrayOutputStream()
        MrkStreamWriter(outputStream).use {
            it.write(marcRecord)
        }

        val mrkRecord = MrkStreamReader(ByteArrayInputStream(outputStream.toByteArray())).use {
            it.next()
        }

        assertThat(mrkRecord.controlFields).isNotEmpty
        assertThat(mrkRecord.dataFields).isNotEmpty

        for ((index, mrkControlField) in mrkRecord.controlFields.withIndex()) {
            assertTrue { collator.equals(mrkControlField.toString(), marcRecord.controlFields[index].toString()) }
        }

        for ((index, mrkDataField) in mrkRecord.dataFields.withIndex()) {
            assertTrue { collator.equals(mrkDataField.toString(), marcRecord.dataFields[index].toString()) }
        }
    }
}