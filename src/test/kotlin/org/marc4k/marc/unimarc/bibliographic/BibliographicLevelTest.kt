package org.marc4k.marc.unimarc.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BibliographicLevelTest {

    @Test
    fun `test fromValue()`() {
        assertThat(BibliographicLevel.fromValue('a')).isEqualTo(BibliographicLevel.ANALYTIC_COMPONENT_PART)
        assertThat(BibliographicLevel.fromValue('c')).isEqualTo(BibliographicLevel.COLLECTION)
        assertThat(BibliographicLevel.fromValue('i')).isEqualTo(BibliographicLevel.INTEGRATING_RESOURCE)
        assertThat(BibliographicLevel.fromValue('m')).isEqualTo(BibliographicLevel.MONOGRAPH)
        assertThat(BibliographicLevel.fromValue('s')).isEqualTo(BibliographicLevel.SERIAL)
        assertThat(BibliographicLevel.fromValue('#')).isEqualTo(BibliographicLevel.INVALID)
    }
}