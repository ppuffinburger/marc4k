package org.marc4k.io.codec

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class Marc21DataDecoderTest {
    private val decoder = Marc21DataDecoder()

    @Test
    fun `test createMarcRecord(Iso2709Record) with UTF-8 set`() {
        val iso2709Record = Iso2709Record("99999nam a2299999   4500").apply {
            controlFields.add(Iso2709ControlField("001", "control_number".toByteArray(Charsets.ISO_8859_1)))
            dataFields.add(Iso2709DataField("100", '1', ' ').apply {
                subfields += Iso2709Subfield('a', "Буйда, Юрий.".toByteArray(Charsets.UTF_8))
            })
        }

        val marcRecord = decoder.createMarcRecord(iso2709Record)
        val control = marcRecord.controlFields.find { it.tag == "001" }
        val author = marcRecord.dataFields.find { it.tag == "100" }

        assertAll(
            { assertThat(control.toString()).isEqualTo("001    control_number") },
            { assertThat(author.toString()).isEqualTo("100 1  ‡aБуйда, Юрий.") }
        )
    }

    @Test
    fun `test createMarcRecord(Iso2709Record) with MARC8 set`() {
        val iso2709Record = Iso2709Record("99999nam  2299999   4500").apply {
            controlFields.add(Iso2709ControlField("001", "control_number".toByteArray(Charsets.ISO_8859_1)))
            dataFields.add(Iso2709DataField("100", '1', ' ').apply {
                subfields += Iso2709Subfield('a', "\u001B\u0028\u004E\u0062\u0055\u004A\u0044\u0041\u002C\u0020\u0060\u0052\u0049\u004A\u002E\u001B\u0028\u0042".toByteArray(Charsets.ISO_8859_1))
            })
        }

        val marcRecord = decoder.createMarcRecord(iso2709Record)
        val control = marcRecord.controlFields.find { it.tag == "001" }
        val author = marcRecord.dataFields.find { it.tag == "100" }

        assertAll(
            { assertThat(control.toString()).isEqualTo("001    control_number") },
            { assertThat(author.toString()).isEqualTo("100 1  ‡aБуйда, Юрий.") }
        )
    }

    @Test
    fun `test createMarcRecord(Iso2709Record) with conversion errors`() {
        val iso2709Record = Iso2709Record("99999nam  2299999   4500").apply {
            controlFields.add(Iso2709ControlField("001", "control_number".toByteArray(Charsets.ISO_8859_1)))
            dataFields.add(Iso2709DataField("100", '1', ' ').apply {
                subfields += Iso2709Subfield('a', "\u001B\u004E\u0062\u0055\u004A\u0044\u0041\u002C\u0020\u0060\u0052\u0049\u004A\u002E\u001B\u0028\u0042".toByteArray(Charsets.ISO_8859_1))
            })
        }

        val marcRecord = decoder.createMarcRecord(iso2709Record)
        val control = marcRecord.controlFields.find { it.tag == "001" }
        val author = marcRecord.dataFields.find { it.tag == "100" }

        assertAll(
            { assertThat(control.toString()).isEqualTo("001    control_number") },
            { assertThat(author.toString()).isEqualTo("100 1  ‡aNbUJDA, `RIJ.") },
            { assertThat(marcRecord.errors).hasSize(1) },
            { assertThat(marcRecord.errors[0].toString()).contains("Hex: (0x1b 0x4e 0x62") }
        )
    }
}