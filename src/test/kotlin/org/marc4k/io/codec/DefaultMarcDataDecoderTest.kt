package org.marc4k.io.codec

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.marc4k.MarcException
import org.marc4k.io.converter.marc8.Marc8ToUnicode
import java.nio.charset.Charset

internal class DefaultMarcDataDecoderTest {
    @Test
    fun `test createMarcRecord(Iso2709Record) with UTF-8 encoding`() {
        val iso2709Record = Iso2709Record("99999nam a2299999   4500").apply {
            controlFields.add(Iso2709ControlField("001", "control_number".toByteArray(Charsets.ISO_8859_1)))
            dataFields.add(Iso2709DataField("100", '1', ' ').apply {
                subfields += Iso2709Subfield('a', "Буйда, Юрий.".toByteArray(Charsets.UTF_8))
            })
        }

        val marcRecord = DefaultMarcDataDecoder("UTF-8").createMarcRecord(iso2709Record)
        val control = marcRecord.controlFields.find { it.tag == "001" }
        val author = marcRecord.dataFields.find { it.tag == "100" }

        assertAll(
            { assertThat(control.toString()).isEqualTo("001    control_number") },
            { assertThat(author.toString()).isEqualTo("100 1  ‡aБуйда, Юрий.") }
        )
    }

    @Test
    fun `test createMarcRecord(Iso2709Record) with ISO-8859-1 encoding`() {
        val iso2709Record = Iso2709Record("99999nam a2299999   4500").apply {
            controlFields.add(Iso2709ControlField("001", "control_number".toByteArray(Charsets.ISO_8859_1)))
            dataFields.add(Iso2709DataField("100", '1', ' ').apply {
                subfields += Iso2709Subfield('a', "Nesbø, Jo.".toByteArray(Charsets.ISO_8859_1))
            })
        }

        val marcRecord = DefaultMarcDataDecoder().createMarcRecord(iso2709Record)
        val control = marcRecord.controlFields.find { it.tag == "001" }
        val author = marcRecord.dataFields.find { it.tag == "100" }

        assertAll(
            { assertThat(control.toString()).isEqualTo("001    control_number") },
            { assertThat(author.toString()).isEqualTo("100 1  ‡aNesbø, Jo.") }
        )
    }

    @Test
    fun `test createMarcRecord(Iso2709Record) with ISO-8859-5 encoding`() {
        val iso2709Record = Iso2709Record("99999nam a2299999   4500").apply {
            controlFields.add(Iso2709ControlField("001", "control_number".toByteArray(Charsets.ISO_8859_1)))
            dataFields.add(Iso2709DataField("100", '1', ' ').apply {
                subfields += Iso2709Subfield('a', "Буйда, Юрий.".toByteArray(Charset.forName("ISO-8859-5")))
            })
        }

        val marcRecord = DefaultMarcDataDecoder("ISO-8859-5").createMarcRecord(iso2709Record)
        val control = marcRecord.controlFields.find { it.tag == "001" }
        val author = marcRecord.dataFields.find { it.tag == "100" }

        assertAll(
            { assertThat(control.toString()).isEqualTo("001    control_number") },
            { assertThat(author.toString()).isEqualTo("100 1  ‡aБуйда, Юрий.") }
        )
    }

    @Test
    fun `test createMarcRecord(Iso2709Record) with MARC8 converter`() {
        val iso2709Record = Iso2709Record("99999nam  2299999   4500").apply {
            controlFields.add(Iso2709ControlField("001", "control_number".toByteArray(Charsets.ISO_8859_1)))
            dataFields.add(Iso2709DataField("100", '1', ' ').apply {
                subfields += Iso2709Subfield('a', "\u001B\u0028\u004E\u0062\u0055\u004A\u0044\u0041\u002C\u0020\u0060\u0052\u0049\u004A\u002E\u001B\u0028\u0042".toByteArray(Charsets.ISO_8859_1))
            })
        }

        val marcRecord = DefaultMarcDataDecoder(converter = Marc8ToUnicode()).createMarcRecord(iso2709Record)
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

        val marcRecord = DefaultMarcDataDecoder(converter = Marc8ToUnicode()).createMarcRecord(iso2709Record)
        val control = marcRecord.controlFields.find { it.tag == "001" }
        val author = marcRecord.dataFields.find { it.tag == "100" }

        assertAll(
            { assertThat(control.toString()).isEqualTo("001    control_number") },
            { assertThat(author.toString()).isEqualTo("100 1  ‡aNbUJDA, `RIJ.") },
            { assertThat(marcRecord.errors).hasSize(1) },
            { assertThat(marcRecord.errors[0].toString()).contains("Hex: (0x1b 0x4e 0x62") }
        )
    }

    @Test
    fun `test createMarcRecord(Iso2709Record) with unknown encoding`() {
        val iso2709Record = Iso2709Record("99999nam a2299999   4500").apply {
            controlFields.add(Iso2709ControlField("001", "control_number".toByteArray(Charsets.ISO_8859_1)))
            dataFields.add(Iso2709DataField("100", '1', ' ').apply {
                subfields += Iso2709Subfield('a', "Nesbø, Jo.".toByteArray(Charsets.ISO_8859_1))
            })
        }

        assertThatExceptionOfType(MarcException::class.java).isThrownBy { DefaultMarcDataDecoder("UNKNOWN").createMarcRecord(iso2709Record) }
    }
}