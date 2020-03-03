package org.marc4k.io.converter

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.marc4k.MARC8_CODE_HEX_PATTERN
import org.marc4k.MarcException
import java.io.File

internal class CodeTableTest {
    private val codeTable = CodeTable(javaClass.getResourceAsStream("/codetablesnocjk.xml"))

    @Test
    fun `test constructor(String)`() {
        val given = CodeTable("src/main/resources/codetablesnocjk.xml")
        assertThat(given).isNotNull
    }

    @Test
    fun `test constructor(URI)`() {
        val given = CodeTable(File("src/main/resources/codetablesnocjk.xml").toURI())
        assertThat(given).isNotNull
    }

    @Test
    fun `test constructor(InputStream) with bad data stream`() {
        assertThatExceptionOfType(MarcException::class.java).isThrownBy { CodeTable("bad_data_stream".byteInputStream()) }
    }

    private val isCombiningTestData = listOf(
        0x1F to false,
        0x20 to false,
        0x41 to false,
        0x61 to false,
        0x7E to false,
        0x7F to false,
        0x85 to false,
        0xB1 to false,
        0xE0 to true,
        0xF0 to true,
        0xFE to true,
        0xFF to false
    )

    @TestFactory
    fun `test isCombining(Marc8Code, IsoCode, IsoCode)`() = isCombiningTestData.map { (marc8Code, expected) ->
        DynamicTest.dynamicTest("Given '${String.format(MARC8_CODE_HEX_PATTERN, marc8Code)}', expect isCombining() to be '$expected'") {
            assertThat(codeTable.isCombining(marc8Code, 0x42, 0x45)).isEqualTo(expected)
        }
    }

    private val getCharTestData = listOf(
        Triple(0x0D, 0x42, null),
        Triple(0x20, 0x42, ' '),
        Triple(0x41, 0x42, 'A'),
        Triple(0xC1, 0x42, 'A'),
        Triple(0x20, 0x45, ' '),
        Triple(0xC1, 0x45, 'ℓ'),
        Triple(0x41, 0x45, 'ℓ'),
        Triple(0x41, 0x01, 'A')     // unknown ISOCode return marc8Code.toChar()
    )

    @TestFactory
    fun `test getChar(Marc8Code, IsoCode)`() = getCharTestData.map { (marc8Code, isoCode, expected) ->
        DynamicTest.dynamicTest("Given MARC8 of '${String.format(MARC8_CODE_HEX_PATTERN, marc8Code)}' and ISOCode of '${String.format("0x%02x", isoCode)}', expect getChar() to be '$expected'") {
            assertThat(codeTable.getChar(marc8Code, isoCode)).isEqualTo(expected)
        }
    }
}