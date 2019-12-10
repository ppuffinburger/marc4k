package org.marc4k.io.mrk

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.marc4k.io.NewMarcStreamReader
import org.marc4k.io.codec.Marc21DataDecoder
import java.text.Collator
import kotlin.test.assertTrue

internal class MrkStreamReaderTest {
    private val collator = Collator.getInstance()

    @Test
    fun `test read MRK record`() {
        val marcRecord = NewMarcStreamReader(javaClass.getResourceAsStream("/records/UTF8_auth_record.mrc"), Marc21DataDecoder()).use {
            it.next()
        }

        val mrkRecord = MrkStreamReader(javaClass.getResourceAsStream("/records/MRK_auth_record.mrk")).use {
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
    fun `test read MRK8 record`() {
        val marcRecord = NewMarcStreamReader(javaClass.getResourceAsStream("/records/UTF8_auth_record.mrc"), Marc21DataDecoder()).use {
            it.next()
        }

        val mrkRecord = MrkStreamReader(javaClass.getResourceAsStream("/records/MRK_auth_record.mrk8")).use {
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
    fun `test read MRK records with extra spaces`() {
        MrkStreamReader(javaClass.getResourceAsStream("/records/MRK_bib_records_with_extra_lines.mrk")).use {
            for ((index, given) in it.withIndex()) {
                when (index) {
                    0 -> {
                        assertThat(given.getControlNumber()).isEqualTo("tes96000001 ")
                        val field100 = given.dataFields.first { field -> field.tag == "100" }
                        val field245 = given.dataFields.first { field -> field.tag == "245" }
                        val field500 = given.dataFields.find { field -> field.tag == "500" && field.getData().contains("conversion of curly braces") } ?: throw Exception("Couldn't find 500 tag in record 1")
                        assertAll("MRK record 1 data was converted",
                            { assertThat(field100.indicator1).isEqualTo('2') },
                            { assertThat(field100.indicator2).isEqualTo(' ') },
                            { assertThat(field100.getData()).contains("Deer-Doe") },
                            { assertThat(field245.indicator1).isEqualTo('1') },
                            { assertThat(field245.indicator2).isEqualTo('0') },
                            { assertThat(field245.getData()).contains("test record number 1") },
                            { assertThat(field500.indicator1).isEqualTo(' ') },
                            { assertThat(field500.indicator2).isEqualTo(' ') },
                            { assertThat(field500.getData()).contains("({)") },
                            { assertThat(field500.getData()).contains("(})") }
                        )
                    }
                    1 -> {
                        assertThat(given.getControlNumber()).isEqualTo("tes96000002 ")
                        val field100 = given.dataFields.first { field -> field.tag == "100" }
                        val field245 = given.dataFields.first { field -> field.tag == "245" }
                        val field500 = given.dataFields.first { field -> field.tag == "500" }
                        assertAll("MRK record 2 data was converted",
                            { assertThat(field100.indicator1).isEqualTo('2') },
                            { assertThat(field100.indicator2).isEqualTo(' ') },
                            { assertThat(field100.getData()).contains("Deer-Doe") },
                            { assertThat(field245.indicator1).isEqualTo('1') },
                            { assertThat(field245.indicator2).isEqualTo('0') },
                            { assertThat(field245.getData()).contains("test record number 2") },
                            { assertThat(field500.indicator1).isEqualTo(' ') },
                            { assertThat(field500.indicator2).isEqualTo(' ') },
                            { assertThat(field500.getData()).contains("the uppercase Polish L in Łódź") }
                        )
                    }
                    2 -> {
                        assertThat(given.getControlNumber()).isEqualTo("tes96000003 ")
                        val field100 = given.dataFields.first { field -> field.tag == "100" }
                        val field245 = given.dataFields.first { field -> field.tag == "245" }
                        val field500 = given.dataFields.first { field -> field.tag == "500" }
                        assertAll("MRK record 3 data was converted",
                            { assertThat(field100.indicator1).isEqualTo('2') },
                            { assertThat(field100.indicator2).isEqualTo(' ') },
                            { assertThat(field100.getData()).contains("Deer-Doe") },
                            { assertThat(field245.indicator1).isEqualTo('1') },
                            { assertThat(field245.indicator2).isEqualTo('0') },
                            { assertThat(field245.getData()).contains("test record number 3") },
                            { assertThat(field500.indicator1).isEqualTo(' ') },
                            { assertThat(field500.indicator2).isEqualTo(' ') },
                            { assertThat(field500.getData()).contains("the uppercase Polish L in Łódź") }
                        )
                    }
                    3 -> {
                        assertThat(given.getControlNumber()).isEqualTo("tes96000004 ")
                        val field100 = given.dataFields.first { field -> field.tag == "100" }
                        val field245 = given.dataFields.first { field -> field.tag == "245" }
                        val field500 = given.dataFields.first { field -> field.tag == "500" }
                        assertAll("MRK record 4 data was converted",
                            { assertThat(field100.indicator1).isEqualTo('2') },
                            { assertThat(field100.indicator2).isEqualTo(' ') },
                            { assertThat(field100.getData()).contains("Deer-Doe") },
                            { assertThat(field245.indicator1).isEqualTo('1') },
                            { assertThat(field245.indicator2).isEqualTo('0') },
                            { assertThat(field245.getData()).contains("test record number 4") },
                            { assertThat(field500.indicator1).isEqualTo(' ') },
                            { assertThat(field500.indicator2).isEqualTo(' ') },
                            { assertThat(field500.getData()).contains("degree sign 98.6°") }
                        )
                    }
                    4 -> {
                        assertThat(given.getControlNumber()).isEqualTo("tes96000005 ")
                        val field100 = given.dataFields.first { field -> field.tag == "100" }
                        val field245 = given.dataFields.first { field -> field.tag == "245" }
                        val field500 = given.dataFields.first { field -> field.tag == "500" }
                        assertAll("MRK record 5 data was converted",
                            { assertThat(field100.indicator1).isEqualTo('2') },
                            { assertThat(field100.indicator2).isEqualTo(' ') },
                            { assertThat(field100.getData()).contains("Deer-Doe") },
                            { assertThat(field245.indicator1).isEqualTo('1') },
                            { assertThat(field245.indicator2).isEqualTo('0') },
                            { assertThat(field245.getData()).contains("test record number 5") },
                            { assertThat(field500.indicator1).isEqualTo(' ') },
                            { assertThat(field500.indicator2).isEqualTo(' ') },
                            { assertThat(field500.getData()).contains("the uppercase Polish L in Łódź") }
                        )
                    }
                    5 -> {
                        assertThat(given.getControlNumber()).isEqualTo("tes96000006 ")
                        val field100 = given.dataFields.first { field -> field.tag == "100" }
                        val field245 = given.dataFields.first { field -> field.tag == "245" }
                        val field500 = given.dataFields.first { field -> field.tag == "500" }
                        assertAll("MRK record 6 data was converted",
                            { assertThat(field100.indicator1).isEqualTo('2') },
                            { assertThat(field100.indicator2).isEqualTo(' ') },
                            { assertThat(field100.getData()).contains("Deer-Doe") },
                            { assertThat(field245.indicator1).isEqualTo('1') },
                            { assertThat(field245.indicator2).isEqualTo('2') },
                            { assertThat(field245.getData()).contains("ultimate test record for diacritics") },
                            { assertThat(field500.indicator1).isEqualTo(' ') },
                            { assertThat(field500.indicator2).isEqualTo(' ') },
                            { assertThat(field500.getData()).contains("the uppercase Polish L in Łódź") }
                        )
                    }
                    6 -> {
                        assertThat(given.getControlNumber()).isEqualTo("tes96000007 ")
                        val field100 = given.dataFields.first { field -> field.tag == "100" }
                        val field245 = given.dataFields.first { field -> field.tag == "245" }
                        assertAll("MRK record 7 data was converted",
                            { assertThat(field100.indicator1).isEqualTo('2') },
                            { assertThat(field100.indicator2).isEqualTo(' ') },
                            { assertThat(field100.getData()).contains("Deer-Doe") },
                            { assertThat(field245.indicator1).isEqualTo('1') },
                            { assertThat(field245.indicator2).isEqualTo('2') },
                            { assertThat(field245.getData()).contains("processing of unrecognized mnemonic strings") },
                            { assertThat(field245.getData()).contains("like &zilch; which") }
                        )
                    }
                    7 -> {
                        assertThat(given.getControlNumber()).isEqualTo("tes96000008 ")
                        val field020 = given.dataFields.find { field -> field.tag == "020" && field.getData().contains("35.99") } ?: throw Exception("Couldn't find 020 tag in record 8")
                        val field100 = given.dataFields.first { field -> field.tag == "100" }
                        val field245 = given.dataFields.first { field -> field.tag == "245" }
                        assertAll("MRK record 8 data was converted",
                            { assertThat(field020.indicator1).isEqualTo(' ') },
                            { assertThat(field020.indicator2).isEqualTo(' ') },
                            { assertThat(field020.getData()).contains("""c$35.99""") },
                            { assertThat(field100.indicator1).isEqualTo('2') },
                            { assertThat(field100.indicator2).isEqualTo(' ') },
                            { assertThat(field100.getData()).contains("Deer-Doe") },
                            { assertThat(field245.indicator1).isEqualTo('1') },
                            { assertThat(field245.indicator2).isEqualTo('2') },
                            { assertThat(field245.getData()).contains("processing of the dollar sign") }
                        )
                    }
                    else -> Assertions.fail("Unknown MARC record in MRK_bib_records.mrk")
                }
            }
        }
    }
}