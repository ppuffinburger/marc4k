package org.marc4k.converter

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.marc4k.converter.marc8.Marc8EscapeSequenceParser
import org.marc4k.converter.marc8.Marc8Tracker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class Marc8EscapeSequenceParserTest {
    @Test
    fun `test parse(Tracker) with no escape sequence`() {
        val tracker = Marc8Tracker("data".toCharArray())
        assertFalse { Marc8EscapeSequenceParser().parse(tracker) }
        assertEquals(0, tracker.offset)
        assertEquals(0x42, tracker.g0)
        assertEquals(0x45, tracker.g1)
    }

    @Test
    fun `test parse(Tracker) with invalid designator`() {
        val tracker = Marc8Tracker("\u001B\u002Fdata".toCharArray())
        assertFalse { Marc8EscapeSequenceParser().parse(tracker) }
        assertEquals(0, tracker.offset)
        assertEquals(0x42, tracker.g0)
        assertEquals(0x45, tracker.g1)
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
            val tracker = Marc8Tracker(data.toCharArray())
            assertTrue { Marc8EscapeSequenceParser().parse(tracker) }
            assertEquals(2, tracker.offset)
            assertEquals(isoCode, tracker.g0)
            assertEquals(0x45, tracker.g1)
        }
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G0 with invalid character set`() {
        val tracker = Marc8Tracker("\u001B(Xdata".toCharArray())
        assertFalse { Marc8EscapeSequenceParser().parse(tracker) }
        assertEquals(0, tracker.offset)
        assertEquals(0x42, tracker.g0)
        assertEquals(0x45, tracker.g1)
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G0 alt with invalid character set`() {
        val tracker = Marc8Tracker("\u001B,Xdata".toCharArray())
        assertFalse { Marc8EscapeSequenceParser().parse(tracker) }
        assertEquals(0, tracker.offset)
        assertEquals(0x42, tracker.g0)
        assertEquals(0x45, tracker.g1)
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G0 (Extended Latin) with invalid character set`() {
        val tracker = Marc8Tracker("\u001B(!Xdata".toCharArray())
        assertFalse { Marc8EscapeSequenceParser().parse(tracker) }
        assertEquals(0, tracker.offset)
        assertEquals(0x42, tracker.g0)
        assertEquals(0x45, tracker.g1)
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G0 alt (Extended Latin) with invalid character set`() {
        val tracker = Marc8Tracker("\u001B,!Xdata".toCharArray())
        assertFalse { Marc8EscapeSequenceParser().parse(tracker) }
        assertEquals(0, tracker.offset)
        assertEquals(0x42, tracker.g0)
        assertEquals(0x45, tracker.g1)
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
                Marc8Tracker("\u001B$intermediate${isoCode.toChar()}".toCharArray())
            assertTrue { Marc8EscapeSequenceParser().parse(tracker) }
            assertEquals(2 + intermediate.length, tracker.offset)
            assertEquals(isoCode, tracker.g0)
            assertEquals(0x45, tracker.g1)
        }
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G1 with invalid character set`() {
        val tracker = Marc8Tracker("\u001B\$Xdata".toCharArray())
        assertFalse { Marc8EscapeSequenceParser().parse(tracker) }
        assertEquals(0, tracker.offset)
        assertEquals(0x42, tracker.g0)
        assertEquals(0x45, tracker.g1)
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G1 alt with invalid character set`() {
        val tracker = Marc8Tracker("\u001B-Xdata".toCharArray())
        assertFalse { Marc8EscapeSequenceParser().parse(tracker) }
        assertEquals(0, tracker.offset)
        assertEquals(0x42, tracker.g0)
        assertEquals(0x45, tracker.g1)
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G1 (Extended Latin) with invalid character set`() {
        val tracker = Marc8Tracker("\u001B\$!Xdata".toCharArray())
        assertFalse { Marc8EscapeSequenceParser().parse(tracker) }
        assertEquals(0, tracker.offset)
        assertEquals(0x42, tracker.g0)
        assertEquals(0x45, tracker.g1)
    }

    @Test
    fun `test parse(Tracker) with Technique 2 single byte G1 alt (Extended Latin) with invalid character set`() {
        val tracker = Marc8Tracker("\u001B-!Xdata".toCharArray())
        assertFalse { Marc8EscapeSequenceParser().parse(tracker) }
        assertEquals(0, tracker.offset)
        assertEquals(0x42, tracker.g0)
        assertEquals(0x45, tracker.g1)
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
                Marc8Tracker("\u001B$intermediate${isoCode.toChar()}".toCharArray())
            assertTrue { Marc8EscapeSequenceParser().parse(tracker) }
            assertEquals(2 + intermediate.length, tracker.offset)
            assertEquals(0x42, tracker.g0)
            assertEquals(isoCode, tracker.g1)
        }
    }

    @Test
    fun `test parse(Tracker) with Technique 2 multi byte G0 with invalid character set`() {
        val tracker = Marc8Tracker("\u001B\$Xdata".toCharArray())
        assertFalse { Marc8EscapeSequenceParser().parse(tracker) }
        assertEquals(0, tracker.offset)
        assertEquals(0x42, tracker.g0)
        assertEquals(0x45, tracker.g1)
    }

    @Test
    fun `test parse(Tracker) with Technique 2 multi byte G1 with invalid character set`() {
        val tracker = Marc8Tracker("\u001B\$-Xdata".toCharArray())
        assertFalse { Marc8EscapeSequenceParser().parse(tracker) }
        assertEquals(0, tracker.offset)
        assertEquals(0x42, tracker.g0)
        assertEquals(0x45, tracker.g1)
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
            val tracker = Marc8Tracker("\u001B${intermediate}1data".toCharArray())
            assertTrue { Marc8EscapeSequenceParser().parse(tracker) }
            assertEquals(2 + intermediate.length, tracker.offset)
            assertEquals(if (isG0) 0x31 else 0x42, tracker.g0)
            assertEquals(if (isG1) 0x31 else 0x45, tracker.g1)
        }
    }
}