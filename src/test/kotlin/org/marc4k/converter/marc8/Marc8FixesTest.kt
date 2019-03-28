package org.marc4k.converter.marc8

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class Marc8FixesTest {
    @Test
    fun `test replaceKnownMarc8EncodingIssues()`() {
        assertThat(Marc8Fixes.replaceKnownMarc8EncodingIssues('\u00BC')).isEqualTo("&#x00BC;")
        assertThat(Marc8Fixes.replaceKnownMarc8EncodingIssues('\u00BD')).isEqualTo("&#x00BD;")
        assertThat(Marc8Fixes.replaceKnownMarc8EncodingIssues('\u00BE')).isEqualTo("&#x00BE;")
    }
}