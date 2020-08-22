package org.marc4k.io.converter.marc8

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

internal class Marc8EscapeSequenceParserTest {
    @Test
    fun `test parse(Tracker) with no escape sequence`() {
        val tracker = CodeDataTracker("".toCharArray())
        assertThat(Marc8EscapeSequenceParser().parse(tracker)).isFalse
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.g0).isEqualTo(0x42)
        assertThat(tracker.g1).isEqualTo(0x45)
    }

    @Test
    fun `test parse(Tracker) with only escape`() {
        val tracker = CodeDataTracker("\u001B".toCharArray())
        assertThat(Marc8EscapeSequenceParser().parse(tracker)).isFalse
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.g0).isEqualTo(0x42)
        assertThat(tracker.g1).isEqualTo(0x45)
    }

    @Test
    fun `test parse(Tracker) with no escape sequence and data`() {
        val tracker = CodeDataTracker("data".toCharArray())
        assertThat(Marc8EscapeSequenceParser().parse(tracker)).isFalse
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.g0).isEqualTo(0x42)
        assertThat(tracker.g1).isEqualTo(0x45)
    }

    @Test
    fun `test parse(Tracker) with invalid designator`() {
        val tracker = CodeDataTracker("\u001B\u002Fdata".toCharArray())
        assertThat(Marc8EscapeSequenceParser().parse(tracker)).isFalse
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.g0).isEqualTo(0x42)
        assertThat(tracker.g1).isEqualTo(0x45)
    }

    private val testTechnique1Data = listOf(
        Triple("Greek Symbols", "\u001Bgdata", 0x67),
        Triple("Subscript", "\u001Bbdata", 0x62),
        Triple("Superscript", "\u001Bpdata", 0x70),
        Triple("ASCII Default", "\u001Bsdata", 0x42)
    )

    @TestFactory
    fun `test parse(Tracker) with Technique 1 data`() = testTechnique1Data.map { (characterSet, data, isoCode) ->
        DynamicTest.dynamicTest(characterSet) {
            val tracker = CodeDataTracker(data.toCharArray())
            assertThat(Marc8EscapeSequenceParser().parse(tracker)).isTrue
            assertThat(tracker.offset).isEqualTo(2)
            assertThat(tracker.g0).isEqualTo(isoCode)
            assertThat(tracker.g1).isEqualTo(0x45)
        }
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G0 with invalid character set`() {
        val tracker = CodeDataTracker("\u001B(Xdata".toCharArray())
        assertThat(Marc8EscapeSequenceParser().parse(tracker)).isFalse
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.g0).isEqualTo(0x42)
        assertThat(tracker.g1).isEqualTo(0x45)
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G0 alt with invalid character set`() {
        val tracker = CodeDataTracker("\u001B,Xdata".toCharArray())
        assertThat(Marc8EscapeSequenceParser().parse(tracker)).isFalse
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.g0).isEqualTo(0x42)
        assertThat(tracker.g1).isEqualTo(0x45)
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G0 (Extended Latin) with invalid character set`() {
        val tracker = CodeDataTracker("\u001B(!Xdata".toCharArray())
        assertThat(Marc8EscapeSequenceParser().parse(tracker)).isFalse
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.g0).isEqualTo(0x42)
        assertThat(tracker.g1).isEqualTo(0x45)
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G0 alt (Extended Latin) with invalid character set`() {
        val tracker = CodeDataTracker("\u001B,!Xdata".toCharArray())
        assertThat(Marc8EscapeSequenceParser().parse(tracker)).isFalse
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.g0).isEqualTo(0x42)
        assertThat(tracker.g1).isEqualTo(0x45)
    }

    private val testTechnique2SingleByteG0Data = listOf(
        Triple("Basic Arabic", "(", 0x33),
        Triple("Basic Arabic Alt", ",", 0x33),
        Triple("Extended Arabic", "(", 0x34),
        Triple("Extended Arabic Alt", ",", 0x34),
        Triple("Basic Latin", "(", 0x42),
        Triple("Basic Latin Alt", ",", 0x42),
        Triple("Extended Latin", "(", 0x45),
        Triple("Extended Latin Alt", ",", 0x45),
        Triple("Extended Latin With Second Intermediate", "(!", 0x45),
        Triple("Extended Latin Alt With Second Intermediate", ",!", 0x45),
        Triple("Basic Cyrillic", "(", 0x4E),
        Triple("Basic Cyrillic Alt", ",", 0x4E),
        Triple("Extended Cyrillic", "(", 0x51),
        Triple("Extended Cyrillic Alt", ",", 0x51),
        Triple("Basic Greek", "(", 0x53),
        Triple("Basic Greek Alt", ",", 0x53),
        Triple("Basic Hebrew", "(", 0x32),
        Triple("Basic Hebrew Alt", ",", 0x32)
    )

    @TestFactory
    fun `test parse(Tracker) with Technique 2 single byte G0 data`() = testTechnique2SingleByteG0Data.map { (characterSet, intermediate, isoCode) ->
        DynamicTest.dynamicTest(characterSet) {
            val tracker =
                CodeDataTracker("\u001B$intermediate${isoCode.toChar()}".toCharArray())
            assertThat(Marc8EscapeSequenceParser().parse(tracker)).isTrue
            assertThat(tracker.offset).isEqualTo(2 + intermediate.length)
            assertThat(tracker.g0).isEqualTo(isoCode)
            assertThat(tracker.g1).isEqualTo(0x45)
        }
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G1 with invalid character set`() {
        val tracker = CodeDataTracker("\u001B\$Xdata".toCharArray())
        assertThat(Marc8EscapeSequenceParser().parse(tracker)).isFalse
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.g0).isEqualTo(0x42)
        assertThat(tracker.g1).isEqualTo(0x45)
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G1 alt with invalid character set`() {
        val tracker = CodeDataTracker("\u001B-Xdata".toCharArray())
        assertThat(Marc8EscapeSequenceParser().parse(tracker)).isFalse
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.g0).isEqualTo(0x42)
        assertThat(tracker.g1).isEqualTo(0x45)
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G1 (Extended Latin) with invalid character set`() {
        val tracker = CodeDataTracker("\u001B\$!Xdata".toCharArray())
        assertThat(Marc8EscapeSequenceParser().parse(tracker)).isFalse
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.g0).isEqualTo(0x42)
        assertThat(tracker.g1).isEqualTo(0x45)
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G1 alt (Extended Latin) with invalid character set`() {
        val tracker = CodeDataTracker("\u001B-!Xdata".toCharArray())
        assertThat(Marc8EscapeSequenceParser().parse(tracker)).isFalse
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.g0).isEqualTo(0x42)
        assertThat(tracker.g1).isEqualTo(0x45)
    }

    private val testTechnique2SingleByteG1Data = listOf(
        Triple("Basic Arabic", ")", 0x33),
        Triple("Basic Arabic Alt", "-", 0x33),
        Triple("Extended Arabic", ")", 0x34),
        Triple("Extended Arabic Alt", "-", 0x34),
        Triple("Basic Latin", ")", 0x42),
        Triple("Basic Latin Alt", "-", 0x42),
        Triple("Extended Latin", ")", 0x45),
        Triple("Extended Latin Alt", "-", 0x45),
        Triple("Extended Latin With Second Intermediate", ")!", 0x45),
        Triple("Extended Latin Alt With Second Intermediate", "-!", 0x45),
        Triple("Basic Cyrillic", ")", 0x4E),
        Triple("Basic Cyrillic Alt", "-", 0x4E),
        Triple("Extended Cyrillic", ")", 0x51),
        Triple("Extended Cyrillic Alt", "-", 0x51),
        Triple("Basic Greek", ")", 0x53),
        Triple("Basic Greek Alt", "-", 0x53),
        Triple("Basic Hebrew", ")", 0x32),
        Triple("Basic Hebrew Alt", "-", 0x32)
    )

    @TestFactory
    fun `test parse(Tracker) with Technique 2 single byte G1 data`() = testTechnique2SingleByteG1Data.map { (characterSet, intermediate, isoCode) ->
        DynamicTest.dynamicTest(characterSet) {
            val tracker =
                CodeDataTracker("\u001B$intermediate${isoCode.toChar()}".toCharArray())
            assertThat(Marc8EscapeSequenceParser().parse(tracker)).isTrue
            assertThat(tracker.offset).isEqualTo(2 + intermediate.length)
            assertThat(tracker.g0).isEqualTo(0x42)
            assertThat(tracker.g1).isEqualTo(isoCode)
        }
    }

    @Test
    fun `test parse(Tracker) with Technique 2 multi byte G0 with invalid character set`() {
        val tracker = CodeDataTracker("\u001B\$Xdata".toCharArray())
        assertThat(Marc8EscapeSequenceParser().parse(tracker)).isFalse
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.g0).isEqualTo(0x42)
        assertThat(tracker.g1).isEqualTo(0x45)
    }

    @Test
    fun `test parse(Tracker) with Technique 2 multi byte G1 with invalid character set`() {
        val tracker = CodeDataTracker("\u001B\$-Xdata".toCharArray())
        assertThat(Marc8EscapeSequenceParser().parse(tracker)).isFalse
        assertThat(tracker.offset).isEqualTo(0)
        assertThat(tracker.g0).isEqualTo(0x42)
        assertThat(tracker.g1).isEqualTo(0x45)
    }

    private val testTechnique2MultiByteData = listOf(
        Triple("$", second = true, third = false),
        Triple("$,", second = true, third = false),
        Triple("$)", second = false, third = true),
        Triple("$-", second = false, third = true)
    )

    @TestFactory
    fun `test parse(Tracker) with Technique 2 multi byte data`() = testTechnique2MultiByteData.map { (intermediate, isG0, isG1) ->
        val display = "Using intermediate : '$intermediate'"
        DynamicTest.dynamicTest(display) {
            val tracker = CodeDataTracker("\u001B${intermediate}1data".toCharArray())
            assertThat(Marc8EscapeSequenceParser().parse(tracker)).isTrue
            assertThat(tracker.offset).isEqualTo(2 + intermediate.length)
            assertThat(tracker.g0).isEqualTo(if (isG0) 0x31 else 0x42)
            assertThat(tracker.g1).isEqualTo(if (isG1) 0x31 else 0x45)
        }
    }
}