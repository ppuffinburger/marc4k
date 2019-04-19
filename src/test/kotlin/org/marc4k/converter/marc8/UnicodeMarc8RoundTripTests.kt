package org.marc4k.converter.marc8

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.marc4k.converter.CharacterConverterResult

internal class UnicodeMarc8RoundTripTests {
    private val marc8ToUnicode = Marc8ToUnicode(true)
    private val unicodeToMarc8 = UnicodeToMarc8()

    private val testData = listOf(
        "Test 1" to "テンジン·ギャツォ,",
        "Test 2" to "Hebrew page (טנזין גיאטסו = Ṭenzin Giʼaṭso)",
        "Test 3" to "aבּסתן־דז׳ין־רגיה־מצ׳ו,"
    )

    @TestFactory
    fun `test round trips`() = testData.map { (description, originalText) ->
        DynamicTest.dynamicTest(description) {
            val unicodeToMarc8Result = unicodeToMarc8.convert(originalText)
            assertThat(unicodeToMarc8Result).isInstanceOf(CharacterConverterResult.Success::class.java)

            val marc8ToUnicodeResult = marc8ToUnicode.convert((unicodeToMarc8Result as CharacterConverterResult.Success).conversion)
            assertThat(marc8ToUnicodeResult).isInstanceOf(CharacterConverterResult.Success::class.java)
            assertThat((marc8ToUnicodeResult as CharacterConverterResult.Success).conversion).isEqualTo(originalText)
        }
    }
}