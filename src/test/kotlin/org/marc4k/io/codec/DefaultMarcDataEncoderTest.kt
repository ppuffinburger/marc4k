package org.marc4k.io.codec

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.marc4k.MarcException
import org.marc4k.io.converter.marc8.UnicodeToMarc8
import org.marc4k.marc.ControlField
import org.marc4k.marc.DataField
import org.marc4k.marc.MarcRecord
import org.marc4k.marc.Subfield

internal class DefaultMarcDataEncoderTest {
    @Test
    fun `test createIso2709Record(MarcRecord) with UTF-8 encoding`() {
        val marcRecord = MarcRecord().apply {
            leader.setData("99999nam a2299999   4500")
            controlFields.add(ControlField("001", "control_number"))
            dataFields.add(DataField("100", '1', ' ').apply {
                subfields += Subfield('a', "Буйда, Юрий.")
            })
        }

        val iso2709Record = DefaultMarcDataEncoder("UTF-8").createIso2709Record(marcRecord)
        val control = iso2709Record.controlFields[0]
        val author = iso2709Record.dataFields[0]

        assertAll(
            { assertThat(control.tag).isEqualTo("001") },
            { assertThat(control.data).isEqualTo(byteArrayOf(0x63, 0x6F, 0x6E, 0x74, 0x72, 0x6F, 0x6C, 0x5F, 0x6E, 0x75, 0x6D, 0x62, 0x65, 0x72)) },
            { assertThat(author.tag).isEqualTo("100") },
            { assertThat(author.indicator1).isEqualTo('1') },
            { assertThat(author.indicator2).isEqualTo(' ') },
            { assertThat(author.subfields).hasSize(1) },
            { assertThat(author.subfields[0].name).isEqualTo('a') },
            { assertThat(author.subfields[0].data).isEqualTo(byteArrayOf(0xD0.toByte(), 0x91.toByte(), 0xD1.toByte(), 0x83.toByte(), 0xD0.toByte(), 0xB9.toByte(), 0xD0.toByte(), 0xB4.toByte(), 0xD0.toByte(), 0xB0.toByte(), 0x2C, 0x20, 0xD0.toByte(), 0xAE.toByte(), 0xD1.toByte(), 0x80.toByte(), 0xD0.toByte(), 0xB8.toByte(), 0xD0.toByte(), 0xB9.toByte(), 0x2E)) }
        )
    }

    @Test
    fun `test createIso2709Record(MarcRecord) with ISO-8859-1 encoding`() {
        val marcRecord = MarcRecord().apply {
            leader.setData("99999nam a2299999   4500")
            controlFields.add(ControlField("001", "control_number"))
            dataFields.add(DataField("100", '1', ' ').apply {
                subfields += Subfield('a', "Nesbø, Jo.")
            })
        }

        val iso2709Record = DefaultMarcDataEncoder().createIso2709Record(marcRecord)
        val control = iso2709Record.controlFields[0]
        val author = iso2709Record.dataFields[0]

        assertAll(
            { assertThat(control.tag).isEqualTo("001") },
            { assertThat(control.data).isEqualTo(byteArrayOf(0x63, 0x6F, 0x6E, 0x74, 0x72, 0x6F, 0x6C, 0x5F, 0x6E, 0x75, 0x6D, 0x62, 0x65, 0x72)) },
            { assertThat(author.tag).isEqualTo("100") },
            { assertThat(author.indicator1).isEqualTo('1') },
            { assertThat(author.indicator2).isEqualTo(' ') },
            { assertThat(author.subfields).hasSize(1) },
            { assertThat(author.subfields[0].name).isEqualTo('a') },
            { assertThat(author.subfields[0].data).inHexadecimal().isEqualTo(byteArrayOf(0x4E, 0x65, 0x73, 0x62, 0xF8.toByte(), 0x2C, 0x20, 0x4A, 0x6F, 0x2E)) }
        )
    }

