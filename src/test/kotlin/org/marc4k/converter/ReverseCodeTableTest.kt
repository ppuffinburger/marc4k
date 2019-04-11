package org.marc4k.converter

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.marc4k.MarcException
import org.marc4k.marc8CodeToHex
import org.marc4k.unicodeToHex
import java.io.File

internal class ReverseCodeTableTest {
    private val codeTable = ReverseCodeTable(javaClass.getResourceAsStream("/codetables.xml"))

    @Test
    fun `test constructor(String)`() {
        val given = ReverseCodeTable("src/main/resources/codetablesnocjk.xml")
        Assertions.assertThat(given).isNotNull
    }

    @Test
    fun `test constructor(URI)`() {
        val given = ReverseCodeTable(File("src/main/resources/codetablesnocjk.xml").toURI())
        Assertions.assertThat(given).isNotNull
    }

    @Test
    fun `test constructor(InputStream) with bad data stream`() {
        Assertions.assertThatExceptionOfType(MarcException::class.java).isThrownBy { ReverseCodeTable("bad_data_stream".byteInputStream()) }
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

    private val getCharTestData = listOf(
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
    fun `test getMarc8Array(Char, IsoCode)`() = getCharTestData.map { (character, isoCode, expected) ->
        DynamicTest.dynamicTest("Given '${unicodeToHex(character)}}' and ISOCode of '${String.format("0x%02x", isoCode)}', expect getMarc8Array(Char, IsoCode) to be '${expected.map { marc8CodeToHex(it) }}'") {
            assertThat(codeTable.getMarc8Array(character, isoCode)).isEqualTo(expected)
        }
    }
}