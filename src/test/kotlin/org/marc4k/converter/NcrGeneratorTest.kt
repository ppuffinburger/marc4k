package org.marc4k.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

internal class NcrGeneratorTest {
    private val testData = listOf(
        '1' to "&#x0031;",
        'A' to "&#x0041;",
        'a' to "&#x0061;",
        '؟' to "&#x061F;",
        '陑' to "&#x9651;"
    )

    @TestFactory
    fun `test generate(Char)`() = testData.map { (given, expected) ->
        DynamicTest.dynamicTest("when given '$given' then expect '$expected'") {
            assertThat(NcrGenerator().generate(given)).isEqualTo(expected)
        }
    }

    @Test
    fun `test generate(Int)`() {
        assertThat(NcrGenerator().generate(0x2F81A)).isEqualTo("&#x02F81A;")
    }
}