package org.marc4k.io.converter

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.marc4k.IsoCode
import org.marc4k.MarcException
import org.marc4k.io.converter.marc8.CodeTableTracker
import org.marc4k.marc8CodeToHex
import org.marc4k.unicodeToHex
import java.io.File

internal class ReverseCodeTableTest {
    private val codeTable = ReverseCodeTable(javaClass.getResourceAsStream("/codetables.xml"))

    @Test
    fun `test constructor(String)`() {
        val given = ReverseCodeTable("src/main/resources/codetablesnocjk.xml")
        assertThat(given).isNotNull
    }

    @Test
    fun `test constructor(URI)`() {
        val given = ReverseCodeTable(File("src/main/resources/codetablesnocjk.xml").toURI())
        assertThat(given).isNotNull
    }

    @Test
    fun `test constructor(InputStream) with bad data stream`() {
        assertThatExceptionOfType(MarcException::class.java).isThrownBy { ReverseCodeTable("bad_data_stream".byteInputStream()) }
    }

    private val isCombiningTestData = listOf(
        '\u001F' to false,
        '\u0020' to false,
        '\u0041' to false,
        '\u0061' to false,
        '\u007E' to false,
        '\u007F' to false,
        '\u0085' to false,
        '\u0142' to false,
        '\u0309' to true,
        '\u0327' to true,
        '\u0313' to true,
        '\u00FF' to false
    )

    @TestFactory
    fun `test isCombining(Char)`() = isCombiningTestData.map { (character, expected) ->
        DynamicTest.dynamicTest("Given '${unicodeToHex(character)}', expect isCombining(Char) to be '$expected'") {
            assertThat(codeTable.isCombining(character)).isEqualTo(expected)
        }
    }

    private val getMarc8ArrayTestData = listOf(
        Triple('\u000D', 0x42, charArrayOf()),
        Triple(' ', 0x42, charArrayOf('\u0020')),
        Triple('A', 0x42, charArrayOf('\u0041')),
        Triple('ℓ', 0x45, charArrayOf('\u00C1')),
        Triple('?', 0x42, charArrayOf('\u003F')),
        Triple('?', 0x4E, charArrayOf('\u003F')),
        Triple('A', 0x01, charArrayOf()),
        Triple('フ', 0x31, charArrayOf('\u0069', '\u0025', '\u0055'))
    )

    @TestFactory
    fun `test getMarc8Array(Char, IsoCode)`() = getMarc8ArrayTestData.map { (character, isoCode, expected) ->
        DynamicTest.dynamicTest("Given '${unicodeToHex(character)}' and ISOCode of '${String.format("0x%02x", isoCode)}', expect getMarc8Array(Char, IsoCode) to be '${expected.map { marc8CodeToHex(it) }}'") {
            assertThat(codeTable.getMarc8Array(character, isoCode)).isEqualTo(expected)
        }
    }

    private val isCharacterInCodeTableTestData = listOf(
        ' ' to true,
        'A' to true,
        'ℓ' to true,
        '?' to true,
        'フ' to true,
        '₂' to true,
        'ḱ' to false,
        'ṷ' to false,
        'ṓ' to false
    )

    @TestFactory
    fun `test isCharacterInCodeTable(Char)`() = isCharacterInCodeTableTestData.map { (character, expected) ->
        DynamicTest.dynamicTest("Given '${unicodeToHex(character)}, expect isCharacterInCodeTable(Char) to be '$expected'") {
            assertThat(codeTable.isCharacterInCodeTable(character)).isEqualTo(expected)
        }
    }

    private val isCharacterInCurrentCharacterSetTestData = listOf(
        Triple(' ', 0x42, true),
        Triple(' ', 0x45, false),
        Triple('A', 0x42, true),
        Triple('A', 0x45, false),
        Triple('ℓ', 0x42, false),
        Triple('ℓ', 0x45, true),
        Triple('?', 0x42, true),
        Triple('?', 0x4E, true),
        Triple('A', 0x01, false),
        Triple('フ', 0x31, true),
        Triple('フ', 0x42, false),
        Triple('ṷ', 0x31, false)
    )

    @TestFactory
    fun `test isCharacterInCurrentCharacterSet(Char, IsoCode)`() = isCharacterInCurrentCharacterSetTestData.map { (character, isoCode, expected) ->
        DynamicTest.dynamicTest("Given '${unicodeToHex(character)}' and ISOCode of '${String.format("0x%02x", isoCode)}', expect isCharacterInCurrentCharacterSet(Char, IsoCode) to be '$expected'") {
            assertThat(codeTable.isCharacterInCurrentCharacterSet(character, isoCode)).isEqualTo(expected)
        }
    }

    private val getBestCharacterSetTestData = listOf(
        BestCharacterSetData("Space", ' ',
            CodeTableTracker(), 0x42,
            CodeTableTracker(characterSetsUsed = mutableSetOf(0x42, 0x45))
        ),
        BestCharacterSetData("Exclamation outside of used character sets", '!',
            CodeTableTracker(0x31, 0x34), 0x42,
            CodeTableTracker(0x31, 0x34, mutableSetOf(0x31, 0x34, 0x42))
        ),
        BestCharacterSetData("Cyrillic Tshe", 'Ћ',
            CodeTableTracker(), 0x51,
            CodeTableTracker(characterSetsUsed = mutableSetOf(0x42, 0x45, 0x51))
        ),
        BestCharacterSetData("Arabic Noon with 3 Dots Above", 'ڽ',
            CodeTableTracker(), 0x34,
            CodeTableTracker(characterSetsUsed = mutableSetOf(0x42, 0x45, 0x34))
        ),
        BestCharacterSetData("Greek Capital Alpha", 'Α',
            CodeTableTracker(), 0x53,
            CodeTableTracker(characterSetsUsed = mutableSetOf(0x42, 0x45, 0x53))
        ),
        BestCharacterSetData("Greek Small Alpha", 'α',
            CodeTableTracker(), 0x53,
            CodeTableTracker(characterSetsUsed = mutableSetOf(0x42, 0x45, 0x53))
        ),
        BestCharacterSetData("Right Double Quotation Mark (Greek)", '”',
            CodeTableTracker(), 0x53,
            CodeTableTracker(characterSetsUsed = mutableSetOf(0x42, 0x45, 0x53))
        ),
        BestCharacterSetData("Right Double Quotation Mark (Arabic)", '”',
            CodeTableTracker(0x33, 0x34), 0x33,
            CodeTableTracker(0x33, 0x34, mutableSetOf(0x33, 0x34))
        ),
        BestCharacterSetData("Character not found in any code tables", 'ṷ',
            CodeTableTracker(), null,
            CodeTableTracker()
        )
    )

    @TestFactory
    fun `test getBestCharacterSet(Char, MutableSet)`() = getBestCharacterSetTestData.map { (description, character, originalCodeTableTracker, expectedCharacterSet, expectedCodeTableTracker) ->
        DynamicTest.dynamicTest(description) {
            assertThat(codeTable.getBestCharacterSet(character, originalCodeTableTracker.characterSetsUsed)).isEqualTo(expectedCharacterSet)
            assertThat(originalCodeTableTracker).isEqualTo(expectedCodeTableTracker)
        }
    }

    private data class BestCharacterSetData(val description: String, val character: Char, val originalCodeTableTracker: CodeTableTracker, val expectedCharacterSet: IsoCode?, val codeTableTracker: CodeTableTracker)
}