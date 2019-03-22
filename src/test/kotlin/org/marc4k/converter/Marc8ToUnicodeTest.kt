package org.marc4k.converter

import org.junit.jupiter.api.Test
import org.marc4k.converter.marc8.Marc8ToUnicode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class Marc8ToUnicodeTest {
    @Test
    fun `test outputsUnicode()`() {
        val marc8ToUnicode = Marc8ToUnicode()
        assertTrue { marc8ToUnicode.outputsUnicode() }
    }

    @Test
    fun `test non-translation of NCR data`() {
        val marc8ToUnicode = Marc8ToUnicode(translateNcr = false)
        assertEquals("&#x0031;", (marc8ToUnicode.convert("&#x0031;") as NoErrors).conversion)
    }

    @Test
    fun `test translation of NCR data`() {
        val marc8ToUnicode = Marc8ToUnicode(translateNcr = true)
        assertEquals("1", (marc8ToUnicode.convert("&#x0031;") as NoErrors).conversion)
    }

    @Test
    fun `test conversion of CJK without loading CJK code table using constructor(InputStream)`() {
        val marc8ToUnicode =
            Marc8ToUnicode(javaClass.getResourceAsStream("/codetablesnocjk.xml"))
        assertEquals("╕┣╪╃╗", (marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042") as NoErrors).conversion)
    }

    @Test
    fun `test conversion of CJK with loading CJK code table using constructor(InputStream)`() {
        val marc8ToUnicode = Marc8ToUnicode(javaClass.getResourceAsStream("/codetables.xml"))
        assertEquals("フィリップ", (marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042") as NoErrors).conversion)
    }

    @Test
    fun `test conversion of CJK without loading CJK code table using constructor(String)`() {
        val marc8ToUnicode = Marc8ToUnicode("src/main/resources/codetablesnocjk.xml")
        assertEquals("╕┣╪╃╗", (marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042") as NoErrors).conversion)
    }

    @Test
    fun `test conversion of CJK with loading CJK code table using constructor(String)`() {
        val marc8ToUnicode = Marc8ToUnicode("src/main/resources/codetables.xml")
        assertEquals("フィリップ", (marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042") as NoErrors).conversion)
    }

    @Test
    fun `test auto-loading of CJK code table`() {
        val marc8ToUnicode = Marc8ToUnicode(loadMultiByteCodeTable = false)
        assertEquals("フィリップ", (marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042") as NoErrors).conversion)
    }

    @Test
    fun `test convert(CharArray)`() {
        val marc8ToUnicode = Marc8ToUnicode()
        assertEquals("フィリップ", (marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042".toCharArray()) as NoErrors).conversion)
    }

    @Test
    fun `test convert(ByteArray)`() {
        val marc8ToUnicode = Marc8ToUnicode()
        assertEquals("フィリップ", (marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042".toByteArray()) as NoErrors).conversion)
    }

    @Test
    fun `test convert(String)`() {
        val marc8ToUnicode = Marc8ToUnicode()
        assertEquals("フィリップ", (marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042") as NoErrors).conversion)
    }

    @Test
    fun `test diacritics`() {
        val marc8ToUnicode = Marc8ToUnicode()
        val letterAWithAcuteAccent = "\u00E2a"
        val letterAWithCircumflexAndDotBelow = "\u00E3\u00F2a"
        val combiningDoubleTilde = "\u00FAa\u00FBi"
        val combiningDoubleBreve = "\u00EBa\u00ECi"

        // the following share the same Marc8 code as Extended Latin Combining Double Breve
        val cyrillicCapitalTshe = "\u001B\u0029\u0051\u00EB"
        val arabicLetterNoonWithThreeDotsAbove = "\u001B\u0029\u0034\u00EB"

        assertEquals("á", (marc8ToUnicode.convert(letterAWithAcuteAccent.toCharArray()) as NoErrors).conversion)
        assertEquals("ậ", (marc8ToUnicode.convert(letterAWithCircumflexAndDotBelow.toCharArray()) as NoErrors).conversion)
        assertEquals("a͠i", (marc8ToUnicode.convert(combiningDoubleTilde.toCharArray()) as NoErrors).conversion)
        assertEquals("a͡i", (marc8ToUnicode.convert(combiningDoubleBreve.toCharArray()) as NoErrors).conversion)
        assertEquals("Ћ", (marc8ToUnicode.convert(cyrillicCapitalTshe.toCharArray()) as NoErrors).conversion)
        assertEquals("ڽ", (marc8ToUnicode.convert(arabicLetterNoonWithThreeDotsAbove.toCharArray()) as NoErrors).conversion)
    }
}