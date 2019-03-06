package org.marc4k.marc.marc21.authority

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.marc4k.marc.ControlField
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
    fun `test getValid008Data() with no 008`() {
        val given = AuthorityRecord()
        assertThat(given.getValid008Data()).isNull()
    }

    @Test
    fun `test getValid008Data() with invalid length 008`() {
        val given = AuthorityRecord()
            .apply { controlFields += ControlField("008", "0123") }
        assertThat(given.getValid008Data()).isNull()
    }

    @Test
    fun `test getValid008Data() with 008`() {
        val given = AuthorityRecord()
            .apply { controlFields += ControlField(
                "008",
                "1234567890123456789012345678901234567890"
            )
            }
        assertThat(given.getValid008Data()).isEqualTo("1234567890123456789012345678901234567890")
    }

    @Test
    fun `test getAuthorizedHeadingField() with no authorized heading`() {
        val given = AuthorityRecord()
        assertThat(given.getAuthorizedHeadingField()).isNull()
    }

    @ParameterizedTest
    @CsvSource(
        "100", "110", "111", "130", "151", "147", "148", "150", "151", "155", "162", "180", "181", "182", "185"
    )
    fun `test getAuthorizedHeadingField() returns correctly`(tag: String) {
        val expected =
            DataField(tag, subfields = mutableListOf(Subfield('a', "subfield_a")))
        val given = AuthorityRecord().apply {
            dataFields += expected
        }
        assertThat(given.getAuthorizedHeadingField()).isSameAs(expected)
    }
}