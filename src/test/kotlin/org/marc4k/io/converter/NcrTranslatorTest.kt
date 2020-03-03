package org.marc4k.io.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

internal class NcrTranslatorTest {
    private val testData = listOf(
        "&#x31;" to "1",
        "&#x032;" to "2",
        "&#x0033;" to "3",
        "&#x00034;" to "4",
        "&#x000035;" to "5",
        "&#x3041;" to "ぁ",
        "abc&#x31;def&#x000035;" to "abc1def5",
        "|&#x3041;|" to "|ぁ|"
    )

    @TestFactory
    fun `test parse(String)`() = testData.map { (given, expected) ->
        DynamicTest.dynamicTest("when given '$given' then expect '$expected'") {
            assertThat(NcrTranslator().parse(given)).isEqualTo(expected)
        }
    }
}