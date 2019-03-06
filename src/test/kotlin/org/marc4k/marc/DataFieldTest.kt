package org.marc4k.marc

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DataFieldTest {

    @Test
    fun `test setData() with empty String`() {
        val given = DataField("100").apply { setData("") }
        assertThat(given.getData()).isEmpty()
    }

    @Test
    fun `test setData() with delimiter with no name after`() {
        val given = DataField("100").apply { setData("\u001F") }
        assertThat(given.getData()).isEmpty()
    }

    @Test
    fun `test setData() with delimiter and name with no data`() {
        val given = DataField("100").apply { setData("\u001Fa") }
        assertThat(given.getData()).isEmpty()
    }

    @Test
    fun `test setData() with one subfield`() {
        val given = DataField("100").apply { setData("\u001Fasubfield_a") }
        assertThat(given.getData()).isEqualTo("\u001Fasubfield_a")
    }

    @Test
    fun `test setData() with one subfield followed by delimiter with no name after`() {
        val given = DataField("100").apply { setData("\u001Fasubfield_a\u001F") }
        assertThat(given.getData()).isEqualTo("\u001Fasubfield_a")
    }

    @Test
    fun `test setData() with one subfield followed by delimiter and name with no data`() {
        val given = DataField("100").apply { setData("\u001Fasubfield_a\u001Fb") }
        assertThat(given.getData()).isEqualTo("\u001Fasubfield_a")
    }

    @Test
    fun `test setData() with two subfields`() {
        val given = DataField("100").apply { setData("\u001Fasubfield_a\u001Fbsubfield_b") }
        assertThat(given.getData()).isEqualTo("\u001Fasubfield_a\u001Fbsubfield_b")
    }

    @Test
    fun `test toString()`() {
        val given = DataField(
            "100",
            indicator1 = '1',
            indicator2 = '2',
            subfields = mutableListOf(
                Subfield('a', "subfield_a"),
                Subfield('b', "subfield_b")
            )
        )
        assertThat(given.toString()).isEqualTo("100 12 ‡asubfield_a‡bsubfield_b")
    }
}