package org.marc4k.io.converter.marc8

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertAll
import org.marc4k.io.converter.CharacterConverterResult

internal class Marc8ToUnicodeTest {
    private val marc8ToUnicode = Marc8ToUnicode()

    @Test
    fun `test outputsUnicode()`() {
        assertThat(marc8ToUnicode.outputsUnicode()).isTrue
    }

    @Test
    fun `test non-translation of NCR data`() {
        val given = Marc8ToUnicode(translateNcr = false).convert("&#x0031;".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("&#x0031;") }
        )
    }

    @Test
    fun `test translation of NCR data`() {
        val given = Marc8ToUnicode(translateNcr = true).convert("&#x0031;".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("1") }
        )
    }

    @Test
    fun `test conversion of CJK without loading CJK code table using constructor(InputStream)`() {
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        val marc8ToUnicode = Marc8ToUnicode(javaClass.getResourceAsStream("/codetablesnocjk.xml"))
        val given = marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("╕┣╪╃╗") }
        )
    }

    @Test
    fun `test conversion of CJK with loading CJK code table using constructor(InputStream)`() {
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        val marc8ToUnicode = Marc8ToUnicode(javaClass.getResourceAsStream("/codetables.xml"))
        val given = marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("フィリップ") }
        )
    }

    @Test
    fun `test conversion of CJK without loading CJK code table using constructor(String)`() {
        val marc8ToUnicode = Marc8ToUnicode("src/main/resources/codetablesnocjk.xml")
        val given = marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("╕┣╪╃╗") }
        )
    }

    @Test
    fun `test conversion of CJK with loading CJK code table using constructor(String)`() {
        val marc8ToUnicode = Marc8ToUnicode("src/main/resources/codetables.xml")
        val given = marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("フィリップ") }
        )
    }

    @Test
    fun `test auto-loading of CJK code table`() {
        val marc8ToUnicode = Marc8ToUnicode(loadMultiByteCodeTable = false)
        val given = marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("フィリップ") }
        )
    }

    @Test
    fun `test convert(CharArray)`() {
        val given = marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("フィリップ") }
        )
    }

    @Test
    fun `test convert(ByteArray)`() {
        val given = marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042".toByteArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("フィリップ") }
        )
    }

    @Test
    fun `test convert(String)`() {
        val given = marc8ToUnicode.convert("\u001b\u0024\u0031\u0069\u0025\u0055\u0069\u0025\u0023\u0069\u0025\u006a\u0069\u0025\u0043\u0069\u0025\u0057\u001b\u0028\u0042")
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("フィリップ") }
        )
    }

    private val testDiacriticsData = listOf(
        Triple("A with Acute", "\u00E2a", "á"),
        Triple("A With Circumflex and Dot Below", "\u00E3\u00F2a", "ậ"),
        Triple("Combining Double Tilde", "\u00FAa\u00FBi", "a͠i"),
        Triple("Combining Double Inverted Breve", "\u00EBa\u00ECi", "a͡i"),
        Triple("Combining Double Inverted Breve with Space", "\u00EB \u00ECi", " ͡i"),
        Triple("Combining Double Inverted Breve with Page Switching", "\u00EB\u001B\u0028\u004E\u005A\u00EC\u001B\u0028\u0042\u0073", "з͡s")
    )

    @TestFactory
    fun `test convert(CharArray) with diacritics`() = testDiacriticsData.map { (description, data, expected) ->
        DynamicTest.dynamicTest(description) {
            val given = marc8ToUnicode.convert(data.toCharArray())
            assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java)
            assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo(expected)
        }
    }

    @Test
    fun `test convert(CharArray) with Cyrillic Capital Tshe`() {
        // 0xEB which is the start of Combining Double Inverted Breve is also a valid character in other character sets
        val given = marc8ToUnicode.convert("\u001B\u0029\u0051\u00EB".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("Ћ") }
        )
    }

    @Test
    fun `test convert(CharArray) with Arabic Noon with Three Dots Above`() {
        // 0xEB which is the start of Combining Double Inverted Breve is also a valid character in other character sets
        val given = marc8ToUnicode.convert("\u001B\u0029\u0034\u00EB".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("ڽ") }
        )
    }

    @Test
    fun `test convert(CharArray) with incomplete escape sequence`() {
        val given = marc8ToUnicode.convert("da\u001Bta".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.WithErrors::class.java) },
            { assertThat((given as CharacterConverterResult.WithErrors).conversion).isEqualTo("data") }
        )
    }

    @Test
    fun `test convert(CharArray) with only Combining Double Inverted Breve first half`() {
        val given = marc8ToUnicode.convert("\u00EBai ")
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("a͡i ") }
        )
    }

    @Test
    fun `test convert(CharArray) with only Combining Double Inverted Breve second half`() {
        val given = marc8ToUnicode.convert("a\u00ECi ")
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("ai ") }
        )
    }

    @Test
    fun `test convert(CharArray) with only Combining Double Tilde first half`() {
        val given = marc8ToUnicode.convert("\u00FAai ")
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("a͠i ") }
        )
    }

    @Test
    fun `test convert(CharArray) with only Combining Double Tilde second half`() {
        val given = marc8ToUnicode.convert("a\u00FBi ")
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("ai ") }
        )
    }

    @Test
    fun `test convert(CharArray) with invalid escape sequence`() {
        val given = marc8ToUnicode.convert("da\u001B\u0028ta".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.WithErrors::class.java) },
            { assertThat((given as CharacterConverterResult.WithErrors).conversion).isEqualTo("da(ta") }
        )
    }

    @Test
    fun `test convert(CharArray) with space`() {
        val given = marc8ToUnicode.convert(" ".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo(" ") }
        )
    }

    @Test
    fun `test convert(CharArray) with C0 control character`() {
        val given = marc8ToUnicode.convert("da\u000Dta".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.WithErrors::class.java) },
            { assertThat((given as CharacterConverterResult.WithErrors).conversion).isEqualTo("data") }
        )
    }

    @Test
    fun `test convert(CharArray) with C1 control character`() {
        val given = marc8ToUnicode.convert("da\u009Dta".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.WithErrors::class.java) },
            { assertThat((given as CharacterConverterResult.WithErrors).conversion).isEqualTo("data") }
        )
    }

    @Test
    fun `test convert(CharArray) with zero width joiner`() {
        val given = marc8ToUnicode.convert("\u008D".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("\u200D") }
        )
    }

    @Test
    fun `test convert(CharArray) with zero width non-joiner`() {
        val given = marc8ToUnicode.convert("\u008E".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("\u200C") }
        )
    }

    @Test
    fun `test convert(CharArray) with non-sort begin character`() {
        val given = marc8ToUnicode.convert("\u0088".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("\u0098") }
        )
    }

    @Test
    fun `test convert(CharArray) with non-sort end character`() {
        val given = marc8ToUnicode.convert("\u0089".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("\u009C") }
        )
    }

    @Test
    fun `test convert(CharArray) with EACC and no characters`() {
        val given = marc8ToUnicode.convert("\u001B\u0024\u0031\u0079\u0079".toCharArray())
        assertAll("No valid EACC characters in sequence",
            { assertThat(given).isInstanceOf(CharacterConverterResult.WithErrors::class.java) },
            { assertThat((given as CharacterConverterResult.WithErrors).conversion).isEqualTo("") }
        )
    }

    @Test
    fun `test convert(CharArray) with EACC and one character`() {
        val given = marc8ToUnicode.convert("\u001B\u0024\u0031\u0069\u0079".toCharArray())
        assertAll("One valid EACC character in sequence",
            { assertThat(given).isInstanceOf(CharacterConverterResult.WithErrors::class.java) },
            { assertThat((given as CharacterConverterResult.WithErrors).conversion).isEqualTo("") }
        )
    }

    @Test
    fun `test convert(CharArray) with EACC and two characters`() {
        val given = marc8ToUnicode.convert("\u001B\u0024\u0031\u0069\u0025".toCharArray())
        assertAll("Two valid EACC characters in sequence",
            { assertThat(given).isInstanceOf(CharacterConverterResult.WithErrors::class.java) },
            { assertThat((given as CharacterConverterResult.WithErrors).conversion).isEqualTo("") }
        )
    }

    @Test
    fun `test convert(CharArray) with EACC and three characters`() {
        val given = marc8ToUnicode.convert("\u001B\u0024\u0031\u0069\u0025\u0042".toCharArray())
        assertAll("Three valid EACC characters in sequence",
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("ヂ") }
        )
    }

    @Test
    fun `test convert(CharArray) with non-EACC character in EACC mode`() {
        val given = marc8ToUnicode.convert("\u001B\u0024\u0029\u0031\u0041".toCharArray())
        assertAll("Valid non-EACC character while in EACC mode",
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("A") }
        )
    }

    @Test
    fun `test convert(CharArray) with diacritics with no base character`() {
        val given = marc8ToUnicode.convert("\u00E3".toCharArray())
        assertThat(given).isInstanceOf(CharacterConverterResult.WithErrors::class.java)
    }

    @Test
    fun `test convert(CharArray) with diacritic preceding escape sequence`() {
        val given = marc8ToUnicode.convert("Hebrew page (\u001B(2hpfio bi`hqe = ò\u001B(BTenzin Gi®aòtso)".toCharArray())
        assertAll("",
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("Hebrew page (טנזין גיאטסו = Ṭenzin Giʼaṭso)") }
        )
    }

    @Test
    fun `test convert(CharArray) with invalid character`() {
        val given = marc8ToUnicode.convert("\u00AF".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.WithErrors::class.java) },
            { assertThat((given as CharacterConverterResult.WithErrors).conversion).isEqualTo("") }
        )
    }

    @Test
    fun `test convert(CharArray) with vulgar fractions`() {
        // Some vulgar fractions seem to be routinely encoded wrong in MARC8 records.   They are encoded as MARC8 using their Unicode
        // values instead of translated to an NCR, which conflict with some extended latin MARC8 codes.

        // Switching out of extended latin because two of the vulgar fractions are valid there
        val given = marc8ToUnicode.convert("\u001B\u0024\u0029\u0031\u00BC\u00BD\u00BE".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("&#x00BC;&#x00BD;&#x00BE;") }
        )
    }

    @Test
    fun `test vulgar fractions do not break other languages`() {
        // Some vulgar fractions seem to be routinely encoded wrong in MARC8 records.   They are encoded as MARC8 using their Unicode
        // values instead of translated to an NCR, which conflict with some extended latin and arabic MARC8 codes.
        val given = marc8ToUnicode.convert("\u001B\u0029\u0034\u00BC\u00BD\u00BE".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("ڐڑڒ") }
        )
    }
}