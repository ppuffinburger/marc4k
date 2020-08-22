package org.marc4k.marc.marc21.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.marc4k.marc.ControlField
import org.marc4k.marc.DataField
import org.marc4k.marc.Subfield

internal class BibliographicRecordTest {

    @Test
    fun `test newly constructed BibliographicRecord`() {
        val given = BibliographicRecord()
        assertAll(
            { assertThat(given.leader).isInstanceOf(BibliographicLeader::class.java) },
            { assertThat(given.controlFields).isEmpty() },
            { assertThat(given.dataFields).isEmpty() }
        )
    }

    @Test
    fun `test getValid008Data() with no 008`() {
        val given = BibliographicRecord()
        assertThat(given.getValid008Data()).isNull()
    }

    @Test
    fun `test getValid008Data() with invalid length 008`() {
        val given = BibliographicRecord()
            .apply { controlFields += ControlField("008", "0123") }
        assertThat(given.getValid008Data()).isNull()
    }

    @Test
    fun `test getValid008Data() with 008`() {
        val given = BibliographicRecord()
            .apply { controlFields += ControlField(
                "008",
                "1234567890123456789012345678901234567890"
            )
            }
        assertThat(given.getValid008Data()).isEqualTo("1234567890123456789012345678901234567890")
    }

    @Test
    fun `test getTitleField() with no 245`() {
        val given = BibliographicRecord()
        assertThat(given.getTitleField()).isNull()
    }

    @Test
    fun `test getTitleField() with 245`() {
        val expected =
            DataField("245", subfields = mutableListOf(Subfield('a', "title")))
        val given = BibliographicRecord().apply { dataFields += expected }
        assertThat(given.getTitleField()).isSameAs(expected)
    }

    @Test
    fun `test getMainEntryField() with no main entry`() {
        val given = BibliographicRecord()
        assertThat(given.getMainEntryField()).isNull()
    }

    @Test
    fun `test getMainEntryField() with 100`() {
        val expected =
            DataField("100", subfields = mutableListOf(Subfield('a', "main_entry")))
        val given = BibliographicRecord().apply { dataFields += expected }
        assertThat(given.getMainEntryField()).isSameAs(expected)
    }

    @Test
    fun `test getMainEntryField() with 110`() {
        val expected =
            DataField("110", subfields = mutableListOf(Subfield('a', "main_entry")))
        val given = BibliographicRecord().apply { dataFields += expected }
        assertThat(given.getMainEntryField()).isSameAs(expected)
    }

    @Test
    fun `test getMainEntryField() with 111`() {
        val expected =
            DataField("111", subfields = mutableListOf(Subfield('a', "main_entry")))
        val given = BibliographicRecord().apply { dataFields += expected }
        assertThat(given.getMainEntryField()).isSameAs(expected)
    }

    @Test
    fun `test getMainEntryField() with 130`() {
        val expected =
            DataField("130", subfields = mutableListOf(Subfield('a', "main_entry")))
        val given = BibliographicRecord().apply { dataFields += expected }
        assertThat(given.getMainEntryField()).isSameAs(expected)
    }

    @Test
    fun `test getTargetAudience() with no 008`() {
        val given = BibliographicRecord()
        assertThat(given.getTargetAudience()).isNull()
    }

    @ParameterizedTest
    @CsvSource(
        "LANGUAGE_MATERIAL, MONOGRAPH_ITEM, true",
        "LANGUAGE_MATERIAL, MONOGRAPHIC_COMPONENT_PART, true",
        "LANGUAGE_MATERIAL, COLLECTION, true",
        "LANGUAGE_MATERIAL, SUBUNIT, true",
        "LANGUAGE_MATERIAL, INVALID, false",
        "MANUSCRIPT_LANGUAGE_MATERIAL, MONOGRAPH_ITEM, true",
        "MANUSCRIPT_LANGUAGE_MATERIAL, MONOGRAPHIC_COMPONENT_PART, true",
        "MANUSCRIPT_LANGUAGE_MATERIAL, COLLECTION, true",
        "MANUSCRIPT_LANGUAGE_MATERIAL, SUBUNIT, true",
        "MANUSCRIPT_LANGUAGE_MATERIAL, INVALID, false",
        "COMPUTER_FILE, INVALID, true",
        "NOTATED_MUSIC, INVALID, true",
        "MANUSCRIPT_NOTATED_MUSIC, INVALID, true",
        "PROJECTED_MEDIUM, INVALID, true",
        "NON_MUSICAL_SOUND_RECORDING, INVALID, true",
        "MUSICAL_SOUND_RECORDING, INVALID, true",
        "KIT, INVALID, true",
        "TWO_DIMENSIONAL_NON_PROJECTABLE_GRAPHIC, INVALID, true",
        "THREE_DIMENSIONAL_ARTIFACT_OR_NATURALLY_OCCURRING_OBJECT, INVALID, true",
        "CARTOGRAPHIC_MATERIAL, INVALID, false",
        "MANUSCRIPT_CARTOGRAPHIC_MATERIAL, INVALID, false",
        "MIXED_MATERIAL, INVALID, false"
    )
    fun `test getTargetAudience() returns correctly`(typeOfRecord: TypeOfRecord, bibliographicLevel: BibliographicLevel, expected: Boolean) {
        val given = BibliographicRecord().apply {
            leader.typeOfRecord = typeOfRecord
            leader.bibliographicLevel = bibliographicLevel
            controlFields += ControlField("008", "                      X                 ")
        }
        if (expected) {
            assertThat(given.getTargetAudience()).isEqualTo('X')
        } else {
            assertThat(given.getTargetAudience()).isNull()
        }
    }

    @Test
    fun `test isRda() with DescriptiveCatalogingForm not ISBD_PUNCTUATION_INCLUDED`() {
        val given = BibliographicRecord()
        assertThat(given.isRda()).isFalse
    }

    @Test
    fun `test isRda() with no 040`() {
        val given = BibliographicRecord()
            .apply { leader.descriptiveCatalogingForm = DescriptiveCatalogingForm.ISBD_PUNCTUATION_INCLUDED }
        assertThat(given.isRda()).isFalse
    }

    @Test
    fun `test isRda() with 040 does not contain $e`() {
        val given = BibliographicRecord().apply {
            leader.descriptiveCatalogingForm = DescriptiveCatalogingForm.ISBD_PUNCTUATION_INCLUDED
            dataFields += DataField(
                "040",
                subfields = mutableListOf(Subfield('a', "subfield_a"))
            )
        }
        assertThat(given.isRda()).isFalse
    }

    @Test
    fun `test isRda() with 040 does not contain $erda`() {
        val given = BibliographicRecord().apply {
            leader.descriptiveCatalogingForm = DescriptiveCatalogingForm.ISBD_PUNCTUATION_INCLUDED
            dataFields += DataField(
                "040",
                subfields = mutableListOf(
                    Subfield('a', "subfield_a"),
                    Subfield('e', "subfield_e")
                )
            )
        }
        assertThat(given.isRda()).isFalse
    }

    @Test
    fun `test isRda() with 040 does contain $erda`() {
        val given = BibliographicRecord().apply {
            leader.descriptiveCatalogingForm = DescriptiveCatalogingForm.ISBD_PUNCTUATION_INCLUDED
            dataFields += DataField(
                "040",
                subfields = mutableListOf(
                    Subfield('a', "subfield_a"),
                    Subfield('e', "subfield_e"),
                    Subfield('e', "rda")
                )
            )
        }
        assertThat(given.isRda()).isTrue
    }
}