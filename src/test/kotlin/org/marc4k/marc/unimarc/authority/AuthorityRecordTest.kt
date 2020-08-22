package org.marc4k.marc.unimarc.authority

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.marc4k.marc.DataField
import org.marc4k.marc.Subfield

internal class AuthorityRecordTest {

    @Test
    fun `test newly constructed AuthorityRecord`() {
        val given = AuthorityRecord()
        assertAll(
            { assertThat(given.leader).isInstanceOf(AuthorityLeader::class.java) },
            { assertThat(given.controlFields).isEmpty() },
            { assertThat(given.dataFields).isEmpty() }
        )
    }

    @Test
    fun `test getAuthorizedHeadingField() with no authorized heading`() {
        val given = AuthorityRecord()
        assertThat(given.getAuthorizedHeadingField()).isNull()
    }

    @ParameterizedTest
    @CsvSource(
        "200", "210", "215", "216", "220", "230", "235", "240", "245", "250", "260", "280"
    )
    fun `test getAuthorizedHeadingField() returns correctly`(tag: String) {
        val expected = DataField(tag, subfields = mutableListOf(Subfield('a', "subfield_a")))
        val given = AuthorityRecord().apply {
            dataFields += expected
        }
        assertThat(given.getAuthorizedHeadingField()).isSameAs(expected)
    }
}