    @Test
    fun `test createIso2709Record(MarcRecord) with ISO-8859-5 encoding`() {
        val marcRecord = MarcRecord().apply {
            leader.setData("99999nam a2299999   4500")
            controlFields.add(ControlField("001", "control_number"))
            dataFields.add(DataField("100", '1', ' ').apply {
                subfields += Subfield('a', "Буйда, Юрий.")
            })
        }

        val iso2709Record = DefaultMarcDataEncoder("ISO-8859-5").createIso2709Record(marcRecord)
        val control = iso2709Record.controlFields[0]
        val author = iso2709Record.dataFields[0]

        assertAll(
            { assertThat(control.tag).isEqualTo("001") },
            { assertThat(control.data).isEqualTo(byteArrayOf(0x63, 0x6F, 0x6E, 0x74, 0x72, 0x6F, 0x6C, 0x5F, 0x6E, 0x75, 0x6D, 0x62, 0x65, 0x72)) },
            { assertThat(author.tag).isEqualTo("100") },
            { assertThat(author.indicator1).isEqualTo('1') },
            { assertThat(author.indicator2).isEqualTo(' ') },
            { assertThat(author.subfields).hasSize(1) },
            { assertThat(author.subfields[0].name).isEqualTo('a') },
            { assertThat(author.subfields[0].data).isEqualTo(byteArrayOf(0xB1.toByte(), 0xE3.toByte(), 0xD9.toByte(), 0xD4.toByte(), 0xD0.toByte(), 0x2C, 0x20, 0xCE.toByte(), 0xE0.toByte(), 0xD8.toByte(), 0xD9.toByte(), 0x2E)) }
        )
    }

    @Test
    fun `test createIso2709Record(MarcRecord) with MARC8 set`() {
        val marcRecord = MarcRecord().apply {
            leader.setData("99999nam  2299999   4500")
            controlFields.add(ControlField("001", "control_number"))
            dataFields.add(DataField("100", '1', ' ').apply {
                subfields += Subfield('a', "Буйда, Юрий.")
            })
        }

        val iso2709Record = DefaultMarcDataEncoder(converter = UnicodeToMarc8()).createIso2709Record(marcRecord)
        val control = iso2709Record.controlFields[0]
        val author = iso2709Record.dataFields[0]

        assertAll(
            { assertThat(control.tag).isEqualTo("001") },
            { assertThat(control.data).isEqualTo(byteArrayOf(0x63, 0x6F, 0x6E, 0x74, 0x72, 0x6F, 0x6C, 0x5F, 0x6E, 0x75, 0x6D, 0x62, 0x65, 0x72)) },
            { assertThat(author.tag).isEqualTo("100") },
            { assertThat(author.indicator1).isEqualTo('1') },
            { assertThat(author.indicator2).isEqualTo(' ') },
            { assertThat(author.subfields).hasSize(1) },
            { assertThat(author.subfields[0].name).isEqualTo('a') },
            { assertThat(author.subfields[0].data)
                .isEqualTo(byteArrayOf(0x1B, 0x28, 0x4E, 0x62, 0x55, 0x4A, 0x44, 0x41, 0x2C, 0x20, 0x60, 0x52, 0x49, 0x4A, 0x2E, 0x1B, 0x28, 0x42)) }
        )
    }

    @Test
    fun `test createMarcRecord(Iso2709Record) with unknown encoding`() {
        val marcRecord = MarcRecord().apply {
            leader.setData("99999nam a2299999   4500")
            controlFields.add(ControlField("001", "control_number"))
            dataFields.add(DataField("100", '1', ' ').apply {
                subfields += Subfield('a', "Буйда, Юрий.")
            })
        }

        assertThatExceptionOfType(MarcException::class.java).isThrownBy { DefaultMarcDataEncoder("UNKNOWN").createIso2709Record(marcRecord) }
    }
}