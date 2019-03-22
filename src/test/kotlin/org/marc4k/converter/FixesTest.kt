package org.marc4k.converter

import org.junit.jupiter.api.Test
import org.marc4k.converter.marc8.Fixes
import kotlin.test.assertEquals

internal class FixesTest {
    @Test
    fun `test replaceKnownMarc8EncodingIssues()`() {
        assertEquals("&#x00BC;", Fixes.replaceKnownMarc8EncodingIssues('\u00BC'))
        assertEquals("&#x00BD;", Fixes.replaceKnownMarc8EncodingIssues('\u00BD'))
        assertEquals("&#x00BE;", Fixes.replaceKnownMarc8EncodingIssues('\u00BE'))
    }
}