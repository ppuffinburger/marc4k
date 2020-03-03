package org.marc4k.io.converter.marc8

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.marc4k.IsoCode
import org.marc4k.MarcException

internal class Marc8EscapeSequenceGeneratorTest {
    private val escapeSequenceGenerator = Marc8EscapeSequenceGenerator()

    private val testData = listOf(
        TestData("Subscript - No Escape", 0x62,
            CodeTableTracker(0x62),
            CodeTableTracker(0x62), ""),
        TestData("Subscript - Escape", 0x62,
            CodeTableTracker(), CodeTableTracker().copy(0x62), "\u001B\u0062"),
        TestData("Greek Symbols - No Escape", 0x67,
            CodeTableTracker(0x67),
            CodeTableTracker(0x67), ""),
        TestData("Greek Symbols - Escape", 0x67,
            CodeTableTracker(), CodeTableTracker().copy(0x67), "\u001B\u0067"),
        TestData("Superscript - No Escape", 0x70,
            CodeTableTracker(0x70),
            CodeTableTracker(0x70), ""),
        TestData("Superscript - Escape", 0x70,
            CodeTableTracker(), CodeTableTracker().copy(0x70), "\u001B\u0070"),
        TestData("Basic Arabic - No Escape", 0x33,
            CodeTableTracker(0x33),
            CodeTableTracker(0x33), ""),
        TestData("Basic Arabic - Escape", 0x33,
            CodeTableTracker(), CodeTableTracker().copy(0x33), "\u001B\u0028\u0033"),
        TestData("Basic Latin - No Escape", 0x42,
            CodeTableTracker(),
            CodeTableTracker(), ""),
        TestData("Basic Latin - Escape", 0x42,
            CodeTableTracker(0x31), CodeTableTracker(0x31).copy(0x42), "\u001B\u0028\u0042"),
        TestData("Basic Cyrillic - No Escape", 0x4E,
            CodeTableTracker(0x4E),
            CodeTableTracker(0x4E), ""),
        TestData("Basic Cyrillic - Escape", 0x4E,
            CodeTableTracker(), CodeTableTracker().copy(0x4E), "\u001B\u0028\u004E"),
        TestData("Basic Greek - No Escape", 0x53,
            CodeTableTracker(0x53),
            CodeTableTracker(0x53), ""),
        TestData("Basic Greek - Escape", 0x53,
            CodeTableTracker(), CodeTableTracker().copy(0x53), "\u001B\u0028\u0053"),
        TestData("Basic Hebrew - No Escape", 0x32,
            CodeTableTracker(0x32),
            CodeTableTracker(0x32), ""),
        TestData("Basic Hebrew - Escape", 0x32,
            CodeTableTracker(), CodeTableTracker().copy(0x32), "\u001B\u0028\u0032"),
        TestData("Extended Arabic - No Escape", 0x34,
            CodeTableTracker(g1 = 0x34),
            CodeTableTracker(g1 = 0x34), ""),
        TestData("Extended Arabic - Escape", 0x34,
            CodeTableTracker(), CodeTableTracker().copy(g1 = 0x34), "\u001B\u0029\u0034"),
        TestData("Extended Cyrillic - No Escape", 0x51,
            CodeTableTracker(g1 = 0x51),
            CodeTableTracker(g1 = 0x51), ""),
        TestData("Extended Cyrillic - Escape", 0x51,
            CodeTableTracker(), CodeTableTracker().copy(g1 = 0x51), "\u001B\u0029\u0051"),
        TestData("Extended Latin - No Escape", 0x45,
            CodeTableTracker(),
            CodeTableTracker(), ""),
        TestData("Extended Latin - Escape", 0x45,
            CodeTableTracker(g1 = 0x31), CodeTableTracker(g1 = 0x31).copy(g1 = 0x45), "\u001B\u0029\u0021\u0045"),
        TestData("CJK - No Escape", 0x31,
            CodeTableTracker(0x31),
            CodeTableTracker(0x31), ""),
        TestData("CJK - Escape", 0x31,
            CodeTableTracker(), CodeTableTracker().copy(0x31), "\u001B\u0024\u0031")
        )

    @TestFactory
    fun `test generate(IsoCode, CodeTableTracker)`() = testData.map { (description, given, originalCodeTableTracker, newCodeTableTracker, expected) ->
        DynamicTest.dynamicTest(description) {
            assertThat(escapeSequenceGenerator.generate(given, originalCodeTableTracker)).isEqualTo(expected)
            assertThat(originalCodeTableTracker).isEqualTo(newCodeTableTracker)
        }
    }

    @Test
    fun `test generate(IsoCode, CodeTableTracker) with invalid IsoCode throws Exception`() {
        assertThatExceptionOfType(MarcException::class.java).isThrownBy { escapeSequenceGenerator.generate(0xFF,
            CodeTableTracker()
        ) }
    }

    private data class TestData(val description: String, val given: IsoCode, val originalCodeTableTracker: CodeTableTracker, val newCodeTableTracker: CodeTableTracker, val expected: String)
}