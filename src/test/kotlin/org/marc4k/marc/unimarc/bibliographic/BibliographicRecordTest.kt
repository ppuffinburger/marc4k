package org.marc4k.marc.unimarc.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.marc4k.marc.DataField
import org.marc4k.marc.Subfield

internal class BibliographicRecordTest {

    @Test
    fun `test newly constructed BibliographicRecord`() {
        val given = BibliographicRecord()
        org.junit.jupiter.api.assertAll(
            { assertThat(given.leader).isInstanceOf(BibliographicLeader::class.java) },
            { assertThat(given.controlFields).isEmpty() },
            { assertThat(given.dataFields).isEmpty() }
        )
    }

    @Test
    fun `test getTitleField() with no 200`() {
        val given = BibliographicRecord()
        assertThat(given.getTitleField()).isNull()
    }

    @Test
    fun `test getTitleField() with 200`() {
        val expected = DataField("200", subfields = mutableListOf(Subfield('a', "title")))
        val given = BibliographicRecord().apply { dataFields += expected }
        assertThat(given.getTitleField()).isSameAs(expected)
    }


    @Test
    fun `test getMainEntryField() with no main entry`() {
        val given = BibliographicRecord()
        assertThat(given.getMainEntryField()).isNull()
    }

    @Test
    fun `test getMainEntryField() with 700`() {
        val expected = DataField("700", subfields = mutableListOf(Subfield('a', "main_entry")))
        val given = BibliographicRecord().apply { dataFields += expected }
        assertThat(given.getMainEntryField()).isSameAs(expected)
    }

    @Test
    fun `test getMainEntryField() with 710`() {
        val expected = DataField("710", subfields = mutableListOf(Subfield('a', "main_entry")))
        val given = BibliographicRecord().apply { dataFields += expected }
        assertThat(given.getMainEntryField()).isSameAs(expected)
    }

    @Test
    fun `test getMainEntryField() with 720`() {
        val expected = DataField("720", subfields = mutableListOf(Subfield('a', "main_entry")))
        val given = BibliographicRecord().apply { dataFields += expected }
        assertThat(given.getMainEntryField()).isSameAs(expected)
    }
}