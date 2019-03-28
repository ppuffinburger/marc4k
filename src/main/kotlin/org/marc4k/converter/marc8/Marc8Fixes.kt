package org.marc4k.converter.marc8

object Marc8Fixes {
    private val knownMarc8EncodingIssuesConversions = mapOf(
        '\u00BC' to "&#x00BC;",     // vulgar fraction 1/4
        '\u00BD' to "&#x00BD;",     // vulgar fraction 1/2
        '\u00BE' to "&#x00BE;"      // vulgar fraction 3/4
    )

    internal fun replaceKnownMarc8EncodingIssues(marc8: Char) = knownMarc8EncodingIssuesConversions[marc8]
}