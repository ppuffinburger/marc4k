package org.marc4k.io.converter.marc8

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class CombiningDoubleInvertedBreveParserTest {
    @Test
    fun `test parse(Tracker) with valid sequence`() {
        val tracker = CodeDataTracker("\u00EBa\u00ECi".toCharArray())
        val given = CombiningDoubleInvertedBreveParser().parse(tracker)
        assertAll(
            { assertThat(given).isInstanceOf(CombiningParserResult.Success::class.java) },
            { assertThat((given as CombiningParserResult.Success).result).isEqualTo("a͡i") }
        )
    }

    @Test
    fun `test parse(Tracker) with no first half`() {
        val tracker = CodeDataTracker("".toCharArray())
        val given = CombiningDoubleInvertedBreveParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }

    @Test
    fun `test parse(Tracker) with invalid first half`() {
        val tracker = CodeDataTracker("Xa\u00ECi".toCharArray())
        val given = CombiningDoubleInvertedBreveParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }

    @Test
    fun `test parse(Tracker) with no first character`() {
        val tracker = CodeDataTracker("\u00EB".toCharArray())
        val given = CombiningDoubleInvertedBreveParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }

    @Test
    fun `test parse(Tracker) with invalid first character`() {
        val tracker = CodeDataTracker("\u00EB\u000D\u00ECi".toCharArray())
        val given = CombiningDoubleInvertedBreveParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }

    @Test
    fun `test parse(Tracker) with no second half`() {
        val tracker = CodeDataTracker("\u00EBa".toCharArray())
        val given = CombiningDoubleInvertedBreveParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }

    @Test
    fun `test parse(Tracker) with invalid second half`() {
        val tracker = CodeDataTracker("\u00EBaXi".toCharArray())
        val given = CombiningDoubleInvertedBreveParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }

    @Test
    fun `test parse(Tracker) with no second character`() {
        val tracker = CodeDataTracker("\u00EBa\u00EC".toCharArray())
        val given = CombiningDoubleInvertedBreveParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }

    @Test
    fun `test parse(Tracker) with invalid second character`() {
        val tracker = CodeDataTracker("\u00EBa\u00EC\u000D".toCharArray())
        val given = CombiningDoubleInvertedBreveParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }
}