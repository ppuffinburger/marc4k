package org.marc4k.marc

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

internal class CustomDecimalFormatTest {
    private val defaultFormat = CustomDecimalFormat(3)
    private val allNinesFormat = CustomDecimalFormat(3, OverflowRepresentation.ALL_NINES)
    private val allZeroesFormat = CustomDecimalFormat(3, OverflowRepresentation.ALL_ZEROS)
    private val truncateFormat = CustomDecimalFormat(3, OverflowRepresentation.TRUNCATE)

    private val allNinesTestData = listOf(
        Triple(1, 1.1, "001"),
        Triple(10, 10.1, "010"),
        Triple(100, 100.1, "100"),
        Triple(999, 999.1, "999"),
        Triple(1000, 1000.1, "999"),
        Triple(9999, 9999.1, "999"),
        Triple(10000, 10000.1, "999"),
        Triple(10001, 10001.1, "999")
    )

    @TestFactory
    fun `test format(long) with default OverflowRepresentation`() = allNinesTestData.map { (givenLong, givenFloat, expected) ->
        DynamicTest.dynamicTest("Test that given '$givenLong' and '$givenFloat' is equal to '$expected'") {
            assertThat(defaultFormat.format(givenLong)).isEqualTo(expected)
            assertThat(defaultFormat.format(givenFloat)).isEqualTo(expected)
        }
    }

    @TestFactory
    fun `test format(long) with ALL_NINES`() = allNinesTestData.map { (givenLong, givenFloat, expected) ->
        DynamicTest.dynamicTest("Test that given '$givenLong' and '$givenFloat' is equal to '$expected'") {
            assertThat(allNinesFormat.format(givenLong)).isEqualTo(expected)
            assertThat(allNinesFormat.format(givenFloat)).isEqualTo(expected)
        }
    }

    private val allZeroesTestData = listOf(
        Triple(1, 1.1, "001"),
        Triple(10, 10.1, "010"),
        Triple(100, 100.1, "100"),
        Triple(999, 999.1, "999"),
        Triple(1000, 1000.1, "000"),
        Triple(9999, 9999.1, "000"),
        Triple(10000, 10000.1, "000"),
        Triple(10001, 10001.1, "000")
    )

    @TestFactory
    fun `test format(long) with ALL_ZEROES`() = allZeroesTestData.map { (givenLong, givenFloat, expected) ->
        DynamicTest.dynamicTest("Test that given '$givenLong' and '$givenFloat' is equal to '$expected'") {
            assertThat(allZeroesFormat.format(givenLong)).isEqualTo(expected)
            assertThat(allZeroesFormat.format(givenFloat)).isEqualTo(expected)
        }
    }

    private val truncateTestData = listOf(
        Triple(1, 1.1, "001"),
        Triple(10, 10.1, "010"),
        Triple(100, 100.1, "100"),
        Triple(999, 999.1, "999"),
        Triple(1000, 1000.1, "000"),
        Triple(9999, 9999.1, "999"),
        Triple(10000, 10000.1, "000"),
        Triple(10001, 10001.1, "001")
    )

    @TestFactory
    fun `test format(long) with TRUNCATE`() = truncateTestData.map { (givenLong, givenFloat, expected) ->
        DynamicTest.dynamicTest("Test that given '$givenLong' and '$givenFloat' is equal to '$expected'") {
            assertThat(truncateFormat.format(givenLong)).isEqualTo(expected)
            assertThat(truncateFormat.format(givenFloat)).isEqualTo(expected)
        }
    }
}