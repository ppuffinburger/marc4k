package org.marc4k.converter.marc8

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertAll
import org.marc4k.converter.CharacterConverterResult
import java.text.Normalizer

internal class UnicodeToMarc8Test {
    private val unicodeToMarc8 = UnicodeToMarc8(javaClass.getResourceAsStream("/codetables.xml"))
    private val compatibilityUnicodeToMarc8 = UnicodeToMarc8(javaClass.getResourceAsStream("/codetables.xml"), useCompatibilityDecomposition = true)

    @Test
    fun `test outputsUnicode()`() {
        assertThat(unicodeToMarc8.outputsUnicode()).isFalse()
    }

    @Test
    fun `test constructor(String)`() {
        val unicodeToMarc8 = UnicodeToMarc8("src/main/resources/codetables.xml", true)
        val given = unicodeToMarc8.convert("日本人".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("&#x65E5;&#x672C;&#x4EBA;") }
        )
    }

    @Test
    fun `test convert(CharArray)`() {
        val given = unicodeToMarc8.convert("日本人".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("\u001b\u0024\u0031\u0021\u0042\u0073\u0021\u0043\u0069\u0021\u0030\u0064\u001b\u0028\u0042") }
        )
    }

    @Test
    fun `test convert(CharArray) with only default character sets`() {
        val unicodeToMarc8 = UnicodeToMarc8(javaClass.getResourceAsStream("/codetables.xml"), true)
        val given = unicodeToMarc8.convert("日本人".toCharArray())
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("&#x65E5;&#x672C;&#x4EBA;") }
        )
    }

    @Test
    fun `test convert(ByteArray) with ASCII`() {
        val given = unicodeToMarc8.convert(byteArrayOf(0x48, 0x45, 0x4C, 0x4C, 0x4F))
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("HELLO") }
        )
    }

    @Test
    fun `test convert(String)`() {
        val given = unicodeToMarc8.convert("日本人")
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("\u001b\u0024\u0031\u0021\u0042\u0073\u0021\u0043\u0069\u0021\u0030\u0064\u001b\u0028\u0042") }
        )
    }

    @Test
    fun `test convert(String) with mixed languages`() {
        val given = unicodeToMarc8.convert("日本人 English 日本人")
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("\u001b\u0024\u0031\u0021\u0042\u0073\u0021\u0043\u0069\u0021\u0030\u0064\u001b\u0028\u0042 English \u001B\u0024\u0031\u0021\u0042\u0073\u0021\u0043\u0069\u0021\u0030\u0064\u001B\u0028\u0042") }
        )
    }

    private val testDiacriticsAndSpecialWithCompatibilityData = listOf(
        Triple("A with Acute", "á", "\u00E2a"),
        Triple("A With Circumflex and Dot Below", "ậ", "\u00F2\u00E3a"), // TODO : Dot Below (0xF2) should be the second
        Triple("Combining Double Tilde", "a͠i", "\u00FAa\u00FBi"),
        Triple("Combining Double Inverted Breve", "a͡i", "\u00EBa\u00ECi"),
        Triple("Uppercase Latin O with Horn", "Ơ", "\u00AC"),
        Triple("Lowercase Latin O with Horn", "ơ", "\u00BC"),
        Triple("Uppercase Latin U with Horn", "Ư", "\u00AD"),
        Triple("Uppercase Latin U with Horn", "ư", "\u00BD"),
        Triple("Arabic Alef with Madda above", "آ", "\u001B\u0028\u0033\u0042\u001B\u0028\u0042"),
        Triple("Ellipsis", "…", "..."),
        Triple("Circled Digit 4", "④", "4")
    )

    @TestFactory
    fun `test convert(String) with diacritics and special characters using compatibility`() = testDiacriticsAndSpecialWithCompatibilityData.map { (description, data, expected) ->
        DynamicTest.dynamicTest(description) {
            val given = compatibilityUnicodeToMarc8.convert(data)
            assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java)
            assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo(expected)
        }
    }

    private val testDiacriticsAndSpecialData = listOf(
        Triple("A with Acute", "á", "\u00E2a"),
        Triple("A With Circumflex and Dot Below", "ậ", "\u00F2\u00E3a"), // TODO : Dot Below (0xF2) should be the second
        Triple("Combining Double Tilde", "a͠i", "\u00FAa\u00FBi"),
        Triple("Combining Double Inverted Breve", "a͡i", "\u00EBa\u00ECi"),
        Triple("Uppercase Latin O with Horn", "Ơ", "\u00AC"),
        Triple("Lowercase Latin O with Horn", "ơ", "\u00BC"),
        Triple("Uppercase Latin U with Horn", "Ư", "\u00AD"),
        Triple("Uppercase Latin U with Horn", "ư", "\u00BD"),
        Triple("Arabic Alef with Madda above", "آ", "\u001B\u0028\u0033\u0042\u001B\u0028\u0042"),
        Triple("Ellipsis", "…", "&#x2026;"),
        Triple("Circled Digit 4", "④", "&#x2463;")
    )

    @TestFactory
    fun `test convert(String) with diacritics and special characters`() = testDiacriticsAndSpecialData.map { (description, data, expected) ->
        DynamicTest.dynamicTest(description) {
            val given = unicodeToMarc8.convert(data)
            assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java)
            assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo(expected)
        }
    }

    private val testConvertData = listOf(
        Triple("Subscript Test", "H₂O", "H\u001B\u0062\u0032\u001B\u0028\u0042O"),
        Triple("Superscript Test", "Ba²⁺", "Ba\u001B\u00702+\u001B\u0028\u0042"),
        Triple("Basic Arabic Test", "عربى", "\u001B\u0028\u0033\u0059\u0051\u0048\u0069\u001B\u0028\u0042"),
        Triple("Extended Arabic Test", "۽ٲٳ", "\u001B\u0029\u0034\u00A1\u00A2\u00A3"),
        Triple("Basic Cyrillic Test", "кириллица", "\u001B\u0028\u004EKIRILLICA\u001B\u0028\u0042"),
        Triple("Extended Cyrillic Test", "ґђѓ", "\u001B\u0029\u0051\u00C0\u00C1\u00C2"),
        Triple("Basic Greek Test", "Ελληνικά", "\u001B\u0028\u0053\u0046\u006E\u006E\u006A\u0070\u006C\u006D\u0022\u0061\u001B\u0028\u0042"),
        Triple("Basic Hebrew Test", "עברי", "\u001B\u0028\u0032\u0072\u0061\u0078\u0069\u001B\u0028\u0042"),
        Triple("CJK Test", "日本人", "\u001B\u0024\u0031\u0021\u0042\u0073\u0021\u0043\u0069\u0021\u0030\u0064\u001b\u0028\u0042"),
        Triple("Latin Test", "Latin ©1972", "Latin \u00C31972"),
        Triple("Mix Test", "H₂O ۽ٲٳ English 日本人 ©1972 עברי", "H\u001B\u0062\u0032\u001B\u0028\u0042O \u001B\u0029\u0034\u00A1\u00A2\u00A3 English \u001B\u0024\u0031\u0021\u0042\u0073\u0021\u0043\u0069\u0021\u0030\u0064\u001B\u0028\u0042 \u001B\u0029\u0045\u00C31972 \u001B\u0028\u0032\u0072\u0061\u0078\u0069\u001B\u0028\u0042")
    )

    @TestFactory
    fun `test convert(String) with various data points`() = testConvertData.map { (description, data, expected) ->
        DynamicTest.dynamicTest(description) {
            val given = unicodeToMarc8.convert(data)
            assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java)
            assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo(expected)
        }
    }

    @Test
    fun `test convert(String) with composed unicode`() {
        val given = unicodeToMarc8.convert(Normalizer.normalize("ḱṷṓn", Normalizer.Form.NFC))
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("\u00E2k&#x1E77;\u00E5\u00E2on") }
        )
    }

    @Test
    fun `test convert(String) with composed unicode output as NCR while out of Latin code page`() {
        val given = unicodeToMarc8.convert(Normalizer.normalize("日ṷ", Normalizer.Form.NFC))
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("\u001B\u0024\u0031\u0021\u0042\u0073\u001B\u0028\u0042&#x1E77;") }
        )
    }

    @Test
    fun `test convert(String) with orphaned diacritic`() {
        val given = unicodeToMarc8.convert(Normalizer.normalize("̅", Normalizer.Form.NFC))
        assertAll(
            { assertThat(given).isInstanceOf(CharacterConverterResult.Success::class.java) },
            { assertThat((given as CharacterConverterResult.Success).conversion).isEqualTo("&#x0305;") }
        )
    }
}