package org.marc4k.marc.marc21.bibliographic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BibliographicLevelTest {

    @Test
    fun `test fromValue()`() {
        assertThat(BibliographicLevel.fromValue('a')).isEqualTo(BibliographicLevel.MONOGRAPHIC_COMPONENT_PART)
        assertThat(BibliographicLevel.fromValue('b')).isEqualTo(BibliographicLevel.SERIAL_COMPONENT_PART)
        assertThat(BibliographicLevel.fromValue('c')).isEqualTo(BibliographicLevel.COLLECTION)
        assertThat(BibliographicLevel.fromValue('d')).isEqualTo(BibliographicLevel.SUBUNIT)
        assertThat(BibliographicLevel.fromValue('i')).isEqualTo(BibliographicLevel.INTEGRATING_RESOURCE)
        assertThat(BibliographicLevel.fromValue('m')).isEqualTo(BibliographicLevel.MONOGRAPH_ITEM)
        assertThat(BibliographicLevel.fromValue('s')).isEqualTo(BibliographicLevel.SERIAL)
        assertThat(BibliographicLevel.fromValue('#')).isEqualTo(BibliographicLevel.INVALID)
    }
}