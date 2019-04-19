package org.marc4k.converter.marc8

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class CombiningDoubleTildeParserTest {
    @Test
    fun `test parse(Tracker) with valid sequence`() {
        val tracker = CodeDataTracker("\u00FAa\u00FBi".toCharArray())
        val given = CombiningDoubleTildeParser().parse(tracker)
        assertAll(
            { assertThat(given).isInstanceOf(CombiningParserResult.Success::class.java) },
            { assertThat((given as CombiningParserResult.Success).result).isEqualTo("a͠i") }
        )
    }

    @Test
    fun `test parse(Tracker) with no first half`() {
        val tracker = CodeDataTracker("".toCharArray())
        val given = CombiningDoubleTildeParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }

    @Test
    fun `test parse(Tracker) with invalid first half`() {
        val tracker = CodeDataTracker("Xa\u00FBi".toCharArray())
        val given = CombiningDoubleTildeParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }

    @Test
    fun `test parse(Tracker) with no first character`() {
        val tracker = CodeDataTracker("\u00FA".toCharArray())
        val given = CombiningDoubleTildeParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }

    @Test
    fun `test parse(Tracker) with invalid first character`() {
        val tracker = CodeDataTracker("\u00FA\u000D\u00FBi".toCharArray())
        val given = CombiningDoubleTildeParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }

    @Test
    fun `test parse(Tracker) with no second half`() {
        val tracker = CodeDataTracker("\u00FAa".toCharArray())
        val given = CombiningDoubleTildeParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }

    @Test
    fun `test parse(Tracker) with invalid second half`() {
        val tracker = CodeDataTracker("\u00FAaXi".toCharArray())
        val given = CombiningDoubleTildeParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }

    @Test
    fun `test parse(Tracker) with no second character`() {
        val tracker = CodeDataTracker("\u00FAa\u00FB".toCharArray())
        val given = CombiningDoubleTildeParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }

    @Test
    fun `test parse(Tracker) with invalid second character`() {
        val tracker = CodeDataTracker("\u00FAa\u00FB\u000D".toCharArray())
        val given = CombiningDoubleTildeParser().parse(tracker)
        assertThat(given).isInstanceOf(CombiningParserResult.Failure::class.java)
    }
}