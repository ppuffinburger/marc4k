package org.marc4k.marc

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime

internal class RecordTest {

    @Test
    fun `test getControlNumber() with no 001`() {
        val given = TestRecord()
        assertThat(given.getControlNumber()).isNull()
    }

    @Test
    fun `test getControlNumber() with 001`() {
        val given = TestRecord()
        given.controlFields += ControlField("001", "control_number")
        assertThat(given.getControlNumber()).isEqualTo("control_number")
    }

    @Test
    fun `test getDateOfLatestTransaction() with no 005`() {
        val given = TestRecord()
        assertThat(given.getDateOfLatestTransaction()).isNull()
    }

    @Test
    fun `test getDateOfLatestTransaction() with 005`() {
        val given = TestRecord()
        given.controlFields += ControlField("005", "19700608104321.0")

        val expected = LocalDateTime.of(1970, 6, 8, 10, 43, 21)

        assertThat(given.getDateOfLatestTransaction()).isEqualTo(expected)
    }

    @Test
    fun `test copyFrom()`() {
        val original = TestRecord().apply {
            leader.setData("12345nam a2254321 a 4500")
            controlFields += ControlField("001", "control_number")
            dataFields += DataField(
                "100",
                subfields = mutableListOf(Subfield('a', "subfield_a"))
            )
        }
        val copy = TestRecord().apply { copyFrom(original) }

        assertAll(
            { assertThat(copy).isNotSameAs(original) },
            { assertThat(copy).usingRecursiveComparison().isEqualTo(original) }
        )
    }

    @Test
    fun `test toString()`() {
        val given = TestRecord().apply {
            leader.setData("12345nam a2254321 a 4500")
            controlFields += ControlField("001", "control_number")
            dataFields += DataField(
                "100",
                subfields = mutableListOf(Subfield('a', "subfield_a"))
            )
        }

        val expected = "LEADER 12345nam a2254321 a 4500\n001    control_number\n100    \u2021asubfield_a\n"

        assertThat(given.toString()).isEqualTo(expected)
    }

    internal class TestRecord: Record() {
        override val leader = TestLeader()
    }

    internal class TestLeader: Leader() {
        override fun setData(data: String) {
            require(data.length == 24) { "Leader is not the correct length." }

            _recordLength = data.substring(0..4).toIntOrNull() ?: 0
            _recordStatus = data[5]
            _typeOfRecord = data[6]
            _implementationDefined1 = data.substring(7..9).toCharArray()
            _indicatorCount = if (data[10].isDigit()) data[10].toString().toInt() else 2
            _subfieldCodeCount = if (data[11].isDigit()) data[11].toString().toInt() else 2
            _baseAddressOfData = data.substring(12..16).toIntOrNull() ?: 0
            _implementationDefined2 = data.substring(17..19).toCharArray()
            _entryMap = data.substring(20..23).toCharArray()
        }
    }
}